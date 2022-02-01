package de.hirola.runningplan.services.tracking;

import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.model.Track;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager to handle the recorded track locations.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TrackManager {

    private List<Track.Id> tracks;
    private TrackingDatabaseHelper databaseHelper = null;

    public TrackManager(@NonNull Context context) {
        tracks = new ArrayList<>();
        databaseHelper = new TrackingDatabaseHelper(context);
        loadTracks();
    }

    @Nullable
    public Track.Id addTrack(@NonNull Track track) {
        long primaryKey = databaseHelper.insertTrack(track);
        if (primaryKey > -1) {
            Track.Id trackId = new Track.Id(primaryKey);
            tracks.add(trackId);
            return trackId;
        }
        return null;
    }

    public boolean insertLocationForTrack(@NonNull Track.Id trackId, @NonNull Location location) {
        return databaseHelper.insertLocationForTrack(trackId, location) > -1;
    }

    public void completeTrack(Track.Id trackId) {
        // set stop time
        long stopTime = LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        // calculate avg, speed
    }

    public Track getTrack(Track.Id trackId) {
        return null;
    }

    public void removeTrack(Track.Id trackId) {

    }

    private void loadTracks() {

    }
}
