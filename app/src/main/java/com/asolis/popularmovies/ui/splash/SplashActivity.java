package com.asolis.popularmovies.ui.splash;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.data.DbContract;
import com.asolis.popularmovies.data.DbProvider;
import com.asolis.popularmovies.net.TheMovieDB;
import com.asolis.popularmovies.net.TheMovieDBAPIHelper;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.base.Base;
import com.asolis.popularmovies.ui.main.MainActivity;
import com.asolis.popularmovies.util.PreferenceManager;
import com.asolis.popularmovies.util.SortingType;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SplashActivity extends AppCompatActivity {

    private final String ARG_MOVIES = "movies";
    private final String DEFAULT_PAGE = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        loadData();
    }

    private void loadData() {
        if (PreferenceManager.getSortingType(getApplicationContext()) != SortingType.FAVORITES) {
            TheMovieDB.api().getMovies(PreferenceManager.getSortingType(getApplicationContext())
                            .getPath(), TheMovieDBAPIHelper.getApiKey(),
                    DEFAULT_PAGE, new Callback<Base<Movie>>() {
                        @Override
                        public void success(Base<Movie> moviesBase, Response response) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            Bundle bundle = new Bundle();
                            ArrayList<Movie> movies = new ArrayList<>();
                            movies.addAll(Arrays.asList(moviesBase.getResults()));
                            bundle.putParcelableArrayList(ARG_MOVIES, movies);
                            intent.putExtras(bundle);
                            finish();
                            startActivity(intent);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                                builder.setTitle(getString(R.string.network_issues));
                                builder.setMessage(getString(R.string.network_issues_msg));
                                builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        loadData();
                                    }
                                });
                                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                                final AlertDialog alertDialog = builder.show();
                                alertDialog.setCanceledOnTouchOutside(false);
                            }
                        }
                    });
        } else {
            loadFavorites();
        }
    }

    private void loadFavorites() {
        Log.e("loadFavorites","here");
        Cursor cursor = getContentResolver().query(DbProvider.CONTENT_URI_FAVORITES, null, null, null, null);

        ArrayList<Movie> movies = new ArrayList<>();
        if (cursor != null) {

            Log.e("loadFavorites","cursor != null");
            if (cursor.moveToFirst()) {
                do {
                    // do what you need with the cursor here
                    Movie movie = new Movie();
                    movie.setId(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_ID_COLUMN)));
                    movie.setTitle(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_TITLE_COLUMN)));
                    movie.setOverview(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_OVERVIEW_COLUMN)));
                    movie.setRelease_date(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_RELEASE_DATE_COLUMN)));
                    movie.setVoteAverage(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_VOTE_AVERAGE_COLUMN)));
                    movie.setPosterPath(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_POSTER_PATH_COLUMN)));
                    movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(DbContract.Columns.FAV_BACKDROP_PATH_COLUMN)));
                    movies.add(movie);
                } while (cursor.moveToNext());
            }

            cursor.close();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(ARG_MOVIES, movies);
            intent.putExtras(bundle);
            finish();
            startActivity(intent);
        }
    }
}
