package de.hirola.runningplan.ui.training;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.*;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.model.MutableListLiveData;
import de.hirola.runningplan.model.RunningPlanViewModel;
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

public class TrainingFragment extends Fragment implements AdapterView.OnItemSelectedListener {
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
    //  Trainingszeit
    private Chronometer timer;
    private Handler timeHandler;
    // Intervall des Timers
    private int timerInterval;
    // Timer aktiv?
    private boolean isTimerRunning;
    // was timer active
    private boolean wasTimerRunning;
    //  Trainingszeit in Sekunden
    private int secondsInActivity;
    //  Pause-Zeit in Sekunden
    private long secondsWhenStopped;
    //  Dateipfad für Zwischenspeichern der Trainingsdauer
    /// private var durationSaveFilePath: URL?
    //  Intervall zum Zwischenspeichern (in Sekunden)
    private int saveInterval;
    //  Zeit beim Starten des Trainings
    private LocalDate runningStartDate;
    //  Zeit beim Starten der Pause
    private LocalDate activityStartPauseDate;
    //  Pausen-Zeit berechnet?
    private boolean didPausedSecondsCalculated;
    //  Benachrichtigungen
    // private let userNotificationCenter = UNUserNotificationCenter.current()
    private boolean useNotifications;
    /*//  GPS
    //  Location-Manager
    private var locationManager: CLLocationManager = Global.locationManager
    //  aufgezeichnete Daten
    private var trackLocations: [CLLocation] = []
    //  Index für Genauigkeit - ab wann beginnt die Aufzeichnung
    //  siehe Global
    private var locationRecordingIndex: Int = 0
    //  Dateipfad für Zwischenspeichern des Tracks
    private var trackSaveFilePath: URL?
    //  Track-Aufzeichnung gefunden?
    private var didSavedTrackFound: Bool = false
      */
    private boolean useLocationData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // load the timer values, if the app restored
            // default: 0
            secondsInActivity = savedInstanceState.getInt("secondsInActivity");
            // default: false
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning");
        }
        sharedPreferences = requireContext().getSharedPreferences(
                getString(R.string.preference_file), Context.MODE_PRIVATE);
        // load running plans
        viewModel = new ViewModelProvider(requireActivity()).get(RunningPlanViewModel.class);
        // training data
        // live data
        MutableListLiveData<RunningPlan> mutableRunningPlans = viewModel.getMutableRunningPlans();
        mutableRunningPlans.observe(this, this::onListChanged);
        runningPlans = mutableRunningPlans.getValue();
        // initialize attributes
        initializeAttributes();
        // determine if user (app) can use location data
        setUseLocationData();
        // determine if user (app) can use notifications
        setUseNotifications();
        // set user activated running plan
        setActiveRunningPlan();
        // check if a training can continued
        checkForSavedTraining();
    }

    public View onCreateView(@NotNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View trainingView = inflater.inflate(R.layout.fragment_training, container, false);
        // initialize ui elements
        setViewElements(trainingView);
        return trainingView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save the timer values
        savedInstanceState.putInt("secondsInActivity", secondsInActivity);
        savedInstanceState.putBoolean("isTimerRunning", isTimerRunning);
    }

    @Override
    public void onPause()
    {
        // if the app is paused, stop the timer
        super.onPause();
        wasTimerRunning = isTimerRunning;
        isTimerRunning = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // start the timer again, if he was running
        if (wasTimerRunning) {
            isTimerRunning = true;
        }
        // evtl.wurde ein neuer Laufplan ausgewählt
        setActiveRunningPlan();
        // UI aktualisieren
        showRunningPlanInView();
        showRunningPlanEntryInView();
        // TODO: iOS-Migration
        // App wechselt vom Hintergrund zurück, Timer lief nicht im Hintergrund,
        // Differenz der gesicherten Zeit mit aktueller Zeit zu den aktuellen Sekunden dazurechnen
        /*//  Karten-Einstellungen aktivieren oder deaktivieren
        //  nur wenn kein Training läuft
        if !self.isTrainingRunning {
            self.locationSettings()
        }
        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useNotification) {
            //  Prüfen, ob Benachrichtigung (noch) "erwünscht" sind
            self.requestNotificationAuthorization()
        }
        //  Laufpläne neu laden
        self.loadRunningPlanes()
        //  aktiven Laufplan setzen - kann in Laufplan-Detail geändert worden sein
        self.setActiveRunningPlan()
        //  Startdatum kann geändert wurden sein - PickerView anpassen
        self.trainingDayPickerView.reloadComponent(0)
        //  Laufplan eventl. geändert - Trainingseinträge laden
        self.trainingUnitsPickerView.reloadComponent(0)
        */
    }

    @Override
    public void onStop() {
        super.onStop();
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
        // initialize the timer
        timer = trainingView.findViewById(R.id.chronometer);
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
            int duration = runningPlanEntry.getDuration();
            // Stunden oder Minuten?
            //  Gesamtdauer des Trainings (gespeichert in min)
            if (duration < 60) {
                durationString+= String.valueOf(duration);
                durationString+= " min";
            } else {
                //  in h und min umrechnen
                int hours = (duration * 60) / 3600;
                int minutes = (duration / 60) % 60;
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
    // start recording training
    private void startRecordingTraining() {
        if (useLocationData) {
            if (!isTrainingRunning) {
                //  Aufzeichnung und Monitoring des Trainings neu starten
                //  Reset des (vorherigen) Tracks
                // self.trackLocations.removeAll()
            }
            //  Ereignisverarbeitung (Delegate) starten, wenn neuer Standort verfügbar
            // self.locationManager.startUpdatingLocation()
            //self.locationManager.startMonitoringSignificantLocationChanges()
        }
    }

    // stop recording training
    private void stopRecordingTraining() {
        if (useLocationData) {
            // GPS-Aufzeichnung anhalten
            // self.locationManager.stopUpdatingLocation()
            // self.locationManager.stopMonitoringSignificantLocationChanges()
            // Pause-Zeit "merken"
            // self.activityStartPauseDate = self.trackLocations.last?.timestamp ?? Date()
            // self.didPausedSecondsCalulated = false
        }
    }

    // start the training
    private void startTraining() {
        trainingDaysSpinner.setEnabled(false);
        trainingUnitsSpinner.setEnabled(false);
        if (isTrainingRunning) {
            //  Aufzeichnung (wieder) starten
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
                        getContext(),
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
        // GPS-Aufzeichnung stoppen
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
        // Timer resetten
        resetTimer();
        // Aufzeichnung GPS stoppen
        stopRecordingTraining();
        //  evtl. aufgezeichnete Daten löschen
        //trackLocations.removeAll();
        //  Sicherungen löschen
        removeSavedTraining();
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

    //  Location-Daten aus Cache einlesen und
    //  Aufzeichnung fortsetzen
    private void continueTraining() {
        //  Datei wieder einlesen
        /*if (didSavedTrackFound) {
            do {
                let data = try Data(contentsOf: self.trackSaveFilePath!)
                if let savedLocations = try NSKeyedUnarchiver.unarchiveTopLevelObjectWithData(data) as? [CLLocation] {
                    //  Track wiederherstellen
                    self.trackLocations.removeAll()
                    self.trackLocations.append(contentsOf: savedLocations)
                }
            } catch (let error){
                //  Training muss mit neuem Track fortgesetzt werden
                //  Hinweis an Nutzer
                let alert = UIAlertController(title: AppMessages.readFromFileError,
                        message: NSLocalizedString("Der Track kann nicht fortgesetzt werden.",
                        comment: "Der Track kann nicht fortgesetzt werden."),
                preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                    self.present(alert, animated: true, completion: nil)
                    //  zentrales Logging
                    if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {
                    var errorMessage = LogEntry.debugInformations
                    errorMessage.append(" - ")
                    errorMessage.append(error.localizedDescription)
                    self.app.appLogger.error(errorMessage)
                }
            }
        }
        if self.didSavedDurationFound {
            //  Zeit einlesen und setzen
            //  Datei wieder einlesen
            if self.durationSaveFilePath != nil {
                do {
                    let data = try Data(contentsOf: self.durationSaveFilePath!)
                    if let duration = try NSKeyedUnarchiver.unarchiveTopLevelObjectWithData(data) as? Int {
                        //  Zeit vor Abbruch dazu rechnen
                        self.secondsInActivity += duration
                    }
                } catch (let error){
                    //  Datei kann nicht eingelesen werden, Zeit beginnt bei 0
                    //  Hinweis an Nutzer
                    let alert = UIAlertController(title: AppMessages.readFromFileError,
                            message: NSLocalizedString("Die Trainingsdauer beginnt leider wieder bei 0.",
                            comment: "Die Trainingsdauer beginnt leider wieder bei 0."),
                    preferredStyle: .alert)
                    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                        self.present(alert, animated: true, completion: nil)
                        //  zentrales Logging
                        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {
                        var errorMessage = LogEntry.debugInformations
                        errorMessage.append(" - ")
                        errorMessage.append(error.localizedDescription)
                        self.app.appLogger.error(errorMessage)
                    }
                }
            }
        }
        */
        // Training als aktiv markieren
        isTrainingRunning = true;
        // Timer starten / fortsetzen
        startTimer();
        // Training starten / fortsetzen
        startTraining();
    }

    //  Training "überwachen"
    private void monitoringTraining() {
        if (runningPlanEntry != null && runningUnit != null) {
            // show info
            trainingInfolabel.setText(R.string.training_is_running);
            // Status-Bild anzeigen
            // TODO: Status-Bild trainingactive30x30
            // Monitoring der Trainingszeit
            // TODO: Was ist bei GPS-Timer?
            // Dauer einer Einheit beträgt mindestens 1 min
            if (secondsInActivity >= 60) {
                int duration = runningUnit.getDuration();
                // TODO: Sekunden immer int?
                int minutes = secondsInActivity / 60;
                if (minutes >= duration) {
                    //  Trainingseinheit wurde abgeschlossen
                    //  TODO: Benachrichtigung an Nutzer
                    if (useNotifications) {

                    }
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
    }

    //  Training wurde abgeschlossen
    private void completeTraining() {
        // Timer stoppen
        stopTimer();
        // TODO:
        //  Info-Label wieder auf "normale" Farben setzen
        // setInfoLabelDefaultColors();
        //  Training wurde endgültig gestoppt (abgeschlossen)
        isTrainingRunning = false;
        //  gespeicherte Trainingsdaten löschen
        removeSavedTraining();
        //  show info
        trainingInfolabel.setText(R.string.training_completed);
        // TODO: Status image trainingcompleted30x30
        if (useLocationData) {
            // Aufzeichnung stoppen
            stopRecordingTraining();
            //  Trainingsdaten anzeigen
            String trainingTrackInfoString = "\n";
            trainingTrackInfoString+= R.string.distance;
            trainingTrackInfoString+= ": ";
            //  Länge der Strecke in m
            double distance = 100.00; //self.trackLocations.totalDistance
            if (distance < 999.99) {
                //  Angabe in m
                trainingTrackInfoString+= distance;
                trainingTrackInfoString+= " m";
            } else {
                //  Angabe in km
                distance = distance / 1000;
                // TODO: Formatierung Komma
                trainingTrackInfoString+= distance;
                trainingTrackInfoString+= " km, ";
            }
            //  Geschwindigkeit in m/s
            double averageSpeed = 3.0; //self.trackLocations.averageSpeed
            //  Geschwindigkeit in km/h
            averageSpeed = averageSpeed * 3.6;
            trainingTrackInfoString+= R.string.speed;
            // TODO: Formatierung Komma
            trainingTrackInfoString+= averageSpeed;
            trainingTrackInfoString+= " km/h";
            trainingInfolabel.setText(trainingTrackInfoString);
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

    //
    //  DATA-RECOVERY
    //
    //  Prüfen, ob evtl. bereits ein Track vorliegt, dann fortsetzen
    private void checkForSavedTraining() {
        /*let fileManager = FileManager.default
        let urls = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)
        //  Pfad zu gespeicherten Track
        self.trackSaveFilePath = urls.first?.appendingPathComponent("track.saved")
        //  Pfad zur gespeicherten Trainingsdauer
        self.durationSaveFilePath = urls.first?.appendingPathComponent("duration.saved")
        //  gespeicherter Track vorhanden?
        if self.trackSaveFilePath != nil {
            if fileManager.fileExists(atPath: self.trackSaveFilePath!.path) {
                if fileManager.isReadableFile(atPath: self.trackSaveFilePath!.path) ||
                fileManager.isWritableFile(atPath: self.trackSaveFilePath!.path) {self.didSavedTrackFound = true {
                } else {
                    //  Datei löschen, da nicht verwendbar
                    self.removeSavedTraining()
                }
            }
        }
        //  gespeicherte Trainingsdauer vorhanden?
        if self.durationSaveFilePath != nil {
            if fileManager.fileExists(atPath: self.durationSaveFilePath!.path) {
                if fileManager.isReadableFile(atPath: self.durationSaveFilePath!.path) ||
                fileManager.isWritableFile(atPath: self.durationSaveFilePath!.path) {
                    self.didSavedDurationFound = true
                } else {
                    //  Datei löschen, da nicht verwendbar
                    self.removeSavedTraining()
                }
            }
        }
        if self.didSavedTrackFound || self.didSavedDurationFound {
            //  Nutzer fragen, ob das Training fortgesetzt werden soll
            let alert = UIAlertController(title: NSLocalizedString("Es wurde ein aufgezeichnetes Training gefunden. Fortsetzen?",
                    comment: "Es wurde ein aufgezeichnetes Training gefunden. Fortsetzen?"),
            message: "",
                    preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: NSLocalizedString("Ja", comment: "Ja"),
            style: .default,
                handler: { (action: UIAlertAction!) in
                    //  Training fortsetzen
                    self.continueTraining()
                    return
                }))
                //  Training nicht fortsetzen
                alert.addAction(UIAlertAction(title: NSLocalizedString("Nein", comment: "Nein"),
                style: .cancel,
                        handler: { (action: UIAlertAction!) in
                //  Aufzeichnung löschen
                self.removeSavedTraining()
                return
            }))
            self.present(alert, animated: true, completion: nil)
        }*/
    }

    // Location-Daten zwischenspeichern - für evtl. Abstürze
    // User beendet App ohne Absicht, ...
    private void saveLocations() {
        // Datei in Cache ablegen
        /*if self.trackSaveFilePath != nil {
            do {
                //  Array in Datei schreiben (überschreiben)
                let data = try NSKeyedArchiver.archivedData(withRootObject: self.trackLocations, requiringSecureCoding: false)
                try data.write(to: trackSaveFilePath!)
            } catch (let error){
                //  zentrales Logging
                if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {
                    var errorMessage = LogEntry.debugInformations
                    errorMessage.append(" - ")
                    errorMessage.append(error.localizedDescription)
                    self.app.appLogger.error(errorMessage)
                }
            }
        }*/
    }

    //  aufgezeichnete Daten löschen
    private void removeSavedTraining() {
        //  Dateien löschen
        /*let fileManager = FileManager.default
        //  zwischengespeicherten Track löschen
        if fileManager.fileExists(atPath: self.trackSaveFilePath!.path) {
            if fileManager.isDeletableFile(atPath: self.trackSaveFilePath!.path) {
                do {
                    try fileManager.removeItem(atPath: self.trackSaveFilePath!.path)
                } catch (let error) {
                    //  zentrales Logging
                    if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {
                        var errorMessage = LogEntry.debugInformations
                        errorMessage.append(" - ")
                        errorMessage.append(error.localizedDescription)
                        self.app.appLogger.error(errorMessage)
                    }
                }
            }
        }
        //  zwischengespeicherte Trainingsdauer löschen
        if fileManager.fileExists(atPath: self.durationSaveFilePath!.path) {
            if fileManager.isDeletableFile(atPath: self.durationSaveFilePath!.path) {
                do {
                    try fileManager.removeItem(atPath: self.durationSaveFilePath!.path)
                } catch (let error) {
                    //  zentrales Logging
                    if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {
                        var errorMessage = LogEntry.debugInformations
                        errorMessage.append(" - ")
                        errorMessage.append(error.localizedDescription)
                        self.app.appLogger.error(errorMessage)
                    }
                }
            }
        }*/
    }

    //
    // timer
    // start or restart the timer
    // enable and disable the timer buttons
    private void startTimer() {
        // set the attribute values
        isTimerRunning = true;
        isTrainingPaused = false;
        //  Pause und Stopp sind nun möglich
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
        // add the time, before timer paused
        timer.setBase(SystemClock.elapsedRealtime() + secondsWhenStopped);
        // (re) start the timer
        timer.start();
    }

    // paused the timer
    private void pausedTimer() {
        // stop the timer
        timer.stop();
        // save the stop time
        secondsWhenStopped = timer.getBase() - SystemClock.elapsedRealtime();
        // set the attribute values
        isTimerRunning = true;
        isTrainingPaused = true;
        //  Start-Button ist wieder aktiv, Pause nicht mehr nutzbar
        startButton.setEnabled(true);
        pauseButton.setEnabled(true);
    }

    // stop the timer
    private void stopTimer() {
        // stop the timer
        timer.stop();
        // set the attribute values
        isTimerRunning = false;
        isTrainingPaused = true;
        // Pause und Stop sind nicht mehr möglich
        startButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    // reset the timer
    private void resetTimer() {
        //  reset the timer
        timer.setBase(SystemClock.elapsedRealtime());
        // set the attribute values
        isTimerRunning = false;
        secondsInActivity = 0;
        secondsWhenStopped = 0;
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        // runningStartDate = nil
        // activityStartPauseDate = nil
        // didPausedSecondsCalulated = true
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

    // initialize attributes
    private void initializeAttributes() {
        secondsInActivity = 0;
        secondsWhenStopped = 0;
        saveInterval = 15;
        // timer in sec.
        timerInterval = 1;
        isTimerRunning = false;
        isTrainingPaused = true;
        isTrainingRunning = false;
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

    // determine if the app can use location data
    // TODO: Location
    private void setUseLocationData() {
        //SharedPreferences sharedPref = getContext().getSharedPreferences(
        //        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        useLocationData = false;
    }

    // determine if the app can use notifications
    // TODO: Notifications
    private void setUseNotifications() {
        useNotifications = false;
    }
}