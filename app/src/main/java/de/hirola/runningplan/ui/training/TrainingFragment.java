package de.hirola.runningplan.ui.training;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.runningplan.services.training.TrainingServiceCallback;
import de.hirola.runningplan.services.training.TrainingServiceConnection;
import de.hirola.runningplan.ui.runningplans.RunningPlanListFragment;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.runningplan.util.TrainingNotificationManager;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;
import de.hirola.runningplan.util.ModalOptionDialog;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class TrainingFragment extends Fragment implements AdapterView.OnItemSelectedListener, TrainingServiceCallback {

    private final static String TAG = TrainingFragment.class.getSimpleName();

    private AppLogManager logManager; // app logging
    private SharedPreferences sharedPreferences; // user and app preferences
    private TrainingNotificationManager notificationManager; // sends notification to user

    // app data
    private RunningPlanViewModel viewModel;
    private List<RunningPlan> runningPlans; // cached list of running plans
    private RunningPlan runningPlan; // the actual running plan, selected by the user
                                     // if no running plan selected, the plan with the lowest order number
                                     // will be selected
    private RunningPlanEntry runningPlanEntry; // training entry for a day
    private RunningUnit runningUnit; // active training unit
    private boolean isTrainingRunning = false;
    private boolean isTrainingPaused = false;
    private boolean didCompleteUpdateError; // error occurred while save complete state on units

    // spinner
    private Spinner trainingDaysSpinner;
    private ArrayAdapter<String> trainingDaysSpinnerArrayAdapter;
    private Spinner trainingUnitsSpinner;
    private ArrayAdapter<String>  trainingUnitsSpinnerArrayAdapter;

    // training action button
    private AppCompatImageButton startButton;
    private AppCompatImageButton stopButton;
    private AppCompatImageButton pauseButton;

    // label
    private TextView runningPlanNameLabel;
    private TextView trainingInfolabel;
    private TextView timerLabel; // countdown label

    // training info label
    private ImageView runningPlanStateImageView;
    private ImageView trainingInfoImageView;

    // training data
    private long timeToRun = 0; // remaining time of running unit
    private final TrainingServiceConnection trainingServiceConnection =
            new TrainingServiceConnection(this); // trainings service for timer and location updates
    private ActivityResultLauncher<String[]> locationPermissionRequest = null; // must be initialized in onCreate
    private boolean useLocationData; // user want to use location services
    private boolean locationServicesAllowed; // user has using locations allowed and services are available
    private boolean isTrainingServiceConnected = false; //only if connected to service wie can start a training
    private Track.Id trackId = null; // the id of the actual track
    private final BroadcastReceiver backgroundTimeReceiver = new BroadcastReceiver() { // training time from service
        @Override
        public void onReceive(Context context, Intent intent) {
            timeToRun = intent
                    .getExtras()
                    .getLong(TrainingServiceCallback.SERVICE_RECEIVER_INTENT_EXRAS_DURATION, 0L);
            // refresh the time label
            updateTimerLabel();
            // check the training state
            monitoringTraining();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // restore saved data
        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong("trackId", -1);
            if (id > -1) {
                trackId = new Track.Id(id);
            }
            isTrainingRunning = savedInstanceState.getBoolean("isTrainingRunning", false);
            isTrainingPaused = savedInstanceState.getBoolean("isTrainingPaused", false);
        }
        // app logger
        logManager = AppLogManager.getInstance(requireContext());
        // enable notification for training infos
        notificationManager = new TrainingNotificationManager(requireActivity().getApplicationContext());
        // app preferences
        sharedPreferences = requireContext().getSharedPreferences(
                getString(R.string.preference_file), Context.MODE_PRIVATE);
        // checking location permissions
        useLocationData = sharedPreferences.getBoolean(Global.PreferencesKeys.useLocationData, false);
        locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                // Precise location access granted.
                // No location access granted.
                locationServicesAllowed = fineLocationGranted != null && fineLocationGranted;
                    }
            );
        // register receiver for timer
        requireActivity().registerReceiver(backgroundTimeReceiver,
                new IntentFilter(TrainingServiceCallback.SERVICE_RECEIVER_ACTION));
        // load running plans
        viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        // training data
        // live data
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        mutableRunningPlans.observe(this, this::onListChanged);
        runningPlans = mutableRunningPlans.getValue();
        // set user activated running plan
        setActiveRunningPlan();
        // initialize the location tracking service
        checkLocationPermissions();
        // initialize background timer service
        handleBackgroundTimerService();
    }

    public View onCreateView(@NotNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View trainingView = inflater.inflate(R.layout.fragment_training, container, false);
        // initialize ui elements
        setViewElements(trainingView);
        return trainingView;
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        // save the track id
        if (trackId != null) {
            savedInstanceState.putLong("trackId", trackId.getId());
        }
        // save training state
        savedInstanceState.putBoolean("isTrainingRunning", isTrainingRunning);
        savedInstanceState.putBoolean("isTrainingPaused", isTrainingPaused);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // location permissions can be changed by user
        checkLocationPermissions();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        notificationManager = null;
        requireActivity().unregisterReceiver(backgroundTimeReceiver);
        if (!trainingServiceConnection.isTrainingActive()) {
            // unbind service connections only if no training active
            trainingServiceConnection.stopAndUnbindService(requireActivity().getApplicationContext());
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinnerTrainingDay) {
            // user select another training day (running plan entry)
            // change the selected item in training unit spinner
            if (runningPlan != null) {
                List<RunningPlanEntry> runningPlanEntries = runningPlan.getEntries();
                if (runningPlanEntries.size() > position) {
                    // set the active running plan entry
                    runningPlanEntry = runningPlanEntries.get(position);
                    // update the running units list and the info label
                    showRunningPlanEntryInView();
                }
            }
        } else {
            // user select another training unit
            if (runningPlanEntry != null) {
                List<RunningUnit> runningUnits = runningPlanEntry.getRunningUnits();
                if (runningUnits.size() > position) {
                    // set the selected running unit
                    runningUnit = runningUnits.get(position);
                    // set the new training time
                    timeToRun = runningUnit.getDuration();
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onServiceConnected(ServiceConnection connection) {
        isTrainingServiceConnected = true;
    }

    @Override
    public void onServiceDisconnected(ServiceConnection connection) {
            isTrainingServiceConnected = false;
    }

    @Override
    public void onServiceErrorOccurred(String errorMessage) {
        //TODO: alert to user
        if (logManager.isDebugMode()) {
            logManager.log(null,errorMessage,TAG);
        }
    }

    // check for permissions to track location updates
    private void checkLocationPermissions() {
        if (useLocationData) {
            // check for location permissions
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationServicesAllowed = true;
            } else {
                locationPermissionRequest.launch(new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        }
        if (logManager.isDebugMode()) {
            logManager.log(null,"Location updates are allowed: " + locationServicesAllowed,TAG);
        }
    }

    // initialize background timer service
    private void handleBackgroundTimerService() {
        // bind and start service on application context
        // on fragment the service is destroyed when switching to another fragment
        if (runningPlan != null) {
            trainingServiceConnection.bindAndStartService(requireActivity().getApplicationContext());
            if (logManager.isDebugMode()) {
                logManager.log(null, "Service bind and start.", TAG);
            }
        }
    }

    // set the active running plan for the view
    // is no running plan available, no training is possible
    private void setActiveRunningPlan() {
        // set the active running plan from user
        if (!runningPlans.isEmpty()) {
            User appUser = viewModel.getAppUser();
            if (appUser != null) {
                runningPlan = appUser.getActiveRunningPlan();
                if (runningPlan != null) {
                    int index = runningPlans.indexOf(runningPlan);
                    if (index > -1) {
                        this.runningPlan = runningPlans.get(index);
                    }
                    // check if the users running plan completed
                    if (runningPlan.isCompleted()) {
                        // ask user how we proceed further
                        handleRunningPlan();
                        return;
                    }
                    // set the next uncompleted training day
                    List<RunningPlanEntry> entries = runningPlan.getEntries();
                    Optional<RunningPlanEntry> entry = entries
                            .stream()
                            .filter(r -> !r.isCompleted())
                            .findFirst();
                    entry.ifPresent(planEntry -> runningPlanEntry = planEntry);
                    if (runningPlanEntry != null) {
                        List<RunningUnit> units = runningPlanEntry.getRunningUnits();
                        // TODO: Liste sortiert?
                        Optional<RunningUnit> unit = units
                                .stream()
                                .filter(runningUnit -> !runningUnit.isCompleted())
                                .findFirst();
                        unit.ifPresent(value -> runningUnit = value);
                    }
                }
            }
        } else {
            // info to user
            ModalOptionDialog.showMessageDialog(ModalOptionDialog.DialogStyle.WARNING,
                    requireContext(),
                    getString(R.string.information),
                    getString(R.string.no_running_plans_available),
                    getString(R.string.ok));
        }
    }

    private void setViewElements(@NotNull View trainingView) {
        // timer label starts with 00:00:00
        timerLabel = trainingView.findViewById(R.id.timer);
        updateTimerLabel();
        // training state image view
        // first state info ist paused
        runningPlanStateImageView = trainingView.findViewById(R.id.imageViewRunningPlanState);
        runningPlanStateImageView.setImageResource(R.drawable.active20x20);
        // training state image view
        // first state info ist paused
        trainingInfoImageView = trainingView.findViewById(R.id.imageViewTrainingInfo);
        trainingInfoImageView.setImageResource(R.drawable.baseline_self_improvement_black_24);
        // initialize the training days spinner
        // if user select another day, the spinner for unit changed too
        trainingDaysSpinner = trainingView.findViewById(R.id.spinnerTrainingDay);
        trainingDaysSpinner.setOnItemSelectedListener(this);
        // creating adapter for spinner with empty list
        trainingDaysSpinnerArrayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        // attaching data adapter to spinner with empty list
        trainingDaysSpinner.setAdapter(trainingDaysSpinnerArrayAdapter);
        // initialize the training units spinner
        trainingUnitsSpinner = trainingView.findViewById(R.id.spinnerTrainingUnit);
        trainingUnitsSpinner.setOnItemSelectedListener(this);
        // creating adapter for spinner with empty list
        trainingUnitsSpinnerArrayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        // attaching data adapter to spinner with empty list
        trainingUnitsSpinner.setAdapter(trainingUnitsSpinnerArrayAdapter);
        // Label für den Zugriff initialisieren
        runningPlanNameLabel = trainingView.findViewById(R.id.editTextRunningPlanName);
        trainingInfolabel = trainingView.findViewById(R.id.editTextTrainingInfos);
        // Button listener
        startButton = trainingView.findViewById(R.id.imageButtonStart);
        startButton.setOnClickListener(this::startButtonClicked);
        stopButton = trainingView.findViewById(R.id.imageButtonStop);
        stopButton.setOnClickListener(this::stopButtonClicked);
        pauseButton = trainingView.findViewById(R.id.imageButtonPause);
        pauseButton.setOnClickListener(this::pauseButtonClicked);
        showRunningPlanInView();
        showRunningPlanEntryInView();
    }

    private void showRunningPlanInView() {
        if (runningPlan != null) {
            // display the name of active running plan in view
            runningPlanNameLabel.setText(runningPlan.getName());
            // display the plan state
            if (runningPlan.isCompleted()) {
                runningPlanStateImageView.setImageResource(R.drawable.completed20x20);
            }
            // display the training days (running plan entries) of running plan in spinner
            // add data to spinner
            // addAll(java.lang.Object[]), insert, remove, clear, sort(java.util.Comparator))
            // automatically call notifyDataSetChanged.
            trainingDaysSpinnerArrayAdapter.clear();
            trainingDaysSpinnerArrayAdapter.addAll(getTrainingDaysAsStrings());
            // select the training date in spinner
            int index = runningPlan.getEntries().indexOf(runningPlanEntry);
            if (index > -1) {
                trainingDaysSpinner.setSelection(index);
            }
            showRunningPlanEntryInView();
        } else {
            // no training possible
            // disable the button
            startButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

    // shows the duration of the running plan entry
    private void showRunningPlanEntryInView() {
        if (runningPlanEntry != null) {
            // show the training units of the current running plan entry in spinner
            // add data to spinner
            // addAll(java.lang.Object[]), insert, remove, clear, sort(java.util.Comparator))
            // automatically call notifyDataSetChanged.
            trainingUnitsSpinnerArrayAdapter.clear();
            trainingUnitsSpinnerArrayAdapter.addAll(getTrainingUnitsAsStrings());
            // show the complete training time
            long duration = runningPlanEntry.getDuration();
            String durationString = buildStringForDuration(duration);
            trainingInfolabel.setText(durationString);
            // display state by image
            if (runningPlanEntry.isCompleted()) {
                trainingInfoImageView.setImageResource(R.drawable.baseline_done_black_24);
            } else {
                trainingInfoImageView.setImageResource(R.drawable.baseline_self_improvement_black_24);
            }
        }
    }

    //
    // training
    //
    // start or restart the training
    private void startTraining() {
        if (isTrainingServiceConnected) {
            // disable ui elements to avoid changing while training runs
            trainingDaysSpinner.setEnabled(false);
            trainingUnitsSpinner.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            trainingInfoImageView.setImageResource(R.drawable.baseline_directions_run_black_24);
            // set value for countdown
            timeToRun = runningUnit.getDuration() * 60;
            if (isTrainingRunning) {
                //  resume the training, track id can be null
                trainingInfolabel.setText(R.string.continue_training);
                trainingServiceConnection.resumeTraining(timeToRun, trackId);
            } else if (isValidTraining()) {
                // start a new training
                trainingInfolabel.setText(R.string.start_training);
                trackId = trainingServiceConnection.startTraining(timeToRun, locationServicesAllowed);
                isTrainingRunning = true;
            }
        }
    }

    // paused the training
    private void pauseTraining() {
        if (isTrainingServiceConnected) {
            if (isTrainingRunning) {
                trainingServiceConnection.pauseTraining();
                pauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                trainingInfolabel.setText(R.string.pause_training);
                trainingInfoImageView.setImageResource(R.drawable.baseline_self_improvement_black_24);
                isTrainingPaused = true;
            }
        }
    }

    //  Trainingsabbruch
    private void cancelTraining() {
        if (isTrainingServiceConnected) {
            if (isTrainingRunning) {
                // ask user
                ModalOptionDialog.showYesNoDialog(
                        requireContext(),
                        getString(R.string.question), getString(R.string.ask_for_cancel_training),
                        getString(R.string.ok), getString(R.string.cancel),
                        button -> {
                            if (button == ModalOptionDialog.Button.OK) {
                                // cancel training
                                trainingServiceConnection.cancelTraining();
                                trainingInfolabel.setText(R.string.cancel_training);
                                trainingInfoImageView.setImageResource(R.drawable.baseline_self_improvement_black_24);
                                timeToRun = 0;
                                updateTimerLabel();
                                isTrainingRunning = false;
                                isTrainingPaused = false;
                                // enable spinner and training date button
                                startButton.setEnabled(true);
                                stopButton.setEnabled(false);
                                pauseButton.setEnabled(false);
                                trainingDaysSpinner.setEnabled(true);
                                trainingUnitsSpinner.setEnabled(true);
                            }
                        });
            }
        }
    }

    //  Training "überwachen"
    private void monitoringTraining() {
        if (isTrainingServiceConnected) {
            // show info
            trainingInfolabel.setText(R.string.training_is_running);
            // TODO: Status-Bild trainingactive30x30
            if (timeToRun == 0) {
                // paused the training, maybe there is another one unit
                pauseTraining();
                // unit completed, save the state
                try {
                    runningUnit.setCompleted(true);
                    viewModel.save(runningUnit);
                } catch (SportsLibraryException exception) {
                    // error occurred
                    didCompleteUpdateError = true;
                    // TODO: alert to user
                    if (logManager.isDebugMode()) {
                        logManager.log(exception,"A running unit couldn't set as completed.",TAG);
                    }
                }
                // more units of the training section available
                // than continue the training
                // TODO: next unit
                if (nextUnit()) {
                    // send notification
                    showNotificationForNextUnit();
                    trainingInfolabel.setText(R.string.new_training_unit_starts);
                    // resume training with another unit
                    startTraining();
                } else {
                    // all units of the entry (day) completed
                    completeTraining();
                }
            }
        }
    }

    //  all units for entry are completed
    private void completeTraining() {
        // training entry (day) unit completed
        if (isTrainingServiceConnected) {
            // stop training
            trainingServiceConnection.endTraining();
            // update values / flags
            isTrainingRunning = false;
            timeToRun = 0L;
            // refresh timer label
            updateTimerLabel();
            // display infos
            if (runningPlan.isCompleted()) {
                notificationManager.sendNotification(getString(R.string.running_plan_completed));
                trainingInfolabel.setText(R.string.running_plan_completed);
                runningPlanStateImageView.setImageResource(R.drawable.completed20x20);
            } else {
                notificationManager.sendNotification(getString(R.string.training_completed));
                trainingInfolabel.setText(R.string.training_completed);
            }
            trainingInfoImageView.setImageResource(R.drawable.baseline_done_black_24);
            if (locationServicesAllowed) {
                // get the recorded data
                Track recorderdTrack = trainingServiceConnection.getRecordedTrack(trackId);
                if (recorderdTrack == null) {
                    ModalOptionDialog.showMessageDialog(ModalOptionDialog.DialogStyle.WARNING,
                            requireContext(),
                            getString(R.string.error),
                            getString(R.string.recorded_track_not_found),
                            getString(R.string.ok));
                } else {
                    // show training infos
                    String trainingTrackInfoString = getString(R.string.training_completed) + "\n";
                    trainingTrackInfoString += getString(R.string.distance) + ": ";
                    double runningDistance = recorderdTrack.getDistance();
                    if (runningDistance < 999.99) {
                        //  Angabe in m
                        trainingTrackInfoString += Math.round(runningDistance);
                        trainingTrackInfoString += " m";
                    } else {
                        //  Angabe in km
                        runningDistance = runningDistance / 1000;
                        // TODO: Formatierung Komma
                        trainingTrackInfoString += Math.round(runningDistance);
                        trainingTrackInfoString += " km, ";
                    }
                    trainingTrackInfoString += "\n";
                    //  Geschwindigkeit in m/s
                    double averageSpeed = recorderdTrack.getAverageSpeed();
                    //  Geschwindigkeit in km/h
                    averageSpeed = averageSpeed * 3.6;
                    trainingTrackInfoString += getString(R.string.speed);
                    trainingTrackInfoString += ": ";
                    // TODO: Formatierung Komma
                    trainingTrackInfoString += averageSpeed;
                    trainingTrackInfoString += " km/h";
                    trainingInfolabel.setText(trainingTrackInfoString);
                }
            }

            // save training
            boolean saveTrainings = sharedPreferences.getBoolean(Global.PreferencesKeys.saveTrainings, false);
            if (saveTrainings) {
                // Nutzer fragen, ob gespeichert werden soll
                // TODO: Alert-Dialog
                saveTraining();
            }
            //  Elemente können wieder bedient werden
            trainingDaysSpinner.setEnabled(true);
            trainingUnitsSpinner.setEnabled(true);
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

    // save the training
    private void saveTraining() {
        //TODO: implementieren
    }

    // aktuelle Informationen zum Laufen anzeigen
    private void viewRunningInfos() {
        // Info-Label wieder auf "normale" Farben setzen
        // TODO: Hinweisfarben
        //  self.setInfoLabelDefaultColors()
        /*String trainingInfoString;
        //  zurückgelegte Strecke anzeigen, wenn GPS verfügbar ist
        if (useLocationData) {
            if (trackLocations.count > 0) {
                trainingInfoString+= R.string.distance + ": ";
                double distance = trackLocations.totalDistance;
                //  Angabe in m
                if (distance > 0 && distance < 1000) {
                    // TODO: Format Komma
                    trainingInfoString+= distance + " m\n";
                } else {
                    //  Angabe in km
                    distance = distance / 1000;
                    // TODO: Format Komma
                    trainingInfoString+= distance + " km\n";
                }
                //  aktuelle Geschwindigkeit in km / h umrechnen
                double runningSpeed = trackLocations.last?.speed ?? 0) * 3.6;
                // ... a negative value indicates an invalid speed ...
                if (runningSpeed > 0.5) {
                    //  Informationen zum Label: Standard-Ausgabe
                    trainingInfoString+= R.string.speed + ": ";
                    trainingInfoString+= runningSpeed +" km/h";
                }
                //  Monitoring des Trainings
                if (runningUnit != null) {
                    //  Abgleich der Geschwindigkeit mit dem Soll aus der Bewegungsart
                    //  TODO: Toleranz einbauen, in Abhängigkeit von Zeit
                    double referenceSpeed = runningUnit.getMovementType().getSpeed();
                    if (referenceSpeed > 0.0) {
                        //  zu "langsam"
                        if (runningSpeed + Global.Defaults.movementTolerance < referenceSpeed) {
                            // TODO: Hinweis an Nutzer - Vibration / Ton?
                            // TODO: Farbe
                            trainingInfolabel.setBackgroundColor(Color.YELLOW);
                            trainingInfolabel.setTextColor(Color.BLACK);
                        }
                        //  zu "schnell"
                        if (runningSpeed - Global.Defaults.movementTolerancee > referenceSpeed) {
                            // TODO: Hinweis an Nutzer - Vibration / Ton?
                            // TODO: Farbe
                            trainingInfolabel.setBackgroundColor(Color.RED);
                            trainingInfolabel.setTextColor(Color.WHITE);
                        }
                    }
                }
            }
        }*/
    }

    private void startButtonClicked(View view) {
        startTraining();
    }

    private void pauseButtonClicked(View view) {
        pauseTraining();
    }

    private void stopButtonClicked(View view) {
        cancelTraining();
    }

    // list of training days from selected running plan as string
    @NotNull
    private List<String> getTrainingDaysAsStrings() {
        List<String> trainingDaysStringList = new ArrayList<>();
        if (runningPlan != null) {
            // monday, day 1 and week 1
            LocalDate startDate = runningPlan.getStartDate();
            Iterator<RunningPlanEntry> iterator= runningPlan
                    .getEntries()
                    .stream()
                    .iterator();
            while (iterator.hasNext()) {
                RunningPlanEntry entry = iterator.next();
                int day = entry.getDay();
                int week = entry.getWeek();
                LocalDate trainingDate = startDate.plusDays(day - 1);
                trainingDate = trainingDate.plusWeeks(week - 1);
                String trainingDateAsString = trainingDate
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.getDefault());
                trainingDateAsString+= " (";
                trainingDateAsString+= trainingDate
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                trainingDateAsString+= ")";
                trainingDaysStringList.add(trainingDateAsString);
            }
        }
        return trainingDaysStringList;
    }

    // list of training unit from selected running plan entry as string
    @NotNull
    private List<String> getTrainingUnitsAsStrings() {
        List<String> trainingUnitsStringList = new ArrayList<>();
        if (runningPlanEntry != null) {
            for (RunningUnit runningUnit : runningPlanEntry.getRunningUnits()) {
                // TODO: format spinner
                String trainingUnitsSpinnerElementString = "";
                MovementType movementType = runningUnit.getMovementType();
                if (movementType != null) {
                    // the type of movement
                    try {
                        // load strings from res dynamically
                        String movementKeyString = movementType.getStringForKey();
                        if (movementKeyString.length() > 0) {
                            int remarksResourceStringId = requireContext()
                                    .getResources()
                                    .getIdentifier(movementKeyString, "string", requireContext().getPackageName());
                            trainingUnitsSpinnerElementString = getString(remarksResourceStringId);
                        } else {
                            trainingUnitsSpinnerElementString += R.string.movement_type_not_found;
                        }
                    } catch (Resources.NotFoundException exception) {
                        trainingUnitsSpinnerElementString += R.string.movement_type_not_found;
                        if (logManager.isDebugMode()) {
                            logManager.log(exception,null,TAG);
                        }
                    }
                    // running unit duration
                    trainingUnitsSpinnerElementString += " (";
                    trainingUnitsSpinnerElementString += String.valueOf(runningUnit.getDuration());
                    trainingUnitsSpinnerElementString += " min)";

                } else {
                    trainingUnitsSpinnerElementString += R.string.movement_type_not_found;
                }
                // add the string value for the movement type to the list
                trainingUnitsStringList.add(trainingUnitsSpinnerElementString);
            }
        }
        return trainingUnitsStringList;
    }

    // refresh the cached list of running plans
    private void onListChanged(List<RunningPlan> changedList) {
        runningPlans = changedList;
    }

    // set the next unit of current running plan entry
    private boolean nextUnit() {
        // welche Trainingseinheit wurde vom Nutzer ausgewählt?
        //  nächsten Trainingsabschnitt setzen
        //  nächsten Trainingsabschnitt in spinner auswählen
        if (runningPlanEntry != null) {
            List<RunningUnit> units = runningPlanEntry.getRunningUnits();
            Optional<RunningUnit> unit = units
                    .stream()
                    .filter(runningUnit -> !runningUnit.isCompleted())
                    .findFirst();
            if (unit.isPresent()) {
                runningUnit = unit.get();
                setRunningUnit();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // set the values if a running unit selected
    // called by nextUnit
    private void setRunningUnit() {
        List<RunningUnit> units = runningPlanEntry.getRunningUnits();
        // select the unit in spinner
        int index = units.indexOf(runningUnit);
        if (index > -1) {
            trainingUnitsSpinner.setSelection(index);
        }
        // set the training time
        timeToRun = runningUnit.getDuration();
        // update training info label
        // show the training time for the unit
        String durationString = getString(R.string.unit_time)+ ": " + buildStringForDuration(timeToRun);
        trainingInfolabel.setText(durationString);
    }

    // ask user to reset the plan (repeat),
    // select next plan (by order number) or
    // select plan in list
    private void handleRunningPlan() {
        // active running plan is completed
        // ask user for action
        if (runningPlans != null && runningPlan != null) {
            // deactivate option 3 (argument is null) if all running plans completed
            String optionThreeButtonText = null;
            if (runningPlans.stream().allMatch(RunningPlan::isCompleted)) {
                optionThreeButtonText = getString(R.string.select_running_plan_option_3);
            }
            ModalOptionDialog.showThreeOptionsDialog(
                    requireContext(),
                    getString(R.string.running_plan_completed),
                    getString(R.string.title_select_new_running_plan_options),
                    getString(R.string.select_running_plan_option_1),
                    getString(R.string.select_running_plan_option_2),
                    optionThreeButtonText,
                    button -> {
                        // reset the active running plan
                        if (button == ModalOptionDialog.Button.OPTION_1) {
                            // reset
                            resetRunningPlan();
                        }
                        // select the next running plan by order number
                        if (button == ModalOptionDialog.Button.OPTION_2) {
                            // select from list, open list fragment with running plans
                            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                            Fragment fragment = new RunningPlanListFragment();
                            fragmentTransaction.replace(R.id.content, fragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }
                        // select from list, open list fragment
                        if (button == ModalOptionDialog.Button.OPTION_3) {
                            Optional<RunningPlan> nextRunningPlan = runningPlans
                                    .stream()
                                    .filter(r -> !r.isCompleted())
                                    .min(Comparator.comparing(RunningPlan::getOrderNumber));
                            // try to get the next running plan
                            nextRunningPlan.ifPresent(plan -> runningPlan = plan);
                            // set the new plan as active
                            User appUser = viewModel.getAppUser();
                            if (appUser != null) {
                                appUser.setActiveRunningPlan(runningPlan);
                                try {
                                    viewModel.save(appUser);
                                    // recall the method to set active running plan
                                    setActiveRunningPlan();
                                } catch (SportsLibraryException exception) {
                                    if (logManager.isDebugMode()) {
                                        logManager.log(exception,null,TAG);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    // Reset the running plan. All units are reset as uncompleted.
    private void resetRunningPlan() {
        if (runningPlans != null && runningPlan != null) {
            List<RunningPlanEntry> entries = runningPlan.getEntries();
            // reset units
            for (RunningPlanEntry entry : entries) {
                List<RunningUnit> units = entry.getRunningUnits();
                for (RunningUnit unit : units) {
                    unit.setCompleted(false);
                    // update in datastore
                    try {
                        viewModel.save(unit);
                    } catch (SportsLibraryException exception) {
                        if (logManager.isDebugMode()) {
                            //TODO: info to user
                            exception.printStackTrace();
                        }
                    }
                }
            }
            // refresh ui
            setActiveRunningPlan();
        }
    }

    // check if valid running plan and training day
    private boolean isValidTraining() {
        // is running plan completed?
        if (runningPlan != null) {
            if (runningPlan.isCompleted()) {
                //TODO: Frage an Nutzer
                //Plan neu starten?
            }
        }
        return true;
    }

    private String buildStringForDuration(long duration) {
        // show the complete training time
        String durationString = getString(R.string.total_time)+ " ";
        // display in hour or minutes?
        if (duration < 60) {
            durationString+= String.valueOf(duration);
        } else {
            //  in h und min umrechnen
            long hours = (duration * 60) / 3600;
            long minutes = (duration / 60) % 60;
            durationString+= String.valueOf(hours);
            durationString+= " h : ";
            durationString+= String.valueOf(minutes);
        }
        durationString+= " min";
        return durationString;
    }

    // format the time and refresh the label
    private void updateTimerLabel() {
        // duration in seconds
        // 00:00:00 (hh:mm:ss)
        long hh= timeToRun / 3600;
        long mm = (timeToRun % 3600) / 60;
        long ss = timeToRun % 60;
        timerLabel.setText(String.format(Locale.ENGLISH, "%02d:%02d:%02d", hh, mm, ss));
    }

    // send notification for next unit
    private void showNotificationForNextUnit() {
        // build the message
        int resID = 0;
        long duration = 0;
        if (runningUnit != null) {
            resID = getResources().getIdentifier(
                    runningUnit.getMovementType().getStringForKey(),
                    "string",
                    requireActivity().getPackageName());
            duration = runningUnit.getDuration();
        }
        if (resID == 0) {
            // movement type not found
            resID = R.string.movement_type_not_found;
        }
        String notificationMessage = getString(R.string.new_training_unit_starts)
                + ": "
                + getString(resID) + " (" + duration + " min)";
        notificationManager.sendNotification(notificationMessage);
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            VibratorManager vibratorManager =
                    (VibratorManager) requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);

            vibrator = vibratorManager.getDefaultVibrator();

        } else {
            // backward compatibility for Android API < 31,
            // VibratorManager was only added on API level 31 release.
            // noinspection deprecation
            vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        }

        final int DELAY = 0, VIBRATE = 1000, SLEEP = 1000, START = 3;
        long[] vibratePattern = {DELAY, VIBRATE, SLEEP};

        vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, START));

    }

}