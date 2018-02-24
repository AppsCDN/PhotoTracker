package vitalypanov.phototracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import vitalypanov.phototracker.database.DbSchema.TracksTable;
import vitalypanov.phototracker.model.Track;

import static vitalypanov.phototracker.database.DbSchema.TracksTable.Cols;

/**
 * Singleton for working with Database
 * Created by Vitaly on 24.02.2018.
 */

public class TrackDbHelper {
    private static TrackDbHelper trackDbHelper;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static TrackDbHelper get(Context context) {
        if (trackDbHelper == null) {
            trackDbHelper = new TrackDbHelper(context);
        }
        return trackDbHelper;
    }

    private TrackDbHelper(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new DbSchemaHelper(mContext).getWritableDatabase();
    }

    /**
     * Pack Track object into ContenValues for futher writing into db
     * @param track
     * @return content values object
     */
    private static ContentValues getContentValues(Track track){
        ContentValues values = new ContentValues();
        values.put(Cols.UUID, track.getId().toString());
        values.put(Cols.START_TIME, track.getStartTime().getTime());
        values.put(Cols.END_TIME, track.getEndTime().getTime());
        values.put(Cols.COMMENT, track.getComment());
        // track data store in json format in one database field
        Gson gson = new Gson();
        values.put(Cols.TRACK_DATA, gson.toJson(track.getTrackData(),
                new TypeToken<ArrayList<Track>>() {}.getType()));
        return values;
    }

    /**
     * Insert new track into Db
     * @param track Track object
     */
    public void insertTrack(Track track){
        ContentValues values = getContentValues(track);
        mDatabase.insert(TracksTable.NAME, null, values);
    }

    /**
     * Update already exists in Db track
     * @param track Track object
     */
    public void updateTrack(Track track){
        String uuidString = track.getId().toString();
        ContentValues values = getContentValues(track);
        mDatabase.update(TracksTable.NAME, values,
                Cols.UUID + " =?",
                new String[]{uuidString});
    }
}