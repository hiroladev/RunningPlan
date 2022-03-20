package de.hirola.runningplan.services.training;

import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.model.Track;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager to handle the recorded track locations.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class TrackManager {

    private List<Track.Id> trackIds; // query the list to avoid get from sqlite
    private final Context context;
    private final TrackingDatabaseHelper databaseHelper;
    private TrackPoint trackPoint;

    public TrackManager(@NonNull Context context) {
        this.context = context;
        databaseHelper = new TrackingDatabaseHelper(context);
        trackIds = databaseHelper.getTrackIds();
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
            // set the recording state to true
            trackIds.add(trackId);
            return trackId;
        }
        return null;
    }

    public boolean insertLocationForTrack(@NonNull Track.Id trackId, @NonNull Location location) {
        if (trackIds.contains(trackId)) {
            // create the first point of the track with the start time of track
            if (trackPoint == null) {
                trackPoint = new TrackPoint(location);
            } else {
                // update the point of the track with the start time of track
                trackPoint.setActualLocation(location);
                databaseHelper.updateTrack(trackId, trackPoint);
            }
            return databaseHelper.insertLocationForTrack(trackId, trackPoint);
        }
        return false;
    }

    public boolean completeTrack(Track.Id trackId) {
        if (trackIds.contains(trackId)) {
            // set recording state to false
            int index = trackIds.indexOf(trackId);
            if (index  > -1) {
                trackIds.get(index).setRecording(false);
            }
            return databaseHelper.completeTrack(trackId, trackPoint);
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

    public void clearRecordingStates() {
        // set the recording state to false
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
}
