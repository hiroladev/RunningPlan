package de.hirola.runningplan.services.training;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.sportslibrary.model.LocationData;
import de.hirola.sportslibrary.model.Track;
import de.hirola.sportslibrary.tables.TrackColumns;
import de.hirola.sportslibrary.tables.TrackLocationColumns;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Database helper to create and upgrade sqlite database for tracks (locations).
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TrackingDatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = TrackingDatabaseHelper.class.getSimpleName();

    // if you change the database schema, increment the database version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Tracks.sqlite";

    private static final String SQL_DELETE_LOCATION_DATA =
            "DROP TABLE IF EXISTS " + TrackLocationColumns.TABLE_NAME;

    private static final String SQL_DELETE_TRACKS =
            "DROP TABLE IF EXISTS " + TrackLocationColumns.TABLE_NAME;

    private final AppLogManager logManager; // app logging

    public TrackingDatabaseHelper(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        logManager = AppLogManager.getInstance(context);
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
        sqLiteDatabase.execSQL(SQL_DELETE_LOCATION_DATA);
        sqLiteDatabase.execSQL(SQL_DELETE_TRACKS);
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
        values.put(TrackColumns.STARTTIME, track.getStartTime());
        // insert the new track (row), returning the primary key value of the new row
        long primaryKey = sqLiteDatabase.insert(TrackColumns.TABLE_NAME, null, values);
        sqLiteDatabase.close();
        return primaryKey;
    }

    /**
     * Add a location (update) to an existing track.
     *
     * @param trackId id for the track, where the location to be added
     * @param location the location to be added
     * @return 0 if the location is added or -1 if an error occurred or the track does not exist
     */
    public boolean insertLocationForTrack(Track.Id trackId, @NonNull Location location) {
        if (trackExist(trackId)) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            // insert the location
            // map of values, where column names are the keys
            ContentValues values = new ContentValues();
            // track id
            values.put(TrackLocationColumns.TRACK_ID, trackId.getId());
            // timestamp, UTC
            values.put(TrackLocationColumns.TIME_STAMP, location.getTime());
            // latitude
            values.put(TrackLocationColumns.LATITUDE, location.getLatitude());
            // longitude
            values.put(TrackLocationColumns.LONGITUDE, location.getLongitude());
            // speed
            values.put(TrackLocationColumns.SPEED, location.getSpeed());
            // altitude
            values.put(TrackLocationColumns.ALTITUDE, location.getAltitude());
            // insert the new track (row), returning the primary key value of the new
            long numberOfInsertedRows = sqLiteDatabase.insert(TrackLocationColumns.TABLE_NAME, null, values);
            sqLiteDatabase.close();
            return numberOfInsertedRows > 0;
        }
        return false;
    }

    public boolean completeTrack(@NonNull Track.Id trackId) {
        if (trackExist(trackId)) {
            return true;
        }
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // calculate the values
        // set stop time
        long stopTime = LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        // avg, speed
        double speed = calculateAvgForTrack(trackId);
        double distance = calculateDistanceForTrack(trackId);
        // update the track
        ContentValues values = new ContentValues();
        values.put(TrackColumns.STOPTIME, stopTime);
        values.put(TrackColumns.AVGSPEED, speed);
        values.put(TrackColumns.DISTANCE, distance);
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
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            // WHERE clause "title" = 'My Title'
            String selection = TrackColumns.ID + " = ?";
            String[] selectionArgs = {String.valueOf(trackId.getId())};
            // result as cursor
            try (Cursor cursor = sqLiteDatabase.query(
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
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(TrackColumns.NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(TrackColumns.DESCRIPTION));
                    long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.STARTTIME));
                    long stopTime = cursor.getLong(cursor.getColumnIndexOrThrow(TrackColumns.STOPTIME));
                    float speed = cursor.getFloat(cursor.getColumnIndexOrThrow(TrackColumns.AVGSPEED));
                    float distance = cursor.getFloat(cursor.getColumnIndexOrThrow(TrackColumns.DISTANCE));
                    // create a new track object
                    return new Track(name, description, startTime, stopTime, locations);
                }
                return null;
            } catch (IllegalArgumentException exception) {
                if (logManager.isDebugMode()) {
                    logManager.log(exception,"Column (index) does not exists",TAG);
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
            if (logManager.isDebugMode()) {
                String logMessage = "There have been " + deletedTrackLocationRows + " location rows deleted";
                logManager.log(null, logMessage, TAG);
            }
            // now delete the track
            // Define 'where' part of query.
            String  trackSelection = TrackColumns.ID + " LIKE ?";
            // Specify arguments in placeholder order.
            String[] trackSelectionArgs = { String.valueOf(trackId.getId()) };
            // Issue SQL statement.
            int deletedTrackRows = sqLiteDatabase.delete(TrackLocationColumns.TABLE_NAME, trackSelection, trackSelectionArgs);
            if (logManager.isDebugMode()) {
                String logMessage = "There have been " +  deletedTrackRows + " track rows deleted";
                logManager.log(null, logMessage, TAG);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the id's for all recorded (and saved) tracks.
     *
     * @return a list of id's of recorded tracks or null if there are no tracks
     */
    @Nullable
    public List<Track.Id> getRecordedTracks() {
        // get all locations for the track
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        // get the data
        String[] projection = {
                TrackColumns.ID
        };
        // build a list of locations for the track
        try (Cursor cursor = sqLiteDatabase.query(
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
                // create a new track id object
                Track.Id trackId = new Track.Id(id);
                // add location to list
                trackIds.add(trackId);
            }
            return trackIds;
        } catch (IllegalArgumentException exception) {
            if (logManager.isDebugMode()) {
                logManager.log(exception,"Column (index) does not exists",TAG);
            }
            return null;
        }
    }

    /**
     * Remove all entries from datastore.
     */
    public void clearAll() {
        try (SQLiteDatabase sqLiteDatabase = getWritableDatabase()) {
            sqLiteDatabase.execSQL(SQL_DELETE_LOCATION_DATA);
            sqLiteDatabase.execSQL(SQL_DELETE_TRACKS);
        } catch (SQLException exception) {
            if (logManager.isDebugMode()) {
                logManager.log(exception,null,TAG);
            }
        }
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
        cursor.close();
        return returnValue;
    }

    // private helper method for building a track with his locations
    // list can be empty
    @Nullable
    private List<LocationData> getLocationsForTrack(Track.Id trackId) {
        if (trackExist(trackId)) {
            // get all locations for the track
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            // get the data
            String[] projection = {
                    TrackLocationColumns.TIME_STAMP,
                    TrackLocationColumns.PROVIDER,
                    TrackLocationColumns.LATITUDE,
                    TrackLocationColumns.LONGITUDE,
                    TrackLocationColumns.ALTITUDE,
                    TrackLocationColumns.SPEED
            };
            // WHERE clause
            String selection = TrackLocationColumns.TRACK_ID + " = ?";
            String[] selectionArgs = {String.valueOf(trackId.getId())};
            // results sorted by timestamp
            String sortOrder =
                    TrackLocationColumns.TIME_STAMP + " DESC";
            // build a list of locations for the track
            try (Cursor cursor = sqLiteDatabase.query(
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
                if (logManager.isDebugMode()) {
                    logManager.log(exception,"Column (index) does not exists",TAG);
                }
                return null;
            }
        }
        return null;
    }

    private double calculateAvgForTrack(Track.Id trackId) {

        return 0L;
    }

    private double calculateDistanceForTrack(Track.Id trackId) {

        return 0L;
    }

}
