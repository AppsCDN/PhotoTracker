package vitalypanov.phototracker.database;

/**
 * Created by Vitaly on 24.02.2018.
 */

public class TrackerDatabaseSchema {
    public static final class TracksTable{
        public static final String NAME = "Tracks";
        public static final class Cols{
            public static final String UIID = "uuid";
            public static final String START_TIME = "start_time";
            public static final String END_TIME  ="end_time";
            public static final String COMMENT = "comment";
            public static final String TRACK_DATA = "track_data";
        }
    }
}
