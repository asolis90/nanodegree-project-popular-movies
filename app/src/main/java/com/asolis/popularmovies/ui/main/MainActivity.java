package com.asolis.popularmovies.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.data.DbContract;
import com.asolis.popularmovies.data.DbProvider;
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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int UPDATE_RESULT_CODE = 2;
    private final String FRAGMENT_HOME_TAG = "HOME";
    private final int DEFAULT_PAGE = 1;
    private List<Movie> mFavoriteMovieList = new ArrayList<>();
    private boolean mIsLoaderInitialized;
    private boolean mIsTwoPane;
    private static String ARG_IS_TWO_PANE = "is_two_pane";

    @Nullable
    @Bind(R.id.activity_main_fragment_details) FrameLayout mDetailsFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (mDetailsFrameLayout != null) {
            mIsTwoPane = true;
        }

        if (savedInstanceState == null) {
            setMainContainer(getIntent().getExtras());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_RESULT_CODE) {
            getSupportLoaderManager().restartLoader(0, null, this);
        }
    }

    private ArrayList<Movie> getFavorites(Cursor cursor) {
        ArrayList<Movie> movies = new ArrayList<>();
        if (cursor != null) {
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

                cursor.close();
                return movies;
            }
        }
        return null;
    }

    private void setMainContainer(Bundle bundle) {
        HomeFragment homeFragment = new HomeFragment();
        bundle.putBoolean(ARG_IS_TWO_PANE, mIsTwoPane);
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
                            case TOP_RATED:
                                if (sortType != SortingType.TOP_RATED) {
                                    loadMovies(SortingType.TOP_RATED);
                                }
                                break;

                            case FAVORITES:
                                if (sortType != SortingType.FAVORITES) {
                                    loadFavorites();
                                }
                                break;
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private void loadFavorites() {
        PreferenceManager.setSortingType(getApplicationContext(), SortingType.FAVORITES);

        if (!mIsLoaderInitialized) {
            this.getSupportLoaderManager().initLoader(0, null, this);
        } else {
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_HOME_TAG);
            if (homeFragment != null && homeFragment.isVisible()) {
                homeFragment.setData(mFavoriteMovieList);
            }
        }
    }

    private void loadMovies(final SortingType type) {
        TheMovieDB.api().getMovies(type.getPath(), TheMovieDBAPIHelper.getApiKey(),
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getApplicationContext(),
                DbProvider.CONTENT_URI_FAVORITES,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mFavoriteMovieList != null) {
            mFavoriteMovieList.clear();
        }
        mFavoriteMovieList = getFavorites(cursor);

        if (PreferenceManager.getSortingType(getApplicationContext()) == SortingType.FAVORITES) {
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_HOME_TAG);
            if (homeFragment != null && homeFragment.isVisible()) {
                homeFragment.setData(mFavoriteMovieList);
            }
        }
        mIsLoaderInitialized = true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
