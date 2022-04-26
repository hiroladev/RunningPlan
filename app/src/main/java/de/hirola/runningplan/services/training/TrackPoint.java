package de.hirola.runningplan.services.training;

import android.location.Location;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A helper class to calculate distance, speed and others for a recorded track.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class TrackPoint {

    private final long startTime; // start time of recorded track
    private long lastLocationTimestamp; // the system timestamp for the actual location
    private double actualDistance; // distance of all previous locations
    private Location lastLocation; // the actual location of recorded track

    public TrackPoint(@NotNull Location location) {
        this.lastLocation = location;
        // the track starts
        actualDistance = 0.0;
        // add the start time
        startTime = Instant.now(Clock.system(ZoneId.systemDefault())).toEpochMilli();
        lastLocationTimestamp = startTime;
    }

    public void setActualLocation(@NotNull Location actualLocation) {
            // use the timestamp from system
            lastLocationTimestamp = Instant.now(Clock.system(ZoneId.systemDefault())).toEpochMilli();
            // calculate the distance
            double distance = actualLocation.distanceTo(lastLocation);
            actualDistance += distance;
            // save the last location
            this.lastLocation = actualLocation;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastLocationTimestamp() {
        return lastLocationTimestamp;
    }

    public double getActualDistance() {
        return actualDistance;
    }

    public long getStopTime() {
        return Instant.now(Clock.system(ZoneId.systemDefault())).toEpochMilli();
    }
}
