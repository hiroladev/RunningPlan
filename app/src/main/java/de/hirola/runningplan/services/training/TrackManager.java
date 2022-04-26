package de.hirola.runningplan.services.training;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.model.Track;
import org.tinylog.Logger;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager to handle the recorded track locations.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class TrackManager {

    public static final String LOCATION_UPDATE_ACTION
            = "RunningPlan_Track_Manager_Receiver_Location_Update_Action";
    public static final String TRACK_COMPLETED_ACTION
            = "RunningPlan_Track_Manager_Receiver_Track_Completed_Action";
    public static final String RECEIVER_INTENT_TRACK_DISTANCE = "distance";
    public static final String RECEIVER_INTENT_COMPLETE_STATE = "completeState";

    private final SportsLibrary sportsLibrary;
    private List<Track.Id> trackIds; // query the list to avoid get from sqlite
    private final Context context;
    private final TrackingDatabaseHelper databaseHelper;
    private TrackPoint trackPoint;
    private volatile boolean isRecording;
    private final DatabaseOperationThread databaseOperationThread; // handle database operation in an own thread
    private final BlockingQueue<TrackUpdateTask> databaseOperationsBlockingQueue; // FIFO-Queue for database operations

    public TrackManager(@NonNull Context context) {
        sportsLibrary = ((RunningPlanApplication) context.getApplicationContext()).getSportsLibrary();
        this.context = context;
        databaseHelper = new TrackingDatabaseHelper(context);
        trackIds = databaseHelper.getTrackIds();
        // thread for database operations
        isRecording = false;
        databaseOperationThread = new DatabaseOperationThread();
        databaseOperationsBlockingQueue = new LinkedBlockingQueue<>();

        if (trackIds == null) {
            trackIds = new ArrayList<>();
        }
    }

    @Nullable
    public Track.Id addNewTrack() {
        // create a new track and start recording
        // we needed name, description, start time
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        String name = context.getString(R.string.training_unit_track);
        String description = context.getString(R.string.running_unit_started_at) + " " + formatter.format(now);
        long startTime = Instant.now(Clock.system(ZoneId.systemDefault())).toEpochMilli();
        // build a track with a start time
        Track track = new Track(name, description, startTime);
        long primaryKey = databaseHelper.insertTrack(track);
        if (primaryKey > -1) {
            Track.Id trackId = new Track.Id(primaryKey);
            // remember the new track in local list
            trackIds.add(trackId);
            // set the recording state to true
            isRecording = true;
            return trackId;
        }
        return null;
    }

    public void insertLocationForTrack(@NonNull Track.Id trackId, @NonNull Location location) {
        if (trackIds.contains(trackId)) {
            if (databaseOperationThread.getState() == Thread.State.NEW) {
                // start the thread if not started
                databaseOperationThread.start();
            }

            // create helper object for thread operations
            TrackUpdateTask trackUpdateTask = new TrackUpdateTask();
            trackUpdateTask.trackId = trackId;

            // create the first point of the track with the start time of track
            if (trackPoint == null) {
                trackPoint = new TrackPoint(location);
                trackUpdateTask.trackPoint = trackPoint;
            } else {
                // validate the location data
                // during the first few seconds, no valid data was often supplied in the test
                if (validLocation(trackPoint.getLastLocation(), location)) {
                    // update the track in database in a thread
                    trackPoint.setActualLocation(location);
                    trackUpdateTask.trackPoint = trackPoint;
                    trackUpdateTask.task = TrackUpdateTask.UPDATE_TRACK_TASK;
                    databaseOperationsBlockingQueue.add(trackUpdateTask);
                }
            }
            // add location to database in a thread
            trackUpdateTask.task = TrackUpdateTask.ADD_LOCATION_TASK;
            databaseOperationsBlockingQueue.add(trackUpdateTask);
        }
    }

    public boolean completeTrack(Track.Id trackId) {
        if (trackIds.contains(trackId)) {
            // set recording state to false
            int index = trackIds.indexOf(trackId);
            if (index  > -1) {
                trackIds.get(index).setRecording(false);
            }
            // create helper object for thread operations
            TrackUpdateTask trackUpdateTask = new TrackUpdateTask();
            trackUpdateTask.task = TrackUpdateTask.COMPLETE_TRACK_TASK;
            trackUpdateTask.trackId = trackId;
            trackUpdateTask.trackPoint = trackPoint;
            // complete the track in database operation thread
            databaseOperationsBlockingQueue.add(trackUpdateTask);
            return true;
        }
        return false;
    }

    public Track getTrack(Track.Id trackId) {
        if (trackIds.contains(trackId)) {
            return databaseHelper.getTrack(trackId);
        }
        return null;
    }

    public List<Track> getRecordedTracks() {
        return databaseHelper.getRecordedTracks(trackIds);
    }

    public boolean removeTrack(Track.Id trackId) {
        if (trackIds.contains(trackId)) {
            // remove from local list
            if (trackIds.remove(trackId)) {
                // remove from database
                return databaseHelper.removeTrack(trackId);
            }
        }
        return false;
    }

    // @see https://stackoverflow.com/questions/37485887/how-to-get-accurate-current-location-in-android
    private boolean validLocation(Location lastLocation, Location actualLocation) {
        if (actualLocation == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = actualLocation.getElapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos();
        boolean isSignificantlyNewer = timeDelta > TrainingService.LOCATION_UPDATE_INTERVAL_IN_MILLI;
        boolean isSignificantlyOlder = timeDelta < -TrainingService.LOCATION_UPDATE_INTERVAL_IN_MILLI;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (actualLocation.getAccuracy() - lastLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = actualLocation.getProvider().equals(lastLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    public void end() {
        // set the recording states to false
        // thread
        isRecording = false;
        // recorded tracks
        for (Track.Id trackId: trackIds) {
            if (trackId.isRecording()) {
                trackId.setRecording(false);
            }
        }
        // reset the state in database
        databaseHelper.unsetRecordingStates();
    }

    /**
     * Remove all (inactive) tracks and locations from datastore.
     *
     * @return The number of deleted tracks.
     */
    public int clearAll() {
        // clear only inactive tracks
        List <Track.Id> tempList = new ArrayList<>();
        for (Track.Id trackId: trackIds) {
            if (trackId.isRecording()) {
                // backup track id to temp list
                tempList.add(trackId);
            }
        }
        // clear the previous list
        trackIds.clear();
        // add active tracks
        trackIds.addAll(tempList);
        return databaseHelper.clearAll();
    }

    // helper class for database updates with fifo queue
    private static class TrackUpdateTask {

        static final int UPDATE_TRACK_TASK = 0;
        static final int ADD_LOCATION_TASK = 1;
        static final int COMPLETE_TRACK_TASK = 2;

        Track.Id trackId;
        TrackPoint trackPoint;
        int task;

    }

    // thread to add track updates or location updates to database from a queue in fifo order
    // if queue is empty, the thread waits until a new element is in the queue
    private class DatabaseOperationThread extends Thread {

        public DatabaseOperationThread() {}

        public void run() {

            while (isRecording) {

                TrackUpdateTask trackUpdateTask;
                try {
                    // get and remove a track update from queue
                    // waits until the next update is available
                    trackUpdateTask = databaseOperationsBlockingQueue.take();
                } catch (InterruptedException exception) {
                    if (sportsLibrary.isDebugMode()) {
                        Logger.debug("Error while waiting for track update in queue.", exception);
                    }
                    return;
                }

                // update database
                switch (trackUpdateTask.task) {
                    case TrackUpdateTask.UPDATE_TRACK_TASK:
                        databaseHelper.updateTrack(trackUpdateTask.trackId, trackUpdateTask.trackPoint);
                        break;
                    case TrackUpdateTask.ADD_LOCATION_TASK:
                        // add the location to database and send actual track values to fragment
                        databaseHelper.insertLocationForTrack(trackUpdateTask.trackId, trackUpdateTask.trackPoint);
                        Intent locationUpdateIntent = new Intent(LOCATION_UPDATE_ACTION);
                        locationUpdateIntent.putExtra(RECEIVER_INTENT_TRACK_DISTANCE, trackPoint.getActualDistance());
                        context.sendBroadcast(locationUpdateIntent);
                        break;
                    case TrackUpdateTask.COMPLETE_TRACK_TASK:
                        // complete the track and send the result to the fragment
                        boolean isCompleted = databaseHelper.completeTrack(trackUpdateTask.trackId, trackUpdateTask.trackPoint);
                        Intent trackCompleteIntent = new Intent(TRACK_COMPLETED_ACTION);
                        trackCompleteIntent.putExtra(RECEIVER_INTENT_COMPLETE_STATE, isCompleted);
                        context.sendBroadcast(trackCompleteIntent);
                }
            }
        }
    }
}
