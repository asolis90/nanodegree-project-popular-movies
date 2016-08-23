package com.asolis.popularmovies.net;

import com.asolis.popularmovies.net.models.MovieVideo;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.base.Base;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by angelsolis on 7/30/16.
 */
public interface TheMovieDBAPI {

    String BASE_URL = "https://api.themoviedb.org/3";

    interface params {
        String ID = "id";
        String API_KEY = "api_key";
        String SORT_BY = "sort_by";
        String PAGE = "page";
    }

    @GET("/discover/movie")
    void getMovies(
            @Query(params.API_KEY) String apiKey,
            @Query(params.SORT_BY) String sortBy,
            @Query(params.PAGE) String page,
            Callback<Base<Movie>> callback
    );

    @GET("/movie/{id}")
    void getMovieDetails(
            @Path(params.ID) String id,
            @Query(params.API_KEY) String apiKey,
            Callback<Base<Movie>> callback
    );


    @GET("/movie/{id}/videos")
    void getMovieVideos(
            @Path(params.ID) String id,
            @Query(params.API_KEY) String apiKey,
            Callback<Base<MovieVideo>> callback
    );
}
