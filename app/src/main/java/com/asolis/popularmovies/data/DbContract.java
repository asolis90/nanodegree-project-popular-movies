package com.asolis.popularmovies.data;

/**
 * Created by angelsolis on 8/28/16.
 */

public class DbContract {
    public static final String DATABASE = "movies.db";
    public static final String FAVORITES_TABLE_NAME = "favorites";
    public static final int VERSION = 1;

    public static final String CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + FAVORITES_TABLE_NAME
                    + " ("
                    + Columns.FAV_ID_COLUMN + " INTEGER PRIMARY KEY, "
                    + Columns.FAV_TITLE_COLUMN + " TEXT, "
                    + Columns.FAV_OVERVIEW_COLUMN + " TEXT, "
                    + Columns.FAV_RELEASE_DATE_COLUMN + " TEXT, "
                    + Columns.FAV_VOTE_AVERAGE_COLUMN + " TEXT, "
                    + Columns.FAV_BACKDROP_PATH_COLUMN + " TEXT, "
                    + Columns.FAV_POSTER_PATH_COLUMN + " TEXT );";

    public static final String DROP_FAVORITES_TABLE = "DROP TABLE IF EXISTS " + FAVORITES_TABLE_NAME;

    public class Columns {
        public static final String FAV_ID_COLUMN = "_id";
        public static final String FAV_TITLE_COLUMN = "title";
        public static final String FAV_OVERVIEW_COLUMN = "overview";
        public static final String FAV_RELEASE_DATE_COLUMN = "release_date";
        public static final String FAV_VOTE_AVERAGE_COLUMN = "vote_average";
        public static final String FAV_BACKDROP_PATH_COLUMN = "backdrop_path";
        public static final String FAV_POSTER_PATH_COLUMN = "poster_path";
    }
}
