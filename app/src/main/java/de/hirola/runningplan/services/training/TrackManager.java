package de.hirola.runningplan.services.training;

import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.model.Track;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
 * @since 1.1.1
 */
public class TrackManager {

    private List<Track.Id> tracks; // query the list to avoid get from sqlite
    private final Context context;
    private final TrackingDatabaseHelper databaseHelper;

    public TrackManager(@NonNull Context context) {
        this.context = context;
        databaseHelper = new TrackingDatabaseHelper(context);
        tracks = databaseHelper.getRecordedTracks();
        if (tracks == null) {
            tracks = new ArrayList<>();
        }
    }

    @Nullable
    public Track.Id addNewTrack() {
        // create a new track and start recording
        // we needed name, description, start time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        String description = context.getString(R.string.running_unit_started_at) + " " + formatter.format(now);
        long startTime = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        // build a track with a start time
        Track track = new Track("Running unit", description, startTime);
        long primaryKey = databaseHelper.insertTrack(track);
        if (primaryKey > -1) {
            Track.Id trackId = new Track.Id(primaryKey);
            // remember the new track in local list
            tracks.add(trackId);
            return trackId;
        }
        return null;
    }

    public boolean insertLocationForTrack(@NonNull Track.Id trackId, @NonNull Location location) {
        if (tracks.contains(trackId)) {
            return databaseHelper.insertLocationForTrack(trackId, location);
        }
        return false;
    }

    public boolean completeTrack(Track.Id trackId) {
        if (tracks.contains(trackId)) {
            return databaseHelper.completeTrack(trackId);
        }
        return false;
    }

    public Track getTrack(Track.Id trackId) {
        if (tracks.contains(trackId)) {
            return databaseHelper.getTrack(trackId);
        }
        return null;
    }

    public boolean removeTrack(Track.Id trackId) {
        if (tracks.contains(trackId)) {
            return databaseHelper.removeTrack(trackId);
        }
        return false;
    }
}
