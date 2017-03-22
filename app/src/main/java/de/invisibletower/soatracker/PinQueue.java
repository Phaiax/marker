package de.invisibletower.soatracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by daniel on 21.03.17.
 */

public final class PinQueue {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private PinQueue() {}

    /* Inner class that defines the table contents */
    public static class PinEntry implements BaseColumns {
        public static final String TABLE_NAME = "pins";
        public static final String COLUMN_NAME_REQID = "reqid";
        public static final String COLUMN_NAME_DESCR = "description";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";
        public static final String COLUMN_NAME_ICON = "icon";
        public static final String COLUMN_NAME_TIMESTAMP = "time";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PinEntry.TABLE_NAME + " (" +
                    PinEntry._ID + " INTEGER PRIMARY KEY," +
                    PinEntry.COLUMN_NAME_REQID + " INTEGER," +
                    PinEntry.COLUMN_NAME_DESCR + " TEXT," +
                    PinEntry.COLUMN_NAME_LAT + " TEXT," +
                    PinEntry.COLUMN_NAME_LON + " TEXT," +
                    PinEntry.COLUMN_NAME_ICON + " TEXT," +
                    PinEntry.COLUMN_NAME_TIMESTAMP + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PinEntry.TABLE_NAME;

    public static class PinQueueDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PinQueue.db";

        public PinQueueDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }


        public long insert(PinInfo pin) {


            // Gets the data repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(PinEntry.COLUMN_NAME_REQID, pin.reqid);
            values.put(PinEntry.COLUMN_NAME_DESCR, pin.descr);
            values.put(PinEntry.COLUMN_NAME_LAT, pin.lat);
            values.put(PinEntry.COLUMN_NAME_LON, pin.lon);
            values.put(PinEntry.COLUMN_NAME_ICON, pin.icon);
            values.put(PinEntry.COLUMN_NAME_TIMESTAMP, String.valueOf(pin.ts));

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(PinEntry.TABLE_NAME, null, values);
            return newRowId;
        }

        public PinInfo getNext(int reqid) {

            SQLiteDatabase db = this.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    PinEntry._ID,
                    PinEntry.COLUMN_NAME_REQID,
                    PinEntry.COLUMN_NAME_DESCR,
                    PinEntry.COLUMN_NAME_LAT,
                    PinEntry.COLUMN_NAME_LON,
                    PinEntry.COLUMN_NAME_ICON,
                    PinEntry.COLUMN_NAME_TIMESTAMP
            };

            // Filter results WHERE "title" = 'My Title'
            String selection = PinEntry.COLUMN_NAME_REQID + " = ?";
            String[] selectionArgs = {String.valueOf(reqid)};

            // How you want the results sorted in the resulting Cursor
            String sortOrder = PinEntry.COLUMN_NAME_REQID + " ASC";

            Cursor cursor = db.query(
                    PinEntry.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );

            if (cursor.moveToNext()) {

                PinInfo ret = new PinInfo();
                ret.reqid = cursor.getInt(1);
                ret.descr = cursor.getString(2);
                ret.lat = cursor.getString(3);
                ret.lon = cursor.getString(4);
                ret.icon = cursor.getString(5);
                ret.ts = Long.valueOf(cursor.getString(6));
                cursor.close();
                return ret;
            }
            cursor.close();
            return null;
        }
    }
}

