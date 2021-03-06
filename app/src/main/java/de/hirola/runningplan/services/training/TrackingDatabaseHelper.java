package de.hirola.runningplan.services.training;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.model.LocationData;
import de.hirola.sportsapplications.model.Track;
import de.hirola.sportsapplications.tables.TrackColumns;
import de.hirola.sportsapplications.tables.TrackLocationColumns;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Database helper to create and upgrade sqlite database for tracks (locations).
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class TrackingDatabaseHelper extends SQLiteOpenHelper {

    // if you change the database schema, increment the database version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Tracks.sqlite";

    private static final String SQL_DROP_LOCATION_DATA_TABLE =
            "DROP TABLE IF EXISTS " + TrackLocationColumns.TABLE_NAME;

    private static final String SQL_DROP_TRACK_TABLE =
            "DROP TABLE IF EXISTS " + TrackLocationColumns.TABLE_NAME;

    private final SportsLibrary sportsLibrary; // app logging

    public TrackingDatabaseHelper(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sportsLibrary = ((RunningPlanApplication) context.getApplicationContext()).getSportsLibrary();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create the tables
        sqLiteDatabase.execSQL(TrackLocationColumns.CREATE_TABLE);
        sqLiteDatabase.execSQL(TrackLocationColumns.CREATE_TABLE_INDEX);
        sqLiteDatabase.execSQL(TrackColumns.CREATE_TABLE);
        sqLiteDatabase.execSQL(TrackColumns.CREATE_TABLE_INDEX);
    }

    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        super.onOpen(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // actual there are no upgrades possible
        sqLiteDatabase.execSQL(SQL_DROP_LOCATION_DATA_TABLE);
        sqLiteDatabase.execSQL(SQL_DROP_TRACK_TABLE);
        onCreate(sqLiteDatabase);
    }

    /**
     * Insert a new track in the local track datastore.
     *
     * @param track to add new to the local datastore
     * @return the primary key for the inserted track or -1 if an error occurred
     */
    public long insertTrack(Track track) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // map of values, where column names are the keys
        ContentValues values = new ContentValues();
        // track name
        values.put(TrackColumns.NAME, track.getName());
        // track description
        values.put(TrackColumns.DESCRIPTION, track.getDescription());
        // start time
        values.put(TrackColumns.STARTTIME, track.getStartTimeInMilli());
        // flag as recording, 1 is true
        values.put(TrackColumns.ACTIVE, 1);
        // insert the new track (row), returning the primary key value of the new row
        long primaryKey = sqLiteDatabase.insert(TrackColumns.TABLE_NAME, null, values);
        sqLiteDatabase.close();
        return primaryKey;
    }

    /**
     * Update an existing track from the local track datastore.
     *
     * @param trackId of the track to be updated
     * @param trackPoint with updates for the track
     */
    public void updateTrack(Track.Id trackId, TrackPoint trackPoint) {
        if (trackExist(trackId)) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            // map of values, where column names are the keys
            ContentValues values = new ContentValues();
            // distance
            values.put(TrackColumns.DISTANCE, trackPoint.getActualDistance());
            // update the track, returning the primary key value of the new row
            long updatedRows = sqLiteDatabase.update(TrackColumns.TABLE_NAME,
                    values,
                    "rowid = ?",
                    new String[]{String.valueOf(trackId.getId())});
            sqLiteDatabase.close();
            if (updatedRows > 1) {
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug("More as one rows are updated.");
                }
            }
        }
    }

    /**
     * Add a location (update) to an existing track.
     *
     * @param trackId id for the track, where the location to be added
     * @param trackPoint the point of track to be added
     */
    public void insertLocationForTrack(Track.Id trackId, @NonNull TrackPoint trackPoint) {
        if (trackExist(trackId)) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            // insert the location
            Location location = trackPoint.getLastLocation();
            // map of values, where column names are the keys
            ContentValues values = new ContentValues();
            // track id
            values.put(TrackLocationColumns.TRACK_ID, trackId.getId());
            // system timestamp
            values.put(TrackLocationColumns.TIME_STAMP, trackPoint.getLastLocationTimestamp());
            // latitude
            values.put(TrackLocationColumns.LATITUDE, location.getLatitude());
            // longitude
            values.put(TrackLocationColumns.LONGITUDE, location.getLongitude());
            // speed
            values.put(TrackLocationColumns.SPEED, location.getSpeed());
            // altitude
            values.put(TrackLocationColumns.ALTITUDE, location.getAltitude());
            // insert the new location (row), returning the primary key value of the new location
            sqLiteDatabase.insert(TrackLocationColumns.TABLE_NAME, null, values);sqLiteDatabase.close();
        }
    }

    public boolean completeTrack(@NonNull Track.Id trackId, @Nullable TrackPoint trackPoint) {
        if (!trackExist(trackId) || trackPoint == null) {
            return false;
        }
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // TODO: stop time
        long stopTime = trackPoint.getStopTime();
        double distance = trackPoint.getActualDistance();
        // update the track
        ContentValues values = new ContentValues();
        values.put(TrackColumns.STOPTIME, stopTime);
        //values.put(TrackColumns.DURATION, duration);
        values.put(TrackColumns.DISTANCE, distance);
        // no active recording, o is false
        values.put(TrackColumns.ACTIVE, 0);
        // Which row to update, based on the title
        String selection = TrackColumns.ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(trackId.getId()) };
        int returnValue = sqLiteDatabase.update(
                TrackColumns.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        sqLiteDatabase.close();
        return returnValue == 1;
    }

    @Nullable
    public Track getTrack(Track.Id trackId) {
        if (trackExist(trackId)) {
            // get the track with all location updates
            List<LocationData> locations = getLocationsForTrack(trackId);
            // get the track data from tables
            // WHERE clause "title" = 'My Title'
            // result as cursor
            String selection = TrackColumns.ID + " = ?";
            String[] selectionArgs = {String.valueOf(trackId.getId())};
            try (SQLiteDatabase sqLiteDatabase = getReadableDatabase(); Cursor cursor = sqLiteDatabase.query(
                    TrackColumns.TABLE_NAME,   // table to query
                    null,                      // pass null to get all
                    selection,                 // columns for the WHERE clause
                    selectionArgs,             // values for the WHERE clause
                    null,                      // group by
                    null,                      // having
                    null                       // sort order
            )) {
                if (cursor.getCount() == 1) {
                    // cursor is position at index -1 initially
                    cursor.moveToNext();
                    // get the data (attributes) from cursor
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(TrackColumns.NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(TrackColumns.DESCRIPTION));
                    long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.STARTTIME));
                    long stopTime = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.STOPTIME));
                    double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(TrackColumns.DISTANCE));
                    // create a new track object
                    return new Track(name, description, startTime, stopTime, distance, locations);
                }
                return null;
            } catch (IllegalArgumentException exception) {
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug("Column (index) does not exists", exception);
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Remove a track and his locations from datastore.
     *
     * @param trackId for the track to be removed
     */
    public boolean removeTrack(Track.Id trackId) {
        if (trackExist(trackId)) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            // first delete all locations for the track
            // Define 'where' part of query.
            String trackLocationsSelection = TrackLocationColumns.TRACK_ID + " LIKE ?";
            // Specify arguments in placeholder order.
            String[] trackLocationsSelectionArgs = { String.valueOf(trackId.getId()) };
            // Issue SQL statement.
            int deletedTrackLocationRows = sqLiteDatabase.delete(TrackLocationColumns.TABLE_NAME, trackLocationsSelection, trackLocationsSelectionArgs);
            if (sportsLibrary.isDebugMode()) {
                String logMessage = "There have been " + deletedTrackLocationRows + " location rows deleted";
                Logger.debug(logMessage);
            }
            // now delete the track
            // Define 'where' part of query.
            String  trackSelection = TrackColumns.ID + " LIKE ?";
            // Specify arguments in placeholder order.
            String[] trackSelectionArgs = { String.valueOf(trackId.getId()) };
            // Issue SQL statement.
            int deletedTrackRows = sqLiteDatabase.delete(TrackLocationColumns.TABLE_NAME, trackSelection, trackSelectionArgs);
            if (sportsLibrary.isDebugMode()) {
                String logMessage = "There have been " +  deletedTrackRows + " track rows deleted";
                Logger.debug(logMessage);
            }
            sqLiteDatabase.close();
            return true;
        }
        return false;
    }

    /**
     * Returns the id's for all recorded (and saved) tracks with the recording status.
     *
     * @return a list of id's of recorded tracks and the flags
     *         or null if there are no tracks
     */
    @Nullable
    public List<Track.Id> getTrackIds() {
        // get the id and the flag of tracks
        String[] projection = {
                TrackColumns.ID,
                TrackColumns.ACTIVE
        };
        // build a list of locations for the track
        try (SQLiteDatabase sqLiteDatabase = getReadableDatabase(); Cursor cursor = sqLiteDatabase.query(
                TrackColumns.TABLE_NAME,   // table to query
                projection,                // columns to return (pass null to get all)
                null,                      // columns for the WHERE clause
                null,                      // values for the WHERE clause
                null,                      // group by
                null,                      // having
                null                       // sort order
        )) {
            List<Track.Id> trackIds = new ArrayList<>();
            while (cursor.moveToNext()) {
                // get the data (attributes) from cursor
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.ID));
                long flag = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.ACTIVE));
                // create a new track id object
                Track.Id trackId = new Track.Id(id);
                // determine and set the flag
                boolean active = flag !=0;
                trackId.setRecording(active);
                // add to list
                trackIds.add(trackId);
            }
            return trackIds;
        } catch (IllegalArgumentException exception) {
            if (sportsLibrary.isDebugMode()) {
                Logger.debug("Column (index) does not exists", exception);
            }
            return null;
        }
    }

    /**
     * Returns the id's for all recorded (and saved) tracks.
     *
     * @return a list of id's of recorded tracks or null if there are no tracks
     */
    @Nullable
    public List<Track> getRecordedTracks(@NonNull List<Track.Id> trackIds) {
        List<Track> tracks = new ArrayList<>();
        Iterator<Track.Id> iterator = trackIds.stream().iterator();
        while (iterator.hasNext()) {
            Track.Id trackId = iterator.next();
            tracks.add(getTrack(trackId));
        }
        return tracks;
    }

    /**
     * Set all state of recorded tracks to inactive.
     */
    public void unsetRecordingStates() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // update all "active" tracks
        String whereClauseTracks = TrackColumns.ACTIVE + " = ?";
        String[] whereClauseTracksArgs = { "1" };
        ContentValues values = new ContentValues();
        values.put(TrackColumns.ACTIVE, 0);
        long updatedTracks = sqLiteDatabase.update(TrackColumns.TABLE_NAME, values,
                whereClauseTracks, whereClauseTracksArgs);
        sqLiteDatabase.close();
        if (sportsLibrary.isDebugMode()) {
            String message = String.format("%s track states was set to false.", updatedTracks);
            Logger.debug(message);
        }
    }

    /**
     * Remove all entries from datastore.
     *
     * @return The number of deleted tracks.
     */
    public int clearAll() {
        // hint: to remove all rows and get a count pass "1" as the whereClause
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // delete no active tracks
        String whereClauseTracks = TrackColumns.ACTIVE + " = ?";
        String[] whereClauseTracksArgs = { "0" };
        int deletedTracks = sqLiteDatabase.delete(TrackColumns.TABLE_NAME,whereClauseTracks, whereClauseTracksArgs);
        // delete all locations where no track exists now
        String whereClauseLocations = TrackLocationColumns.TRACK_ID
                + " NOT IN ( SELECT "
                + TrackColumns.ID
                + " FROM "
                + TrackColumns.TABLE_NAME
                + ")";
        int deletedLocations = sqLiteDatabase.delete(TrackLocationColumns.TABLE_NAME,whereClauseLocations, null);
        sqLiteDatabase.close();
        if (sportsLibrary.isDebugMode()) {
            String message = String.format("%s locations and %s tracks deleted.",
                    deletedLocations, deletedTracks);
            Logger.debug(message);
        }
        sqLiteDatabase.close();
        return deletedTracks;
    }

    // private helper method to determine if a track exists
    private boolean trackExist(Track.Id trackId) {
        if (trackId == null) {
            return false;
        }
        // try to get the track id
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String[] projection = {
                TrackColumns.ID
        };
        // WHERE clause "title" = 'My Title'
        String selection = TrackColumns.ID + " = ?";
        String[] selectionArgs = {String.valueOf(trackId.getId())};
        // result as cursor
        Cursor cursor = sqLiteDatabase.query(
                TrackColumns.TABLE_NAME,   // table to query
                projection,                // columns to return (pass null to get all)
                selection,                 // columns for the WHERE clause
                selectionArgs,             // values for the WHERE clause
                null,                      // group by
                null,                      // having
                null                       // sort order
        );
        boolean returnValue = cursor.getCount() == 1;
        sqLiteDatabase.close();
        cursor.close();
        return returnValue;
    }

    // private helper method for building a track with his locations
    // list can be empty
    @Nullable
    private List<LocationData> getLocationsForTrack(Track.Id trackId) {
        if (trackExist(trackId)) {
            // get all locations for the track
            // get the data
            // WHERE clause
            // results sorted by timestamp
            // build a list of locations for the track
            String selection = TrackLocationColumns.TRACK_ID + " = ?";
            String[] selectionArgs = {String.valueOf(trackId.getId())};
            String sortOrder =
                    TrackLocationColumns.TIME_STAMP + " DESC";
            String[] projection = {
                    TrackLocationColumns.TIME_STAMP,
                    TrackLocationColumns.PROVIDER,
                    TrackLocationColumns.LATITUDE,
                    TrackLocationColumns.LONGITUDE,
                    TrackLocationColumns.ALTITUDE,
                    TrackLocationColumns.SPEED
            };
            try (SQLiteDatabase sqLiteDatabase = getReadableDatabase(); Cursor cursor = sqLiteDatabase.query(
                    TrackLocationColumns.TABLE_NAME,   // table to query
                    projection,                // columns to return (pass null to get all)
                    selection,                 // columns for the WHERE clause
                    selectionArgs,             // values for the WHERE clause
                    null,                      // group by
                    null,                      // having
                    sortOrder                  // sort order
            )) {
                List<LocationData> locations = new ArrayList<>();
                while (cursor.moveToNext()) {
                    // get the data (attributes) from cursor
                    long timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(TrackLocationColumns.TIME_STAMP));
                    String provider = cursor.getString(cursor.getColumnIndexOrThrow(TrackLocationColumns.PROVIDER));
                    float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(TrackLocationColumns.LATITUDE));
                    float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(TrackLocationColumns.LONGITUDE));
                    float altitude = cursor.getFloat(cursor.getColumnIndexOrThrow(TrackLocationColumns.ALTITUDE));
                    float speed = cursor.getFloat(cursor.getColumnIndexOrThrow(TrackLocationColumns.SPEED));
                    // create a new location data object
                    LocationData locationData = new LocationData(timeStamp, provider, latitude, longitude, altitude, speed);
                    // add location to list
                    locations.add(locationData);
                }
                return locations;
            } catch (IllegalArgumentException exception) {
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug("Column (index) does not exists", exception);
                }
                return null;
            }
        }
        return null;
    }

}
