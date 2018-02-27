package vitalypanov.phototracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import vitalypanov.phototracker.database.DbSchema.TracksTable;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.model.TrackPhoto;

import static vitalypanov.phototracker.database.DbSchema.TracksTable.Cols;

/**
 * Singleton for working with Database
 * Created by Vitaly on 24.02.2018.
 */

public class TrackDbHelper {
    private static final String TAG = "TrackDbHelper";
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
        values.put(Cols.DISTANCE, track.getDistance());
        values.put(Cols.COMMENT, track.getComment());
        // track data store in json format in one database field
        Gson gson = new Gson();
        values.put(Cols.TRACK_DATA, gson.toJson(track.getTrackData(),
                new TypeToken<ArrayList<TrackLocation>>() {}.getType()));
        values.put(Cols.PHOTO_FILES, gson.toJson(track.getPhotoFiles(),
                new TypeToken<ArrayList<TrackPhoto>>() {}.getType()));
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

    /**
     * Delete track from Db
     * @param track Track object
     */
    public void deleteTrack(Track track){
        mDatabase.delete(TracksTable.NAME, TracksTable.Cols.UUID + " = ?",
                new String[]{track.getId().toString()});
    }

    private TrackCursorWrapper queryTracks(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(TracksTable.NAME,
                null,   // columns. null value is selecting all columns
                whereClause,
                whereArgs,
                null,   // group by
                null,   // having
                Cols.ID + " desc"    // order by
        );
        return new TrackCursorWrapper(cursor);
    }

    /**
     * Reading all tracks from db to list
     * @return
     */
    public List<Track> getTracks(){
        List<Track> tracks = new ArrayList<>();
        TrackCursorWrapper cursor =queryTracks(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                tracks.add(cursor.getTrack());
                cursor.moveToNext();
            }
        }catch (Exception ex) {
            Log.d(TAG, "getTracks: " +ex.toString());
        } finally {
            cursor.close();
        }
        return tracks;
    }

    /**
     * Get tracks count in db table
     * @return
     */
    public long getTracksCount(){
        return DatabaseUtils.queryNumEntries(mDatabase, TracksTable.NAME);
    }
}