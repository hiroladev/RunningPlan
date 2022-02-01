package de.hirola.runningplan.ui.training;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.runningplan.services.ServiceCallback;
import de.hirola.runningplan.services.timer.TimerServiceConnection;
import de.hirola.runningplan.services.tracking.TrackingServiceConnection;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;

import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import de.hirola.sportslibrary.ui.ModalOptionDialog;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class TrainingFragment extends Fragment implements AdapterView.OnItemSelectedListener, ServiceCallback {

    // Preferences
    private SharedPreferences sharedPreferences;
    // Spinner
    private Spinner trainingDaysSpinner;
    private ArrayAdapter<String> trainingDaysSpinnerArrayAdapter;
    Spinner trainingUnitsSpinner;
    private ArrayAdapter<String>  trainingUnitsSpinnerArrayAdapter;
    // Timer button
    private AppCompatImageButton startButton;
    private AppCompatImageButton stopButton;
    private AppCompatImageButton pauseButton;
    // Label
    private TextView runningPlanNameLabel;
    private TextView trainingInfolabel;
    // app data
    private RunningPlanViewModel viewModel;
    // cached list of running plans
    private List<RunningPlan> runningPlans;
    // the actual running plan, selected by the user
    // if no running plan selected, the plan with the lowest order number will be selected
    private RunningPlan runningPlan;
    // alle Laufpläne abgeschlossen?
    private boolean allRunningPlansCompleted;
    //  aktuell aktive Trainingseinheit zum ausgewählten Trainingsplan
    //  z.B. Woche: 3, Tag: 1 (Montag), 7 min gesamt,  2 min Laufen, 3 min langsames Gehen, 2 min Laufen
    private RunningPlanEntry runningPlanEntry;
    // training day
    private LocalDate trainingDate;
    //  aktive Laufplan-Trainingseinheit
    private RunningUnit runningUnit;
    //  Training wurde gestartet?
    private boolean isTrainingRunning;
    //  Training pausiert?
    private boolean isTrainingPaused;
    //  Trainingsdauer-Aufzeichnung gefunden?
    private boolean didSavedDurationFound;
    //  aktive Laufplan-Trainingseinheit
    //  Fehler beim Speichern der abgeschlossenen Trainingseinheit
    private boolean didCompleteUpdateError;
    private boolean didAllRunningPlanesCompleted;
    //  training time
    BroadcastReceiver backgroundTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            timeToRun = intent
                    .getExtras()
                    .getLong(ServiceCallback.SERVICE_RECEIVER_INTENT_EXRAS_DURATION, 0L);
            // refresh the time label
            updateTimerLabel();
            // check the training state
            monitoringTraining();
        }
    };
    // get the time from background service
    private boolean backgroundTimerServiceIsConnected = false;
    private final TimerServiceConnection timerServiceConnection =
            new TimerServiceConnection(this); // background timer service
    private TextView timerLabel; // timer label
    private long timeToRun = 0; // remaining time of running unit
    // Timer aktiv?
    private boolean isTimerRunning = false;
    // was timer active
    private boolean wasTimerRunning= false;
    // location services
    // get location updates in background as service
    private ActivityResultLauncher<String[]> locationPermissionRequest = null; // must be initialized in onCreate
    private boolean useLocationData; // user want to use location services
    private boolean locationServicesAllowed; // user has using locations allowed and services are available
    private boolean locationTrackingServiceIsConnected = false;
    private Track.Id trackId = null; // the id of the actual track
    private final TrackingServiceConnection trackingServiceConnection =
            new TrackingServiceConnection(this); // service connection

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // load the track id, if the app restored
            trackId = savedInstanceState.getParcelable("trackid");
        }
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
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            locationServicesAllowed = true;
                        } else {
                            // No location access granted.
                            locationServicesAllowed = false;
                        }
                    }
            );
        // register receiver for timer
        requireActivity().registerReceiver(backgroundTimeReceiver,
                new IntentFilter(ServiceCallback.SERVICE_RECEIVER_ACTION));
        // initialize the location tracking service
        handleLocationTrackingService();
        // initialize background timer service
        handleBackgroundTimerService();
        // load running plans
        viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        // training data
        // live data
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        mutableRunningPlans.observe(this, this::onListChanged);
        runningPlans = mutableRunningPlans.getValue();
        // set user activated running plan
        setActiveRunningPlan();
        //TODO: check if a training can continued
    }

    public View onCreateView(@NotNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // restore saved data
        if (savedInstanceState != null) {
            trackId = savedInstanceState.getParcelable("trackId");
        }
        View trainingView = inflater.inflate(R.layout.fragment_training, container, false);
        // initialize ui elements
        setViewElements(trainingView);
        return trainingView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save the track id
        if (trackId != null) {
            savedInstanceState.putParcelable("trackId", trackId);
        }
    }

    @Override
    public void onPause() {
        wasTimerRunning = isTimerRunning;
        isTimerRunning = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // location permissions can be changed by user
        handleLocationTrackingService();
        if (wasTimerRunning) {
            // start the timer again, if he was running
            isTimerRunning = true;
            // refresh the timer label
            long duration = timerServiceConnection.getActualDuration();
            if (duration > -1) {
                timerLabel.setText(String.valueOf(duration));
            }
        }
        // maybe user has changed the active plan
        setActiveRunningPlan();
        // show possible changes in ui
        showRunningPlanInView();
        showRunningPlanEntryInView();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // unregister receiver
        requireActivity().unregisterReceiver(backgroundTimeReceiver);
        // unbind service connections
        trackingServiceConnection.stopAndUnbindService(requireContext());
        timerServiceConnection.stopAndUnbindService(requireContext());
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
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onServiceConnected(ServiceConnection connection) {
        if (connection instanceof TrackingServiceConnection) {
            locationTrackingServiceIsConnected = true;
        }
        if (connection instanceof TimerServiceConnection) {
            backgroundTimerServiceIsConnected = true;
        }
    }

    @Override
    public void onServiceDisconnected(ServiceConnection connection) {
        if (connection instanceof TrackingServiceConnection) {
            locationTrackingServiceIsConnected = false;
        }
        if (connection instanceof TimerServiceConnection) {
            backgroundTimerServiceIsConnected = false;
        }
    }

    @Override
    public void onServiceErrorOccurred(String errorMessage) {
        if (Global.DEBUG) {
            //TODO: logging / alerts
            System.out.println(errorMessage);
        }
    }

    // initialize or stop the location tracking service
    private void handleLocationTrackingService() {
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
            if (locationServicesAllowed && !locationTrackingServiceIsConnected) {
                // bind and start service
                trackingServiceConnection.bindAndStartService(requireContext());
            }
            if (!locationServicesAllowed) {
                // stop the service, if location tracking not allowed now
                trackingServiceConnection.stopAndRemoveTrackRecording();
            }
        }
        if (Global.DEBUG) {
            //TODO: Logging
            System.out.println(locationServicesAllowed);
        }
    }

    // initialize background timer service
    private void handleBackgroundTimerService() {
        timerServiceConnection.bindAndStartService(requireContext());
        if (Global.DEBUG) {
            //TODO: Logging
            System.out.println("Background Timer");
        }
    }

    // set the active running plan for the view
    private void setActiveRunningPlan() {
        //  einen Laufplan vorauswählen und alle entsprechenden Daten darstellen
        boolean userHasRunningPlan = false;
        if (!runningPlans.isEmpty()) {
            User appUser = viewModel.getAppUser();
            if (appUser != null) {
                RunningPlan runningPlan = appUser.getActiveRunningPlan();
                if (runningPlan != null) {
                    int index = runningPlans.indexOf(runningPlan);
                    if (index > -1) {
                        this.runningPlan = runningPlans.get(index);
                    }
                    userHasRunningPlan = true;
                }
            }
            // es konnte kein Plan ermittelt werden
            if (this.runningPlan == null) {
                Optional<RunningPlan>  runningPlan = runningPlans
                        .stream()
                        .filter(r -> !r.completed())
                        .min(Comparator.comparing(RunningPlan::getOrderNumber));
                // den (nicht abgeschlossenen) Plan mit der niedrigsten Nummer zuordnen
                runningPlan.ifPresent(plan -> this.runningPlan = plan);
            }
            // evtl. wurden alle Laufpläne durchgeführt
            if (this.runningPlan == null) {
                // alle Laufpläne wurde abgeschlossen
                if (runningPlans.stream().allMatch(RunningPlan::completed)) {
                    allRunningPlansCompleted = true;
                } else {
                    allRunningPlansCompleted = false;
                }
                // erster Laufplan wird zugewiesen
                runningPlan = runningPlans.get(0);
            }
            // Zuordnung des aktuellen Laufplans beim Nutzer speichern,
            // wenn noch nicht zugeordnet
            if (appUser != null && !userHasRunningPlan) {
                appUser.setActiveRunningPlan(runningPlan);
                try {
                    viewModel.update(appUser);
                } catch (SportsLibraryException exception) {
                    if (Global.DEBUG) {
                        // TODO: Logging
                        exception.printStackTrace();
                    }
                }
            }
            List<RunningPlanEntry> entries = runningPlan.getEntries();
            // set the next uncompleted training day
            Optional<RunningPlanEntry> entry = entries
                    .stream()
                    .filter(r -> !r.completed())
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

    private void setViewElements(@NotNull View trainingView) {
        // timer label starts with 00:00:00
        timerLabel = trainingView.findViewById(R.id.timer);
        updateTimerLabel();
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
            // show the name of active running plan in view
            runningPlanNameLabel.setText(runningPlan.getName());
            // show the training days (running plan entries) of running plan in spinner
            // add data to spinner
            // addAll(java.lang.Object[]), insert, remove, clear, sort(java.util.Comparator))
            // automatically call notifyDataSetChanged.
            trainingDaysSpinnerArrayAdapter.clear();
            trainingDaysSpinnerArrayAdapter.addAll(getTrainingDaysAsStrings());
            if (allRunningPlansCompleted) {
                // TODO: Alle Laufpläne abgeschlossen: UI anpassen
                //  aktive Trainingseinheit (Tag) setzen
                //  erste offene Einheit aus Liste wählen
                // Hinweis an Nutzer
                trainingInfolabel.setText(R.string.all_runninplans_completed);
            } else {
                // select the training date in spinner
                int index = runningPlan.getEntries().indexOf(runningPlanEntry);
                if (index > -1) {
                    trainingDaysSpinner.setSelection(index);
                }
            }
            // TODO: Bilder
            /*
            /  Status-Bild anzeigen
            if let trainingStatusImage = UIImage(named: "trainingcompleted30x30") {
            //  Bild für den Status des Laufplanes
            self.completedEntryImageView.image = trainingStatusImage
             */
            // Dauer des gesamten Trainings anzeigen
            showRunningPlanEntryInView();
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
            String durationString = getString(R.string.total_time)+ " ";
            long duration = runningPlanEntry.getDuration();
            // Stunden oder Minuten?
            //  Gesamtdauer des Trainings (gespeichert in min)
            if (duration < 60) {
                durationString+= String.valueOf(duration);
                durationString+= " min";
            } else {
                //  in h und min umrechnen
                long hours = (duration * 60) / 3600;
                long minutes = (duration / 60) % 60;
                durationString+= String.valueOf(hours);
                durationString+= " h : ";
                durationString+= String.valueOf(minutes);
                durationString+= " min";
            }
            trainingInfolabel.setText(durationString);
        }
    }

    //
    // training methods
    //
    // start or resume recording training
    private void startRecordingTraining() {
        if (locationServicesAllowed) {
            if (isTrainingRunning && trackId != null) {
                //  resume the track
                // TODO: TrackId was not found
                trackingServiceConnection.resumeTrackRecording(trackId);
            } else {
                // start recording a new track
                trackId = trackingServiceConnection.startTrackRecording();
            }
        }
    }

    // stop recording training
    private void stopRecordingTraining() {
        if (locationServicesAllowed) {
            // pause location tracking
            if (isTrainingRunning) {
                trackingServiceConnection.pauseTrackRecording();
            } else {
                // stop recording
                trackingServiceConnection.stopTrackRecording();
            }
        }
    }

    // start the training
    private void startTraining() {
        trainingDaysSpinner.setEnabled(false);
        trainingUnitsSpinner.setEnabled(false);
        if (isTrainingRunning) {
            //  start recording location (again)
            startRecordingTraining();
            // set an info text
            trainingInfolabel.setText(R.string.continue_training);
        } else {
            // prüfen, ob aktueller Tag ein Trainingstag ist und / oder
            // noch Einheiten offen sind
            if (isValidTraining()) {
                // aktuellen Trainingstag in Spinner auswählen
                // erste Trainingseinheit auswählen
                //  Aufzeichnung und Monitoring des Trainings starten
                startRecordingTraining();
                isTrainingRunning = true;
                // show info
                trainingInfolabel.setText(R.string.start_training);
            } else {
                //TODO: Hinweis an Nutzer
                // data could not saved
                // alert dialog to user
                ModalOptionDialog.showMessageDialog(ModalOptionDialog.DialogStyle.CRITICAL,
                        requireContext(),
                        getString(R.string.error),
                        getString(R.string.save_data_error),
                        getString(R.string.ok));
            }
        }
        //  Status-Bild anzeigen
        // TODO: Status-Bild trainingactive30x30
    }

    //  Trainingspause
    private void pauseTraining() {
        // paused record locations
        stopRecordingTraining();
        // show info
        trainingInfolabel.setText(R.string.pause_training);
        // Status-Bild anzeigen
        // TODO: Status-Bild trainingpaused30x30
    }

    //  Trainingsabbruch
    private void cancelTraining() {
        // show info
        trainingInfolabel.setText(R.string.cancel_training);
        // reset timer
        resetTimer();
        // stop recording locations
        stopRecordingTraining();
        //  evtl. aufgezeichnete Daten löschen
        //trackLocations.removeAll();
        //  Flag setzen
        isTrainingRunning = false;
        // Info-Label wieder auf "normale" Farben setzen
        // TODO: Farben
        // enable spinner and training date button
        trainingDaysSpinner.setEnabled(true);
        trainingUnitsSpinner.setEnabled(true);
        // Status-Bild anzeigen
        // TODO: Status-Bild trainingplanned30x30
    }

    //  Training "überwachen"
    private void monitoringTraining() {
        if (runningPlanEntry != null && runningUnit != null) {
            // show info
            trainingInfolabel.setText(R.string.training_is_running);
            // Status-Bild anzeigen
            // TODO: Status-Bild trainingactive30x30
            // Monitoring der Trainingszeit
            if (timeToRun == 0) {
                //  Trainingseinheit wurde abgeschlossen
                // stop the timer
                stopTimer();
                //  Status abgeschlossen für Abschnitt speichern
                try {
                    runningUnit.setCompleted(true);
                    viewModel.update(runningUnit);
                } catch (SportsLibraryException exception) {
                    // error occurred
                    didCompleteUpdateError = true;
                    // TOD: Logging / Info
                    if (Global.DEBUG) {
                        exception.printStackTrace();
                    }
                }
                // weitere Einheiten des Trainingsabschnittes verfügbar?
                // welche Trainingseinheit wurde vom Nutzer ausgewählt?
                //  nächsten Trainingsabschnitt setzen
                //  nächsten Trainingsabschnitt in spinner auswählen
                // TODO: next unit
                if (false) {
                    resetTimer();
                    //  Training geht weiter ...
                    //  Hinweis an Nutzer - Vibration / Ton?
                    //    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
                    // show info
                    trainingInfolabel.setText(R.string.new_training_unit_starts);
                    // Timer (wieder) starten
                    startTimer();
                } else {
                    //  alle Einheiten des Abschnittes (Tages) abgeschlossen
                    //  Benachrichtigung an Nutzer
                    //  Vibration
                    // AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
                    //  Hinweise
                    // self.sendUserNotification(cause:1)
                    // Training abgeschlossen
                    completeTraining();
                }
            }
        }
    }

    //  Training wurde abgeschlossen
    private void completeTraining() {
        // TODO: Info-Label wieder auf "normale" Farben setzen
        // setInfoLabelDefaultColors();
        //  Training wurde endgültig gestoppt (abgeschlossen)
        isTrainingRunning = false;
        //  show info
        trainingInfolabel.setText(R.string.training_completed);
        // TODO: Status image trainingcompleted30x30
        if (locationServicesAllowed) {
            // stops recording locations
            stopRecordingTraining();
            // get the recorded data
            Track recorderdTrack = trackingServiceConnection.getRecordedTrack(trackId);
            if (recorderdTrack == null) {
                ModalOptionDialog.showMessageDialog(ModalOptionDialog.DialogStyle.WARNING,
                        requireContext(),
                        getString(R.string.error),
                        getString(R.string.recorded_track_not_found),
                        getString(R.string.ok));
            } else {
                // show training infos
                String trainingTrackInfoString = getString(R.string.distance);
                trainingTrackInfoString += ": ";
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
                double averageSpeed = recorderdTrack.getAvgspeed();
                //  Geschwindigkeit in km/h
                averageSpeed = averageSpeed * 3.6;
                trainingTrackInfoString += getString(R.string.speed);
                // TODO: Formatierung Komma
                trainingTrackInfoString += averageSpeed;
                trainingTrackInfoString += " km/h";
                trainingInfolabel.setText(trainingTrackInfoString);
            }
        }

        //  Speichern der Trainingsdaten
        //  ohne GPS, dann ohne Track / Route, aber mit Dauer, wenn vom Nutzer gewünscht
        // user setting in one shared preference file
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
        //  Reset der Stopp-Uhr
        resetTimer();
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

    // timer
    // start or restart the timer
    // enable and disable the timer buttons
    private void startTimer() {
        if (backgroundTimerServiceIsConnected) {
            if (wasTimerRunning) {
                // resume timer
                timerServiceConnection.resumeTimer();
            } else {
                // start the background timer
                if (runningUnit != null) {
                    timeToRun = runningUnit.getDuration() * 60;
                    updateTimerLabel();
                    timerServiceConnection.startTimer(timeToRun);
                }
            }
            // set the attribute values
            isTimerRunning = true;
            isTrainingPaused = false;
            //  Pause und Stopp sind nun möglich
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
        }
    }

    // paused the timer
    private void pausedTimer() {
        if (backgroundTimerServiceIsConnected) {
            // pause the timer
            timerServiceConnection.pauseTimer();
            // set the attribute values
            isTimerRunning = true;
            isTrainingPaused = true;
            //  Start-Button ist wieder aktiv, Pause nicht mehr nutzbar
            startButton.setEnabled(true);
            pauseButton.setEnabled(true);
        }
    }

    // stop the timer
    private void stopTimer() {
        if (backgroundTimerServiceIsConnected) {
            // stop the timer
            timerServiceConnection.stopTimer();
            // set the attribute values
            isTimerRunning = false;
            isTrainingPaused = true;
            // Pause und Stop sind nicht mehr möglich
            startButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

    // reset the timer
    private void resetTimer() {
        // set the attribute values
        timeToRun = 0;
        isTimerRunning = false;
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        // refresh the timer label
        updateTimerLabel();
    }

    private void startButtonClicked(View view) {
        // start or resume the timer
        startTimer();
        // start or resume the training
        startTraining();
    }

    private void pauseButtonClicked(View view) {
        // paused the timer
        pausedTimer();
        // paused the training
        pauseTraining();
    }

    private void stopButtonClicked(View view) {
        //  Timer stoppen
        //  Timer läuft?
        if (isTimerRunning) {
            //  TODO: Nutzer fragen if else return
            //  Hinweis an Nutzer
            // Soll das Training wirklich beendet werden?",
            stopTimer();
            cancelTraining();
        }
    }

    // list of training days from selected running plan as string
    @NotNull
    private List<String> getTrainingDaysAsStrings() {
        List<String> trainingDaysStringList = new ArrayList<>();
        if (runningPlan != null && runningPlanEntry != null) {
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
                        if (Global.DEBUG) {
                            // TODO: Logging
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

    // check if valid running plan and training day
    private boolean isValidTraining() {
        // is running plan completed?
        if (runningPlan != null) {
            if (runningPlan.completed()) {
                //TODO: Frage an Nutzer
                //Plan neu starten?
            }
        }
        return true;
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

}