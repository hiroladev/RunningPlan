package de.hirola.runningplan.ui.training;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;

import de.hirola.runningplan.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.hirola.sportslibrary.model.RunningPlanEntry;
import de.hirola.sportslibrary.model.RunningUnit;
import de.hirola.sportslibrary.model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class TrainingFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    // Spinner
    Spinner trainingUnitsSpinner;
    // Button
    Button selectTrainingDayButton;
    Button startButton;
    Button stopButton;
    Button pauseButton;
    // Label
    TextView runningPlanNameLabel;
    TextView trainingDateLabel;
    TextView trainingInfolabel;
    // app data
    private RunningPlanViewModel viewModel;
    // training data
    private List<RunningPlan> runningPlans;
    // the actual running plan, selected by the user
    // if no running plan selected, the plan with the lowest order number will be selected
    private RunningPlan runningPlan;
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
    //  Timer-Objekt
    // private var timer: Timer?
    // Timer aktiv?
    private boolean isTimerRunning;
    //  Trainingszeit in Sekunden
    private int secondsInActivity;
    //  Pause-Zeit in Sekunden
    private int secondsInPause;
    //  Dateipfad für Zwischenspeichern der Trainingsdauer
    /// private var durationSaveFilePath: URL?
    //  Intervall zum Schreiben in Sekunden - 15?
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
        // load running plans
        viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        runningPlans = viewModel.getRunningPlans().getValue();
        // initialize attributes
        secondsInActivity = 0;
        secondsInPause = 0;
        // TODO: aus Global
        saveInterval = 15;
        isTimerRunning = false;
        // determine if user (app) can use location data
        setUseLocationData();
        // determine if user (app) can use notifications
        setUseNotifications();
        // set user activated running plan
        setActiveRunningPlan();
        // check if a training can continued
        checkForSavedTraining();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View trainingView = inflater.inflate(R.layout.fragment_training, container, false);
        // initialize the spinner
        trainingUnitsSpinner = trainingView.findViewById(R.id.spinner);
        // Label für den Zugriff initialisieren
        runningPlanNameLabel = trainingView.findViewById(R.id.editTextRunningPlanNameLabel);
        trainingDateLabel = trainingView.findViewById(R.id.editTextTrainingDateLabel);
        trainingInfolabel = trainingView.findViewById(R.id.editTextTrainingInfoLabel);
        // Button listener
        selectTrainingDayButton = trainingView.findViewById(R.id.buttonSelectTrainingDay);
        selectTrainingDayButton.setOnClickListener(this::selectTrainingDayButtonClicked);
        startButton = trainingView.findViewById(R.id.imageButtonStart);
        startButton.setOnClickListener(this::startButtonClicked);
        stopButton = trainingView.findViewById(R.id.imageButtonStop);
        stopButton.setOnClickListener(this::stopButtonClicked);
        pauseButton = trainingView.findViewById(R.id.imageButtonPause);
        pauseButton.setOnClickListener(this::pauseButtonClicked);
        // Spinner listener
        Spinner spinner = trainingView.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> runningPlanEntriesNames = new ArrayList<>();
        if (runningPlan != null) {
            List<RunningPlanEntry> entries = runningPlan.getEntries();
        }
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, runningPlanEntriesNames);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        // Inflate the layout for this fragment
        return trainingView;
    }

    @Override
    public void onResume() {
        super.onResume();
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

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setActiveRunningPlan() {
        //  einen Laufplan vorauswählen und alle entsprechenden Daten darstellen
        if (!runningPlans.isEmpty()) {
            User appUser = viewModel.getAppUser().getValue();
            if (appUser != null) {
                RunningPlan runningPlan = appUser.getActiveRunningPlan();
                if (runningPlan != null) {
                    int index = runningPlans.indexOf(runningPlan);
                    if (index > -1) {
                        this.runningPlan = runningPlans.get(index);
                    }
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
                    // Hinweis an Nutzer
                    trainingInfolabel.setText(R.string.all_runninplans_completed);
                    // TODO: Bilder
                    /*
                    /  Status-Bild anzeigen
                    if let trainingStatusImage = UIImage(named: "trainingcompleted30x30") {
                    //  Bild für den Status des Laufplanes
                    self.completedEntryImageView.image = trainingStatusImage
                     */
                }
                // erster Laufplan wird zugewiesen
                runningPlan = runningPlans.get(0);
                // TODO: Images anpassen, s.o.
            }
            // Zuordnung des aktuellen Laufplans beim Nutzer speichern
            if (appUser != null) {
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
            // Name des Laufplans anzeigen (aktualisieren)
            runningPlanNameLabel.setText(runningPlan.getName());
            //  aktive Trainingseinheit (Tag) setzen
            //  erste offene Einheit aus Liste wählen
            List<RunningPlanEntry> entries = runningPlan.getEntries();
            // TODO: sind die Abschnitte in der richtigen Reihenfolge?
            Optional<RunningPlanEntry> entry = entries
                    .stream()
                    .filter(RunningPlanEntry::completed)
                    .findFirst();
            entry.ifPresent(planEntry -> runningPlanEntry = planEntry);
            if (runningPlanEntry != null) {
                // show the training day
                showTrainingDate();
                List<RunningUnit> units = runningPlanEntry.getRunningUnits();
                // TODO: Liste sortiert?
                Optional<RunningUnit> unit = units
                        .stream()
                        .filter(runningUnit -> !runningUnit.isCompleted())
                        .findFirst();
                unit.ifPresent(value -> runningUnit = value);
                // TODO: Spinner select
                //  Dauer des gesamten Trainings anzeigen
                showRunningPlanEntryInView();
            }
        }
    }

    // shows the duration of the running plan entry
    private void showRunningPlanEntryInView() {
        if (runningPlanEntry != null) {
            String durationString = getString(R.string.total_time)+ " ";
            int duration = runningPlanEntry.duration();
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
                trainingInfolabel.setText(durationString);
            }
        }
    }

    // shows the selected training day with name of weekday
    private void showTrainingDate() {
        int day = runningPlanEntry.getDay();
        int week = runningPlanEntry.getWeek();
        // start date is a monday
        trainingDate = runningPlan.getStartDate();
        // add day and week
        trainingDate.plusDays(day - 1);
        trainingDate.plusWeeks(week - 1);
        DayOfWeek dayOfWeek = trainingDate.getDayOfWeek();
        String trainingDateString = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
        trainingDateString+= " (";
        trainingDateString+= trainingDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        trainingDateString+= ")";
        trainingDateLabel.setText(trainingDateString);
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
        //  set spinner and training date button disabled
        trainingUnitsSpinner.setEnabled(false);
        selectTrainingDayButton.setEnabled(false);
        if (isTrainingRunning) {
            //  Aufzeichnung (wieder) starten
            startRecordingTraining();
            // set an info text
            trainingInfolabel.setText(R.string.continue_training);
        } else {
            //  Aufzeichnung und Monitoring des Trainings starten
            startRecordingTraining();
            isTrainingRunning = true;
            // show info
            trainingInfolabel.setText(R.string.start_training);
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
        trainingUnitsSpinner.setEnabled(true);
        selectTrainingDayButton.setEnabled(true);
        // show info
        trainingInfolabel.setText(R.string.pause_training);
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
        boolean saveTrainings = false;
        SharedPreferences userSettings = Objects.requireNonNull(getContext())
                .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (userSettings != null) {
            saveTrainings = userSettings.getBoolean(Global.PreferencesKeys.saveTrainings, false);
        }
        if (saveTrainings) {
            //  Nutzer fragen, ob gespeichert werden soll
            // TODO: Alert-Dialog
            saveTraining();
        }

        //  PickerView-Elemente können wieder bedient werden
        self.trainingDayPickerView.isUserInteractionEnabled = true
        self.trainingUnitsPickerView.isUserInteractionEnabled = true

        //  Reset der Stopp-Uhr
        self.resetTimer()

    }

    //  Lauf-Infos anzeigen
    private void viewRunningInfos() {

        //  Info-Label wieder auf "normale" Farben setzen
        self.setInfoLabelDefaultColors()

        var labelString: String = ""

        //  zurückgelegte Streck anzeigen
        //  wenn GPS verfügbar
        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            if self.trackLocations.count > 0 {

                var distance = self.trackLocations.totalDistance

                //  Angabe in m
                if distance > 0 && distance < 1000 {

                    labelString = NSLocalizedString("zurückgelegte Strecke: ", comment: "zurückgelegte Strecke: ")
                    labelString.append(String(format: "%.2f", distance))
                    labelString.append(" m")
                    labelString.append("\n")

                }

                //  Angabe in km
                if distance > 999 {

                    labelString = NSLocalizedString("zurückgelegte Strecke: ", comment: "zurückgelegte Strecke: ")
                    distance = distance / 1000
                    labelString.append(String(format: "%.2f", distance))
                    labelString.append(" km")
                    labelString.append("\n")

                }

                //  aktuelle Geschwindigkeit in km / h umrechnen
                let runningSpeed = (self.trackLocations.last?.speed ?? 0) * 3.6

                // ... a negative value indicates an invalid speed ...
                if runningSpeed > 0.5 {

                    //  Infos zum Label: Standard-Ausgabe
                    labelString.append(NSLocalizedString("Geschwindigkeit: ", comment: "Geschwindigkeit: "))
                    labelString.append(String(format: "%.2f", runningSpeed))
                    labelString.append(" km/h")

                }

                //  Monitoring des Trainings
                if self.runningUnit != nil {

                    //  Abgleich der Geschwindigkeit mit dem Soll aus der Bewegungsart
                    //  TODO: Toleranz einbauen, in Abhängigkeit von Zeit
                    let referenceSpeed = runningUnit!.movementType?.speed ?? 0.0
                    if referenceSpeed > 0.0 {

                        //  zu "langsam"
                        if runningSpeed + Global.movementTolerance < referenceSpeed {

                            //  TODO: Hinweis an Nutzer - Vibration / Ton?
                            self.runningInfoLabel.backgroundColor = Global.AppAppearance.iOSWarningColor
                            self.runningInfoLabel.textColor = Global.AppAppearance.iOSWarningContrastColor

                        }

                        //  zu "schnell"
                        if runningSpeed - Global.movementTolerance > referenceSpeed {

                            //  TODO: Hinweis an Nutzer - Vibration / Ton?
                            self.runningInfoLabel.backgroundColor = Global.AppAppearance.iOSWarningColor
                            self.runningInfoLabel.textColor = Global.AppAppearance.iOSWarningContrastColor

                        }

                    }

                }

            }

        }

    }

    //
    //  DATA-RECOVERY
    //
    //  Prüfen, ob evtl. bereits ein Track vorliegt, dann fortsetzen
    private void checkForSavedTraining() {

        let fileManager = FileManager.default
        let urls = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)
        //  Pfad zu gespeicherten Track
        self.trackSaveFilePath = urls.first?.appendingPathComponent("track.saved")
        //  Pfad zur gespeicherten Trainingsdauer
        self.durationSaveFilePath = urls.first?.appendingPathComponent("duration.saved")

        //  gespeicherter Track vorhanden?
        if self.trackSaveFilePath != nil {

            if fileManager.fileExists(atPath: self.trackSaveFilePath!.path) {

                if fileManager.isReadableFile(atPath: self.trackSaveFilePath!.path) ||
                fileManager.isWritableFile(atPath: self.trackSaveFilePath!.path) {

                    self.didSavedTrackFound = true

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

        }

    }

    //  Location-Daten zwischenspeichern - für evtl. Abstürze
    //  User beendet App ohne Absicht, ...
    private void saveLocations() {

        //  Datei in Cache ablegen
        if self.trackSaveFilePath != nil {

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

        }

    }

    //  aufgezeichnete Daten löschen
    private void removeSavedTraining() {

        //  Dateien löschen
        let fileManager = FileManager.default

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

        }

    }

    //
    //  TIMER
    //
    //  Start der Stopp-Uhr
    private void startTimer() {

        //  Zeit im Cache sichern
        if self.durationSaveFilePath == nil {

            //  Dateipfad zuweisen
            //  wird von Timer-Funktion in Intervalle gesichert
            let fileManager = FileManager.default
                let urls = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)
                self.durationSaveFilePath = urls.first?.appendingPathComponent("duration.saved")

        }

        //  Timer starten, läuft im Hintergrund jedoch nicht weiter
        //  ohne GPS wird versucht über die Zeitdiffernz die Trainingszeit im Hintergrund
        //  "weiterlaufen zu lasen"
        if !self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            //  Startzeit merken
            self.runningStartDate = Date()

        }

        //  angegebene Funktion wird aller 1 s aufgerufen
        self.timer = Timer.scheduledTimer(timeInterval: 1,
                target: self,
                selector: (#selector(timerEventOccured)),
        userInfo: nil,
                repeats: true)

        self.isTimerRunning = true
        self.isTrainingPaused = false

        //  Pause und Stop sind nun möglich
        self.startButton.isEnabled = false
        self.pauseButton.isEnabled = true
        self.stopButton.isEnabled = true

    }

    //  Pause der Stopp-Uhr
    private void pausedTimer() {

        //  Timer anhalten
        self.timer?.invalidate()
        self.isTimerRunning = true
        self.isTrainingPaused = true

        //  Start-Button ist wieder aktiv, Pause nicht mehr nutzbar
        self.startButton.isEnabled = true
        self.pauseButton.isEnabled = false

    }

    //  Stoppen der Stopp-Uhr
    private void stopTimer() {

        //  Timer stoppen
        self.timer?.invalidate()
        self.isTimerRunning = false
        self.isTrainingPaused = true

        //  Pause und Stop sind nicht mehr möglich
        self.startButton.isEnabled = true
        self.pauseButton.isEnabled = false
        self.stopButton.isEnabled = false

    }

    //  Reset der Stopp-Uhr
    private void resetTimer() {

        //  Timer resetten
        self.isTimerRunning = false
        self.timer?.invalidate()
        self.secondsInActivity = 0
        self.secondsInPause = 0
        self.runningStartDate = nil
        self.activityStartPauseDate = nil
        self.didPausedSecondsCalulated = true
        self.timerLabel.text = "00:00:00"

    }

    //  App geht in den Hintergrund, Timer läuft nicht im Hintergrund,
    //  Timer anhalten
    private vvoid deactivateTimer() {

        //  Timer anhalten
        if self.isTimerRunning {

            self.timer?.invalidate()

        }

    }

    //  App wechselt vom Hintergrund zurück, Timer lief nicht im Hintergrund,
    //  Differenz der gesicherten Zeit mit aktuelle Zeit zu den aktuellen Sekunden dazurechnen
    private void reactivateTimer() {

        //  Timer wieder starten
        if self.isTimerRunning && !self.isTrainingPaused {

            // "vergangene" Zeit vom Start des Trainings bis jetzt dazu rechnen
            //  funktioniert leider nicht zuverlässig, besser über GPS aktualisieren
            if self.runningStartDate != nil {

                let deltaTime: Int = Int(Date().timeIntervalSince(self.runningStartDate!))
                self.secondsInActivity += deltaTime

            }

            //  Timer fortsetzen
            self.startTimer()

        }

    }

    //  Dauer des Trainingsabschnitt kontrollieren und Timer-Label aktualisieren.
    private void timerEventOccured() {

        //  Zeit nur erhöhen, wenn kein GPS verfügbar ist
        //  ansonsten werden die Sekunden über die Zeitdiffernez ermittelt
        if !self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            //  Zeit vor "unfreiwilligem" Abbruch dazurechnen
            self.secondsInActivity += 1

        }

        //  Timer-Label aktualisieren
        let time = TimeInterval(self.secondsInActivity)
        let hours = Int(time) / 3600
        let minutes = Int(time) / 60 % 60
        let seconds = Int(time) % 60
        self.timerLabel.text = String(format:"%02i:%02i:%02i", hours, minutes, seconds)

        //  Zeit aller x Sekunden im Cache sichern
        if self.durationSaveFilePath != nil && self.secondsInActivity % self.saveInterval == 0 {

            do {

                //  Zeit in Datei schreiben (überschreiben)
                let data = try NSKeyedArchiver.archivedData(withRootObject: self.secondsInActivity, requiringSecureCoding: false)
                try data.write(to: durationSaveFilePath!)

            } catch (let error){

                //  zentrales Logging
                if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {

                    var errorMessage = LogEntry.debugInformations
                    errorMessage.append(" - ")
                    errorMessage.append(error.localizedDescription)
                    self.app.appLogger.error(errorMessage)

                }

            }

        }

        // Training überwachen
        self.monitoringTraining()

    }

    //
    //  USER-NOTIFICATIONS
    //
    //  Benachrichtigung senden, wenn neue Trainingseinheit beginnt (0) oder
    //  Training abgeschlossen ist (1)
    private void sendUserNotification(cause: Int) {

        // Inhalt der Benachrichtigung
        let notificationContent = UNMutableNotificationContent()

        switch cause {

            //  Trainingsabschnitt abgeschlossen
            case 0:

                // Titel des Benachrichtigungsinhaltes
                notificationContent.title = NSLocalizedString("Neuer Abschnitt beginnt", comment: "Ein neuer Trainingsabschnitt beginnt")

                //  weitere Informationen
                var message = NSLocalizedString("Prima gemacht und nun weiter!", comment: "Prima gemacht und nun weiter!")

                //  nächsten Abschnitt anzeigen
                //  aktueller Abschnitt
                var index = self.trainingUnitsPickerView.selectedRow(inComponent: 0)
                if index > -1 {

                index += 1
                if let units = self.runningPlanEntry?.runningUnits {

                    if index < units.count {

                        //  Bewegungsart anzeigen
                        if let actualMovementType = units[index].movementType {

                            message.append("\n")
                            message.append(NSLocalizedString("Bewegungsart: ", comment: "Bewegungsart: "))
                            message.append(actualMovementType.stringForKey)

                        }

                    }

                }

            }

            notificationContent.body = message

            //  Tagestraining abgeschlossen
            case 1:

                // Titel des Benachrichtigungsinhaltes
                notificationContent.title = NSLocalizedString("Training abgeschlossen", comment: "Training wurde abgeschlossen")
                //  weitere Informationen
                notificationContent.body = NSLocalizedString("Super, für heute hast Du es geschafft!", comment: "Super, für heute hast Du es geschafft!")

            default:
                return
        }

        //  Anzahl der "Nummern" am App-Icon
        notificationContent.badge = NSNumber(value: 1)

        //  Benachrichtigungs-Sound
        notificationContent.sound = UNNotificationSound.default

        // Bild zur Benachrichtigung hinzufügen
        if let url = Bundle.main.url(forResource: "winner60x60", withExtension: "png") {

            if let attachment = try? UNNotificationAttachment(identifier: "dune", url: url, options: nil) {

                notificationContent.attachments = [attachment]
            }
        }

        //  Trigger zum Auslösen der Benachrichtigung, in x Sekunden, nicht wiederholen -> sofort und einmal
        //  NSException: time interval must be greater than 0'
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)

        //  Anforderung zur Benachrichtigung
        let request = UNNotificationRequest(identifier: "RunningNotification", content: notificationContent, trigger: trigger)

        //  Benachrichtung an Center übergeben
        self.userNotificationCenter.add(request) { (error) in

            if let error = error {

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

    //  Zustimmung Nutzer für Benachrichtigungen
    private void requestNotificationAuthorization() {

        //  Benachrichtigungen an Nutzer
        let notificationCenter = UNUserNotificationCenter.current()
        let notificationOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        notificationCenter.requestAuthorization(options: notificationOptions) { (granted, error) in

            if !granted {

                //  User hat Zustimmung aufgehoben, z.B. in den Systemeinstellungen
                //  User-Parameter wird angepasst
                self.app.userSettings.setValue(false, forKey: UserSettings.KeyNames.useNotification)

                //  Hinweis an Nutzer
                let alert = UIAlertController(title: AppMessages.notificationError, message: "", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                    self.present(alert, animated: true, completion: nil)

            }

            if (error != nil) {

                //  Fehler bei der Ermittlung, als besser deaktivieren
                self.app.userSettings.setValue(false, forKey: UserSettings.KeyNames.useNotification)

            }

        }

    }


    //  es wurde ein Laufabschnitt ausgewählt
    private void pickerViewRunningUnitRowSelected(_ row: Int) {

        //  ausgewählte Trainingseinheit speichern
        //  wird für Monitoring des Trainings benötigt
        if self.runningPlanEntry != nil {

            let units = self.runningPlanEntry!.runningUnits

            if row < units.count {

                self.runningUnit = units[row]

            }

        }

    }

    // Button click event handling
    private void selectTrainingDayButtonClicked(View view) {

        //  neuer Tag - neuer Trainingsplan-Abschnitt
        //  aus Zeile der PickerView Woche und Tag ermitteln
        //  Tag ermitteln
        var day = (row + 1) % 7
        if day == 0 {

            //  Sonntag
            day = 7
        }

        //  Woche ermitteln
        //  Formel ((week - 1) * 7) + day umstellen
        let week =  ((row + 1 - day) / 7) + 1

        //  zu gewählter Woche und Tag den Trainingsabschnittes ermitteln und speichern
        if self.runningPlan != nil {

            var didUnitFound: Bool = false
            let entries = runningPlan!.entries
            for entry in entries {

                if entry.week == week && entry.day == day {

                    //  Trainingsabschnitt speichern
                    self.runningPlanEntry = entry

                    //  erste Trainingseinheit des Trainingsabschnitts speichern
                    if self.runningPlanEntry?.runningUnits.count ?? 0 > 0 {

                        self.runningUnit = self.runningPlanEntry!.runningUnits.first

                    }

                    //  Darstellung des Trainingsabschnittes in der View
                    self.showRunningPlanEntryInView()

                    //  PickerView kann wieder bedient werden
                    self.trainingUnitsPickerView.isUserInteractionEnabled = true

                    //  Flag setzen
                    didUnitFound = true
                    break

                }

            }

            //  evtl. eine Tag mit Trainingspause
            if !didUnitFound {

                //  kein aktiver Abschnitt (Pause)
                self.runningUnit = nil

                //  PickerView sperren
                self.trainingUnitsPickerView.isUserInteractionEnabled = false

                //  Anzeige der Trainingszeit löschen
                self.trainingDurationLabel.text = ""

                //  Status-Informationen
                self.runningInfoLabel.text = NSLocalizedString("Kein Training an diesem Tag.", comment: "Kein Training an diesem Tag.")

                //  Status-Bild anzeigen
                if let trainingStatusImage = UIImage(named: "trainingpaused30x30") {

                    //  Bild für den Status des Laufplanes
                    self.completedEntryImageView.image = trainingStatusImage

                }

            } else {

                //  Status-Informationen
                self.runningInfoLabel.text = NSLocalizedString("Training ist in Planung.", comment: "Training ist in Planung.")

                //  Status-Bild anzeigen
                if let trainingStatusImage = UIImage(named: "trainingplanned30x30") {

                    //  Bild für den Status des Laufplanes
                    self.completedEntryImageView.image = trainingStatusImage

                }

            }

        }

        //  "abhängige" PickerView der Laufeineiten aktualisieren
        self.trainingUnitsPickerView.reloadComponent(0)

    }

    private void startButtonClicked(View view) {

        //  Übereinstimmung aktuelles Datum mit Startdatum
        //  Nutzer-Hinweis, Startdatum in Abhängigkeit vom aktuellen Datum anpassen:
        //  Mo -> Mo, ab Di -> vorheriger Mo



        //  Timer starten / fortsetzen
        self.startTimer()
        //  Training starten / fortsetzen
        self.startTraining()

    }

    private void pauseButtonClicked(View view) {

        //  Timer pausieren
        self.pausedTimer()
        //  Training pausieren
        self.pauseTraining()

    }

    private void stopButtonClicked(View view) {


        //  Timer stoppen
        //  Timer läuft?
        if self.isTimerRunning {

            //  Nutzer fragen
            //  Hinweis an Nutzer
            let alert = UIAlertController(title: NSLocalizedString("Soll das Training wirklich beendet werden?",
                    comment: "Soll das Training wirklich beendet werden?"),
            message: "",
                    preferredStyle: .alert)

            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { (action: UIAlertAction!) in

                self.stopTimer()
                self.cancelTraining()
                return
            }))
                //  Abbrechen
                alert.addAction(UIAlertAction(title: NSLocalizedString("Abbrechen",
                        comment: "Aktion abbrechen. Cancel."),
                style: .cancel,
                        handler: { (action: UIAlertAction!) in
                return
            }))
            self.present(alert, animated: true, completion: nil)

        }


    }

    // initialize attributes
    private void setAttributeValues() {
        secondsInActivity = 0;
        secondsInPause = 0;
        // TODO: aus Global
        saveInterval = 15;
        isTimerRunning = false;
        isTrainingPaused = true;
        isTrainingRunning = false;
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
    /*

        //  Inhalt
        func pickerView(_ pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusing view: UIView?) -> UIView {

        let pickerLabel = UILabel()
        pickerLabel.textAlignment = .center

        //  Standardfarben
        pickerLabel.backgroundColor = Global.AppAppearance.iOSDefaultBackgroundColor
        pickerLabel.textColor = Global.AppAppearance.iOSDefaultTintColor

        //  Font
        pickerLabel.font = UIFont.systemFont(ofSize: 22.0, weight: UIFont.Weight.heavy)

        switch pickerView {

        case self.trainingDayPickerView:

        //  max Anzahl an Tagen
        //  TODO: tatsächlichen Tag (Datum) anzeigen
        //  TODO: um 90 Grad verdrehen, andere View?
        //  pickerLabel.transform = CGAffineTransform(rotationAngle: 90 * (.pi / 180 ))

        //  wenn ein Statrdatum gesetzt ist, dann den tatsächlichen Tag anzeigen ...
        if self.runningPlan!.startDate != nil {

        pickerLabel.text = self.dayAsStringForRow(row)

        } else {

        //  ansonsten nur Tag des Trainings als Zahl anzeigen
        pickerLabel.text = String(row + 1)

        }
        return pickerLabel

        case self.trainingUnitsPickerView:

        //  Trainingsabschnitte des jeweiligen Tages
        if self.runningPlanEntry != nil {

        let units = self.runningPlanEntry?.runningUnits
        if row < units?.count ?? 0 {

        //  Abschnitt darstellen
        let unit = units?[row]
        var labelString = ""

        let duration = unit?.duration ?? 0
        if duration < 60 {

        labelString = String(duration) + " min"

        } else {

        //  in h und min umrechnen
        let hours = (duration * 60) / 3600
        let minutes = (duration / 60) % 60
        labelString = String(hours) + " h und " + String(minutes) + " min"

        }

        labelString = labelString + " " + (unit?.movementType?.stringForKey ?? "")
        pickerLabel.text = labelString

        //  Abschnitt abgeschlossen?
        if unit?.completed ?? false {

        //  Status-Informationen
        self.runningInfoLabel.text = NSLocalizedString("Training wurde abgeschlossen.", comment: "Training wurde abgeschlossen.")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingcompleted30x30") {

        //  Bild für den Status des Laufplanes
        self.completedEntryImageView.image = trainingStatusImage

        }

        } else {

        //  Status-Informationen
        self.runningInfoLabel.text = NSLocalizedString("Training ist in Planung.", comment: "Training ist in Planung.")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingplanned30x30") {

        //  Bild für den Status des Laufplanes
        self.completedEntryImageView.image = trainingStatusImage

        }

        }

        }

        }

        //  Trainingspause am gewählten Tag
        if self.runningUnit == nil {

        pickerLabel.text = ""

        //  Status-Informationen
        self.runningInfoLabel.text = NSLocalizedString("Kein Training an diesem Tag.", comment: "Kein Training an diesem Tag.")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingpaused30x30") {

        //  Bild für den Status des Laufplanes
        self.completedEntryImageView.image = trainingStatusImage

        }

        }

        return pickerLabel

default:

        return pickerLabel

        }

        }

        //  Nutzer hat Eintrag in Auswahlbox gewählt
        func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {

        switch pickerView {

        //  Trainingstag geändert
        case self.trainingDayPickerView:

        self.pickerViewTrainingDayRowSelected(row)

        case self.trainingUnitsPickerView:

        self.pickerViewRunningUnitRowSelected(row)
*/
}