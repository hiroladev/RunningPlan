package de.hirola.runningplan.services.training;

import android.location.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A helper class to calculate distance, speed and others for a recorded track
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
public class TrackPoint {

    private final long startTime; // start time of recorded track from gps
    private double actualDistance; // distance of all previous locations
    private Location location; // the actual location of recorded track

    public TrackPoint(@NotNull Location location) {
        this.location = location;
        // the track starts
        actualDistance = 0.0;
        // save the start time
        startTime = location.getTime();
    }

    public void setActualLocation(@NotNull Location location) {
        // calculate the distance
        double distance = this.location.distanceTo(location);
        actualDistance += distance;
        // save the last location
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
}
