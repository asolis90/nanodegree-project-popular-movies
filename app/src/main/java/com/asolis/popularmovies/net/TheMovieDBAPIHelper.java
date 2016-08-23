package com.asolis.popularmovies.net;

/**
 * Created by angelsolis on 8/6/16.
 */

public class TheMovieDBAPIHelper {

    public static final String SORT_BY_POPULARITY = "popularity.desc";
    public static final String SORT_BY_HIGHEST_RATE = "vote_average.desc";
    private static final String API_KEY = "";


    public static String getApiKey() {
        return API_KEY;
    }
}
