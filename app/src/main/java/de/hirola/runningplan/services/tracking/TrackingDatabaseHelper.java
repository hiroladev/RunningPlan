package de.hirola.runningplan.services.tracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.model.Track;
import de.hirola.sportslibrary.tables.TrackColumns;
import de.hirola.sportslibrary.tables.TrackLocationColumns;

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

    // if you change the database schema, increment the database version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Tracks.db";

    private static final String SQL_DELETE_LOCATION_DATA =
            "DROP TABLE IF EXISTS " + TrackLocationColumns.TABLE_NAME;

    private static final String SQL_DELETE_TRACKS =
            "DROP TABLE IF EXISTS " + TrackLocationColumns.TABLE_NAME;

    public TrackingDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
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
    public long insertTrack(@NonNull Track track) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // map of values, where column names are the keys
        ContentValues values = new ContentValues();
        // track name
        values.put(TrackColumns.NAME, track.getName());
        // convert start time to milli
        long startTime =  track.getStartTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        values.put(TrackColumns.STARTTIME, startTime);
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
    public long insertLocationForTrack(@NonNull Track.Id trackId, @NonNull Location location) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // check if track existing
        // projection of columns use for query.
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
        long returnValue = 0L;
        if (cursor.getCount() != 1) {
            returnValue = -1;
        }
        cursor.close();
        if (returnValue == 0) {
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
            returnValue = sqLiteDatabase.insert(TrackLocationColumns.TABLE_NAME, null, values);
            sqLiteDatabase.close();
            return returnValue;
        }
        return returnValue;
    }

    public boolean completeTrack(@NonNull Track.Id trackId, long stopTime) {
        if (trackExist(trackId)) {
            return true;
        }
        return false;
    }

    @Nullable
    public Track getTrack(Track.Id trackId) {
        if (trackExist(trackId)) {
            // get the track with all location updates
            // first get the track data from tables
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
            long returnValue = 0L;
            if (cursor.getCount() != 1) {
                returnValue = -1;
            }
            cursor.close();
            return null;
        }
        return null;
    }

    @Nullable
    public List<Location> getLocationsForTrack(Track.Id trackId) {
        if (trackExist(trackId)) {

            return new ArrayList<>();
        }
        return null;
    }

    public long clearAll() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        try {
            sqLiteDatabase.execSQL(SQL_DELETE_LOCATION_DATA);
            sqLiteDatabase.execSQL(SQL_DELETE_TRACKS);
            return 0;
        } catch (SQLException exception) {
            if (Global.DEBUG) {
                //TODO: logging
                exception.printStackTrace();
            }
            return -1;
        } finally {
            sqLiteDatabase.close();
        }
    }

    private boolean trackExist(Track.Id trackId) {
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
        boolean returnValue = false;
        if (cursor.getCount() == 1) {
            returnValue = true;
        }
        cursor.close();
        return returnValue;
    }

}
