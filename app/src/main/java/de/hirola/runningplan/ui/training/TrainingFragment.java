package de.hirola.runningplan.ui.training;

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
import de.hirola.sportslibrary.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TrainingFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // Button
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
    private boolean didAllRunningPlanesCompleted;
    /*
    //  Benachrichtigungen
    private let userNotificationCenter = UNUserNotificationCenter.current()

    //  GPS
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

    //  Trainingszeit
    //  Timer-Objekt
    private var timer: Timer?
    // Timer aktiv?
    private var isTimerRunning: Bool = false
    //  Trainingszeit in Sekunden
    private var secondsInActivity: Int = 0
    //  Pausezeit in Sekunden
    private var secondsInPause: Int = 0
    //  Dateipfad für Zwischenspeichern der Trainingsdauer
    private var durationSaveFilePath: URL?
    //  Intervall zum Schreiben in Sekunden
    private var saveInterval: Int = 15
    //  Zeit beim Starten des Trainings
    private var runningStartDate: Date?
    //  Zeit beim Starten der Pause
    private var activityStartPauseDate: Date?
    //  Pausen-Zeit berechnet?
    private var didPausedSecondsCalulated: Bool = true

    //  Training
    //  Training wurde gestartet?
    private var isTrainingRunning: Bool = false
    //  Training pausiert?
    private var isTrainingPaused: Bool = true
    //  Trainingsdauer-Aufzeichnung gefunden?
    private var didSavedDurationFound: Bool = false
    //  aktive Laufplan-Trainingseinheit
    private var runningUnit: RunningUnit?
    //  Fehler beim Speichern der abgeschlossenen Trainingseinheit
    private var didCompleteUpdateError: Bool = false
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load running plans
        viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        runningPlans = viewModel.getRunningPlans().getValue();
        // set user activated running plan
        setActiveRunningPlan();
        // check if a training can continued
        checkForSavedTraining();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View trainingView = inflater.inflate(R.layout.fragment_training, container, false);
        super.onCreate(savedInstanceState);
        // Label für den Zugriff initialisieren
        runningPlanNameLabel = trainingView.findViewById(R.id.editTextRunningPlanNameLabel);
        trainingDateLabel = trainingView.findViewById(R.id.editTextTrainingDateLabel);
        trainingInfolabel = trainingView.findViewById(R.id.editTextTrainingInfoLabel);
        // Button listener
        Button selectTrainingDayButton = trainingView.findViewById(R.id.buttonSelectTrainingDay);
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



    /*
    for entry in result {

            let runningPlan = entry as! RunningPlan

            //  abgeschlossene Laufpläne ausblenden
            if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.hideCompletedRunningPlanes) {

                if !runningPlan.completed {

                    self.runningPlanes.append(runningPlan)

                }

            } else {

                self.runningPlanes.append(runningPlan)

            }

        }

        //  Array nach Reihenfolge der Laufpläne sortieren
        self.runningPlanes.sort(by: { $0.orderNumber < $1.orderNumber })

     */

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
                // Trainingstag setzen und anzeigen
                int day = runningPlanEntry.getDay();
                int week = runningPlanEntry.getWeek();
                LocalDate startDate;
                if (runningPlan != null) {
                    // Startdatum ist im Laufplan gesetzt
                    startDate = runningPlan.getStartDate();
                } else {
                    startDate = LocalDate.now();
                }
                // Woche festlegen
                LocalDate trainingDate = startDate.plusWeeks(week - 1);
                // Tag festlegen, Start-Tag ist immer Montag!
                trainingDate = trainingDate.plusDays(day - 1);
            }

        }



                    }

                    //  aktive Trainingseinheit des Trainingsabschnittes setzen
                    //  abgeschlossene Einheiten überspringen
                    let units = self.runningPlanEntry!.runningUnits
                    if let index = units.firstIndex(where: {$0.completed == false}) {

                        //  Trainingsabschnitt als aktiv setzen
                        self.runningUnit = units[index]

                        //  in PickerView auswählen
                        if index < self.trainingUnitsPickerView.numberOfRows(inComponent: 0) {

                            self.trainingUnitsPickerView.selectRow(index, inComponent: 0, animated: false)

                        }

                        //  Dauer des gesamten Trainings anzeigen
                        self.showRunningPlanEntryInView()
    }

    private void showRunningPlanEntryInView() {

        if self.runningPlanEntry != nil {

            //  Gesamtdauer darstellen
            var durationString = NSLocalizedString("Gesamtdauer des Trainings ", comment: "Gesamtdauer des Trainings ")
            let duration = self.runningPlanEntry!.duration
            // Stunden oder Minuten?
            //  Gesamtdauer des Trainings (gespeichert in min)
            if duration < 60 {

                durationString.append(String(duration))
                durationString.append(" min")

            } else {

                //  in h und min umrechnen
                let hours = (duration * 60) / 3600
                let minutes = (duration / 60) % 60
                durationString.append(String(hours))
                durationString.append(" h und ")
                durationString.append(String(minutes))
                durationString.append(" min")

            }
            self.trainingDurationLabel.text = durationString

        }

    }

    private String dayAsStringForRow(_ row: Int) {

        let startDate = self.runningPlan!.startDate!
                let calendar = Calendar(identifier: .gregorian)

        //  Tag ist Start-Tag + Zeile
        var dateInterval = DateComponents()
        dateInterval.day = row

        if let day = calendar.date(byAdding: dateInterval, to: startDate) {

            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "dd.MM.yyyy"

            //  Wochentag-Name
            //  Tag ermitteln für Text aus Global-Array
            var dayOfWeek = (row + 1) % 7
            if dayOfWeek == 0 {

                //  Sonntag
                dayOfWeek = 7
            }
            var labelString: String = Global.daysOfWeeks[dayOfWeek]

            labelString.append(", ")
            //  Datum
            labelString.append(dateFormatter.string(from: day))

            return labelString

        }

        return String(row + 1)

    }

    private void locationSettings() {

        //  GPS-Einstellungen setzen
        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            //  MapKit-Ereignisse der Karte selbst verarbeiten
            self.locationManager.delegate = self

            if CLLocationManager.locationServicesEnabled() {

                //  aktuellen Standort anfordern
                self.locationManager.requestLocation()

            } else {

                locationManager.requestWhenInUseAuthorization()

            }

        } else {

            //  GPS-Einstellungen "löschen"
            self.locationManager.delegate = nil
            self.locationManager.stopMonitoringSignificantLocationChanges()

        }

    }

    //  Prüfung der Koordinaten
    private boolean coordinateValid(_ coordinate: CLLocationCoordinate2D) {

        let latitude = coordinate.latitude
        let longitude = coordinate.longitude

        if latitude >= Global.ValidLocationValues.latitudeMinValue && latitude <= Global.ValidLocationValues.latitudeMaxValue &&
                longitude >= Global.ValidLocationValues.longitudeMinValue && longitude <= Global.ValidLocationValues.longitudeMaxValue {

            return true

        }

        return false
    }

    //
    //  TRAINING
    //
    //  Training aufzeichnen
    private void startRecordingTraining() {

        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            if !self.isTrainingRunning {

                //  Aufzeichung und Montoring des Trainings neu starten
                //  Reset des (vorherigen) Tracks
                self.trackLocations.removeAll()

            }

            //  Ereignisverarbeitung (Delegate) starten, wenn neuer Standort verfügbar
            self.locationManager.startUpdatingLocation()
            self.locationManager.startMonitoringSignificantLocationChanges()

        }

    }

    //  Training aufzeichnen stoppen
    private void stopRecordingTraining() {

        if Global.AppSettings.useGPS && self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            //  GPS-Aufzeichnung anhalten
            self.locationManager.stopUpdatingLocation()
            self.locationManager.stopMonitoringSignificantLocationChanges()

            //  Pause-Zeit "merken"
            self.activityStartPauseDate = self.trackLocations.last?.timestamp ?? Date()
            self.didPausedSecondsCalulated = false

        }

    }

    //  Start des Trainings
    private void startTraining() {

        //  PickerView-Element dürfen nicht mehr bedient werden können
        self.trainingDayPickerView.isUserInteractionEnabled = false
        self.trainingUnitsPickerView.isUserInteractionEnabled = false

        if self.isTrainingRunning {

            //  Aufzeichnung (wieder) starten
            self.startRecordingTraining()
            self.runningInfoLabel.text = NSLocalizedString("Training wird fortgesetzt", comment: "Fortsetzung des Trainings, Aufzeichnen von Daten.")

        } else {

            //  Aufzeichung und Montoring des Trainings starten
            self.startRecordingTraining()
            self.isTrainingRunning = true
            self.runningInfoLabel.text = NSLocalizedString("Start des Trainings", comment: "Start des Trainings, Aufzeichnen von Daten.")

        }

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingactive30x30") {

            //  Bild für den Status des Laufplanes
            self.completedEntryImageView.image = trainingStatusImage

        }

    }

    //  Trainingspause
    private void pauseTraining() {

        //  GPS-Aufzeichnung stoppen
        self.stopRecordingTraining()

        //  Laufinfos aktualisieren
        self.runningInfoLabel.text = NSLocalizedString("Trainingspause", comment: "Trainingspause")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingpaused30x30") {

            //  Bild für den Status des Laufplanes
            self.completedEntryImageView.image = trainingStatusImage

        }

    }

    //  Trainingsabbruch
    private void cancelTraining() {

        //  Timer resetten
        self.resetTimer()

        //  Aufzeichnung GPS stoppen
        self.stopRecordingTraining()

        //  evtl. aufgezeichnete Daten löschen
        self.trackLocations.removeAll()

        //  Sicherungen löschen
        self.removeSavedTraining()

        //  Flag setzen
        self.isTrainingRunning = false

        //  Info-Label wieder auf "normale" Farben setzen
        self.setInfoLabelDefaultColors()

        //  PickerView-Elemente können wieder bedient werden
        self.trainingDayPickerView.isUserInteractionEnabled = true
        self.trainingUnitsPickerView.isUserInteractionEnabled = true

        self.runningInfoLabel.text = NSLocalizedString("Abbruch des Trainings", comment: "Abbruch des Trainings, kein Speichern von Daten.")

        //  Status-Informationen
        self.runningInfoLabel.text = NSLocalizedString("Training ist in Planung.", comment: "Training ist in Planung.")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingplanned30x30") {

            //  Bild für den Status des Laufplanes
            self.completedEntryImageView.image = trainingStatusImage

        }

    }

    //  Location-Daten aus Cache einlesen und
    //  Aufzeichnung fortsetzen
    private void continueTraining() {

        //  Datei wieder einlesen
        if self.didSavedTrackFound {

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

        //  Training als aktiv markieren
        self.isTrainingRunning = true

        //  Timer starten / fortsetzen
        self.startTimer()

        //  Training starten / fortsetzen
        self.startTraining()
    }

    //  Training "überwachen"
    private void monitoringTraining() {

        //  Status-Information
        self.runningInfoLabel.text = NSLocalizedString("Training läuft.", comment: "Training läuft.")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingactive30x30") {

            //  Bild für den Status des Laufplanes
            self.completedEntryImageView.image = trainingStatusImage

        }

        //  Monitoring der Trainingszeit
        //  TODO: Was ist bei GPS-Timer?
        //  Dauer einer Einheit beträgt mindestens 1 min
        if self.secondsInActivity >= 60 {

            let duration = self.runningUnit!.duration
            let minutes = round(Double(self.secondsInActivity / 60))
            if Int(minutes) >= duration {

                //  Trainingseinheit wurde abgeschlossen
                //  Benachrichtigung an Nutzer
                if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useNotification) {

                    self.sendUserNotification(cause: 0)

                }

                if self.runningPlanEntry != nil {

                    //  Status abgeschlossen für Abschnitt speichern
                    if self.runningUnit != nil {

                        do {

                            try self.app.datastore.update(onObject: self.runningUnit, {

                                    self.runningUnit!.completed = true

                            })

                        } catch (let error) {

                            //  zentrales Logging
                            if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useRemoteLogging) || Global.AppSettings.debug {

                                var errorMessage = LogEntry.debugInformations
                                errorMessage.append(" - ")
                                errorMessage.append(error.localizedDescription)
                                self.app.appLogger.error(errorMessage)

                            }

                            //  Fehler "merken"
                            self.didCompleteUpdateError = true

                        }

                    }

                    //  weitere Einheiten des Trainingsabschnittes verfügbar?
                    //  welche Trainingseinheit wurde vom Nutzer ausgewählt?
                    let trainingUnitIndex = self.trainingUnitsPickerView.selectedRow(inComponent: 0) + 1

                    if trainingUnitIndex < self.runningPlanEntry?.runningUnits.count ?? 0 {

                        //  nächsten Trainingsabschnitt setzen
                        self.runningUnit = self.runningPlanEntry!.runningUnits[trainingUnitIndex]

                        if trainingUnitIndex < self.trainingUnitsPickerView.numberOfRows(inComponent: 0) {

                            //  nächsten Trainingsabschnitt in PickerView auswählen
                            self.trainingUnitsPickerView.selectRow(trainingUnitIndex, inComponent: 0, animated: true)

                        }

                        //  Timer reseten
                        self.resetTimer()

                        //  Training geht weiter ...
                        //  Hinweis an Nutzer - Vibration / Ton?
                        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)

                        //  Info-Label aktualisieren
                        self.runningInfoLabel.text = NSLocalizedString("Neue Laufeinheit beginnt.", comment: "Neue Laufeinheit beginnt.")

                        //  Timer (wieder) starten
                        self.startTimer()

                    } else {

                        //  alle Einheiten des Abschnittes (Tages) abgeschlossen
                        //  Benachrichtigung an Nutzer
                        //  Vibration
                        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
                        //  Hinweise
                        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useNotification) {

                            self.sendUserNotification(cause: 1)

                        }

                        //  Info-Label aktualisieren
                        self.runningInfoLabel.text = NSLocalizedString("Training beendet.", comment: "Training beendet.")

                        //  Training abgeschlossen
                        self.completeTraining()

                    }

                }

            }

        }

    }

    //  Training wurde abgeschlossen
    private void completeTraining() {

        //  Timer stoppen
        self.stopTimer()

        //  Info-Label wieder auf "normale" Farben setzen
        self.setInfoLabelDefaultColors()

        //  Training wurde endgültig gestoppt (abgeschlossen)
        self.isTrainingRunning = false

        //  gespeicherte Trainingsdaten löschen
        self.removeSavedTraining()

        //  Infos ausgeben
        var labelString = NSLocalizedString("Training abgeschlossen.", comment: "Training abgeschlossen.")

        //  Status-Bild anzeigen
        if let trainingStatusImage = UIImage(named: "trainingcompleted30x30") {

            //  Bild für den Status des Laufplanes
            self.completedEntryImageView.image = trainingStatusImage

        }

        //  nur wenn GPS-Nutzung von Nutzer gewünscht wird
        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.useGPS) {

            //  Aufzeichnung stoppen
            self.stopRecordingTraining()

            //  Trainingsdaten anzeigen
            labelString.append("\n")
            labelString.append(NSLocalizedString("Länge: ", comment: "Länge: "))

            //  Länge der Strecke in m
            var distance = self.trackLocations.totalDistance

            if distance < 999.99 {

                //  Angabe in m
                labelString.append(String(format: "%.2f", distance))
                labelString.append(" m")
                labelString.append("\n")

            } else {

                //  Angabe in km
                distance = distance / 1000
                labelString.append(String(format: "%.2f", distance))
                labelString.append(" km")
                labelString.append("\n")

            }

            //  Geschwindigkeit in m/s
            var averageSpeed = self.trackLocations.averageSpeed
            //  Geschwindigkeit in km/h
            averageSpeed = averageSpeed * 3.6
            labelString.append(NSLocalizedString("Geschw.: ", comment: "durchschnittliche Geschwindigkeit: "))
            labelString.append(String(format: "%.2f", averageSpeed))
            labelString.append(" km/h")

            self.runningInfoLabel.text = labelString

        }

        //  Speichern der Trainingsdaten
        //  ohne GPS, dann ohne Track / Route, aber mit Dauer
        //  wenn vom Nutzer gewünscht
        if self.app.userSettings.getBool(forKey: UserSettings.KeyNames.saveTrainings) {

            //  Nutzer fragen, ob gespeichert werden soll
            let alert = UIAlertController(title: NSLocalizedString("Soll das Training gespeichert werden?",
                    comment: "Soll das Training gespeichert werden?"),
            message: "",
                    preferredStyle: .alert)

            alert.addAction(UIAlertAction(title: NSLocalizedString("Ja", comment: "Ja"),
            style: .default,
                handler: { (action: UIAlertAction!) in

                    //  sofort speichern
                    self.saveTraining()
                    return
                }))
                //  ansonsten nicht
                alert.addAction(UIAlertAction(title: NSLocalizedString("Nein", comment: "Nein"),
                style: .cancel,
                        handler: { (action: UIAlertAction!) in
                return
            }))
            self.present(alert, animated: true, completion: nil)

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