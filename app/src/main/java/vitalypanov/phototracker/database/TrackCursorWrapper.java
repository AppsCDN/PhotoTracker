package vitalypanov.phototracker.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import vitalypanov.phototracker.database.DbSchema.TracksTable;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.model.TrackPhoto;

/**
 * Created by Vitaly on 25.02.2018.
 */

public class TrackCursorWrapper extends CursorWrapper {
    public TrackCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    /**
     * Parsing db cursor, creatting Track object
     * @return Track object
     */
    public Track getTrack(){
        Track track = new Track(UUID.fromString(getString(getColumnIndex(TracksTable.Cols.UUID))));
        track.setStartTime(new Date(getLong(getColumnIndex(TracksTable.Cols.START_TIME))));
        track.setEndTime(new Date(getLong(getColumnIndex(TracksTable.Cols.END_TIME))));
        track.setDistance(getDouble(getColumnIndex(TracksTable.Cols.DISTANCE)));
        track.setComment(getString(getColumnIndex(TracksTable.Cols.COMMENT)));
        track.setTrackData(parseTrackDataJSON(getString(getColumnIndex(TracksTable.Cols.TRACK_DATA))));
        track.setPhotoFiles(parsePhotoFilesJSON(getString(getColumnIndex(TracksTable.Cols.PHOTO_FILES))));
        return track;
    }

    private ArrayList<TrackLocation> parseTrackDataJSON(String jsonString) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, new TypeToken<ArrayList<TrackLocation>>() {}.getType());
    }

    private ArrayList<TrackPhoto> parsePhotoFilesJSON(String jsonString) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, new TypeToken<ArrayList<TrackPhoto>>() {}.getType());
    }
}
