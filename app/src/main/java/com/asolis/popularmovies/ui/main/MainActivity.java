package com.asolis.popularmovies.ui.main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.net.TheMovieDB;
import com.asolis.popularmovies.net.TheMovieDBAPIHelper;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.base.Base;
import com.asolis.popularmovies.ui.base.BaseActivity;
import com.asolis.popularmovies.ui.home.HomeFragment;
import com.asolis.popularmovies.util.PreferenceManager;
import com.asolis.popularmovies.util.SortingType;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends BaseActivity {

    private final String FRAGMENT_HOME_TAG = "HOME";
    private final int DEFAULT_PAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            setMainContainer(getIntent().getExtras());
        }
    }

    private void setMainContainer(Bundle bundle) {
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, homeFragment, FRAGMENT_HOME_TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_sort_by:
                showSortingDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortingDialog() {
        final SortingType sortType = PreferenceManager.getSortingType(getApplicationContext());
        final String[] arr = getResources().getStringArray(R.array.sorting_by_array);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.action_sort_by)).setItems(arr, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        switch (SortingType.getEnumFromInt(position)) {
                            case POPULAR:
                                if (sortType != SortingType.POPULAR) {
                                    loadMovies(SortingType.POPULAR);
                                }
                                break;
                            case HIGHEST_RATE:
                                if (sortType != SortingType.HIGHEST_RATE) {
                                    loadMovies(SortingType.HIGHEST_RATE);
                                }
                                break;
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private void loadMovies(final SortingType type) {
        TheMovieDB.api().getMovies(TheMovieDBAPIHelper.getApiKey(), type.getPath(),
                String.valueOf(DEFAULT_PAGE), new Callback<Base<Movie>>() {
                    @Override
                    public void success(Base<Movie> moviesBase, Response response) {
                        PreferenceManager.setSortingType(getApplicationContext(), type);
                        ArrayList<Movie> movies = new ArrayList<>();
                        movies.addAll(Arrays.asList(moviesBase.getResults()));
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                                .findFragmentByTag(FRAGMENT_HOME_TAG);
                        if (homeFragment != null && homeFragment.isVisible()) {
                            homeFragment.setData(movies);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(getString(R.string.network_issues));
                            builder.setMessage(getString(R.string.network_issues_msg));
                            builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    loadMovies(type);
                                }

                            });
                            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            final AlertDialog alertDialog = builder.show();
                            alertDialog.setCanceledOnTouchOutside(false);
                        }
                    }
                });
    }
}
