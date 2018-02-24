package vitalypanov.phototracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import vitalypanov.phototracker.database.TrackerDatabaseSchema.TracksTable;

/**
 * Created by Vitaly on 24.02.2018.
 */

public class TrackerDatabaseHelper extends SQLiteOpenHelper{
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "trackerDatabase.db";

    public TrackerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TracksTable.NAME + "(" +
            " id integer primary key autoincrement, " +
                TracksTable.Cols.START_TIME +", " +
                TracksTable.Cols.END_TIME +", " +
                TracksTable.Cols.COMMENT +", " +
                TracksTable.Cols.TRACK_DATA +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
