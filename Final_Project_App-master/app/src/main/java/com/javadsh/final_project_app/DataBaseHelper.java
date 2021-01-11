package com.javadsh.final_project_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by javad Sh on 2/2/2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper
{
    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "BtDevices";

    // keyword
    private static final String Total = "total";
    // Table Names
    private static final String TABLE_DEVICES = "BtDevices";

    // Common column names
    private static final String KEY_ID = "_id";
    private static final String KEY_CREATED_AT = "created_at";

    // devices Table - column names
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_DEVICE_ID = "device_mac_id";
    private static final String KEY_DEVICE_STATUS= "device_status";
    // devices Create Statements
    private static final String CREATE_TABLE_DEVICES = "CREATE TABLE "
            + TABLE_DEVICES + "(" + KEY_ID + " INTEGER PRIMARY KEY autoincrement," + KEY_DEVICE_NAME
            + " TEXT," + KEY_DEVICE_STATUS + " INTEGER," +KEY_DEVICE_ID + " Text," + KEY_CREATED_AT
            + " DATETIME" + ")";

    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(CREATE_TABLE_DEVICES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        onCreate(sqLiteDatabase);
    }

    public void createDevices(BtDevices btDevices)
    {
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICES;
        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();

        //insert row
        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_NAME, btDevices.getName());
        values.put(KEY_DEVICE_STATUS, 0);
        values.put(KEY_DEVICE_ID, btDevices.getMac_id());
        values.put(KEY_CREATED_AT, getDateTime());

        db.insert(TABLE_DEVICES, null, values);

        db.close();
    }

    public void updateDevice(BtDevices btDevices)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_NAME, btDevices.getName());
        values.put(KEY_DEVICE_STATUS, btDevices.getStatus());
        values.put(KEY_DEVICE_ID, btDevices.getMac_id());
        values.put(KEY_CREATED_AT, getDateTime());


        // updating row
        db.update(TABLE_DEVICES, values, KEY_DEVICE_ID + " = ?",
                new String[] { String.valueOf(btDevices.getMac_id()) });
        db.close();


    }
    public void deleteFromDevices(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_DEVICES,KEY_ID+"=?",new String[]{Integer.toString(id)});

        db.close();
    }

    public void deleteFromDevices(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_DEVICES,KEY_DEVICE_NAME+"=?",new String[]{name});

        db.close();
    }

    public void deleteFromDevices(String name , String mac_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_DEVICES,KEY_DEVICE_ID+"=?",new String[]{mac_id});

        db.close();
    }

    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEVICES, null, null);
        db.close();
    }


    public Cursor fetchBtDevices()
    {
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICES;
        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);
        return mCursor;
    }


    public Cursor fetchByNameDevice(String inputText) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Db Helper", inputText);
        Cursor mCursor ;

        mCursor = db.query(true, TABLE_DEVICES, new String[] {KEY_ID,KEY_DEVICE_NAME,KEY_DEVICE_STATUS,KEY_DEVICE_ID,KEY_CREATED_AT},
                KEY_DEVICE_NAME + " like '%" + inputText + "%'", null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        db.close();
        return mCursor;
    }

    public Cursor fetchByMacId(String inputText) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Db Helper", inputText);
        Cursor mCursor;

        mCursor = db.query(true, TABLE_DEVICES, new String[] {KEY_ID,KEY_DEVICE_NAME,KEY_DEVICE_STATUS,KEY_DEVICE_ID,KEY_CREATED_AT},
                KEY_DEVICE_ID+ " like '%" + inputText + "%'", null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        db.close();
        return mCursor;
    }
    public int fetchStatusByMacId(String inputText) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Db Helper", inputText);
        Cursor mCursor ;

        mCursor = db.query(true, TABLE_DEVICES, new String[] {KEY_ID,KEY_DEVICE_NAME,KEY_DEVICE_STATUS,KEY_DEVICE_ID,KEY_CREATED_AT},
                KEY_DEVICE_ID+ " like '%" + inputText + "%'", null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        db.close();
        return mCursor.getInt(2);
    }

    /*
 * get datetime
*/
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
