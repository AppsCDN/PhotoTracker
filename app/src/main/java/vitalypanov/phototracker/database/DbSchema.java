package vitalypanov.phototracker.database;

/**
 * Created by Vitaly on 24.02.2018.
 */

public class DbSchema {
    public static final class TracksTable{
        public static final String NAME = "Tracks";
        public static final class Cols{
            public static final String ID = "_id";
            public static final String UUID = "uuid";
            public static final String START_TIME = "start_time";
            public static final String END_TIME  ="end_time";
            public static final String COMMENT = "comment";
            public static final String DISTANCE = "distance";
            public static final String TRACK_DATA = "track_data";
            public static final String PHOTO_FILES = "photo_files";
        }
    }
}
