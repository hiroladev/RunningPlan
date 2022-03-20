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
 * @since 0.1
 */
public class TrackPoint {

    private final static float MIN_DISTANCE_SPEED = 0.55f; // min speed for calculating distance

    private final long startTime; // start time of recorded track
    private double actualDistance; // distance of all previous locations
    private Location location; // the actual location of recorded track

    public TrackPoint(@NotNull Location location) {
        this.location = location;
        // the track starts
        actualDistance = 0.0;
        // add the start time
        startTime = Instant.now(Clock.system(ZoneId.systemDefault())).toEpochMilli();
    }

    public void setActualLocation(@NotNull Location location) {
        // calculate the distance
        if (location.hasSpeed() && location.getSpeed() > MIN_DISTANCE_SPEED) {
            double distance = this.location.distanceTo(location);
            actualDistance += distance;
        }
        // add the last location
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public long getStartTime() {
        return startTime;
    }

    public double getActualDistance() {
        return actualDistance;
    }

    public long getStopTime() {
        return Instant.now(Clock.system(ZoneId.systemDefault())).toEpochMilli();
    }
}
