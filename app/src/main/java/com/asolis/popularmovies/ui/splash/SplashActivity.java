package com.asolis.popularmovies.ui.splash;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.net.TheMovieDB;
import com.asolis.popularmovies.net.TheMovieDBAPIHelper;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.base.Base;
import com.asolis.popularmovies.ui.main.MainActivity;
import com.asolis.popularmovies.util.PreferenceManager;

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
    }
}
