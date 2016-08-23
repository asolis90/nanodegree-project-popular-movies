package com.asolis.popularmovies.net;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by angelsolis on 8/4/16.
 */

public class TheMovieDB {

    private static TheMovieDB mInstance;
    private TheMovieDBAPI API;

    public static TheMovieDBAPI api() {
        if (mInstance == null) {
            mInstance = new TheMovieDB();
        }
        return mInstance.getAPI();
    }

    public TheMovieDBAPI getAPI() {
        return API;
    }

    private TheMovieDB(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(TheMovieDBAPI.BASE_URL)
                .setClient(new OkClient())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        API = restAdapter.create(TheMovieDBAPI.class);
    }
}
