package de.hirola.runningplan.tracking;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.tables.LocationDataColumns;
import de.hirola.sportslibrary.tables.TrackColumns;


/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Database helper to create and upgrade sqlite database for tracks (locations).
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class LocationTrackingDatabaseHelper extends SQLiteOpenHelper {

    // if you change the database schema, increment the database version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Tracks.db";

    private static final String SQL_DELETE_LOCATION_DATA =
            "DROP TABLE IF EXISTS " + LocationDataColumns.TABLE_NAME;

    private static final String SQL_DELETE_TRACKS =
            "DROP TABLE IF EXISTS " + LocationDataColumns.TABLE_NAME;

    public LocationTrackingDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create the tables
        sqLiteDatabase.execSQL(LocationDataColumns.CREATE_TABLE);
        sqLiteDatabase.execSQL(LocationDataColumns.CREATE_TABLE_INDEX);
        sqLiteDatabase.execSQL(TrackColumns.CREATE_TABLE);
        sqLiteDatabase.execSQL(TrackColumns.CREATE_TABLE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // actual there are no upgrades possible
        sqLiteDatabase.execSQL(SQL_DELETE_LOCATION_DATA);
        sqLiteDatabase.execSQL(SQL_DELETE_TRACKS);
        onCreate(sqLiteDatabase);
    }
}
