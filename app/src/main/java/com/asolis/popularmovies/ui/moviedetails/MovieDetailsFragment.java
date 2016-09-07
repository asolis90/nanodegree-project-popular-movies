package com.asolis.popularmovies.ui.moviedetails;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.data.DbContract;
import com.asolis.popularmovies.data.DbProvider;
import com.asolis.popularmovies.net.TheMovieDB;
import com.asolis.popularmovies.net.TheMovieDBAPIHelper;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.MovieReview;
import com.asolis.popularmovies.net.models.MovieVideo;
import com.asolis.popularmovies.net.models.base.Base;
import com.asolis.popularmovies.recyclerview.viewholders.MoviesViewHolder;
import com.asolis.popularmovies.ui.main.MainActivity;
import com.asolis.popularmovies.widget.ReviewLayout;
import com.asolis.popularmovies.widget.VideoLayout;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by angelsolis on 9/4/16.
 */

public class MovieDetailsFragment extends Fragment {

    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.activity_details_ll_videos) LinearLayout mVideosLinearLayout;
    @Bind(R.id.activity_details_ll_reviews) LinearLayout mReviewsLinearLayout;
    @Bind(R.id.activity_details_cv_videos) CardView mVideosCardView;
    @Bind(R.id.activity_details_cv_reviews) CardView mReviewsCardView;
    @Bind(R.id.activity_details_iv_backdrop) ImageView mBackdropImageView;
    @Bind(R.id.activity_details_iv_poster) ImageView mPosterImageView;
    @Bind(R.id.activity_details_tv_title) TextView mTitleTextView;
    @Bind(R.id.activity_details_tv_release_date) TextView mReleaseDataTextView;
    @Bind(R.id.activity_details_tv_overview) TextView mOverviewTextView;
    @Bind(R.id.activity_details_tv_vote_avg) TextView mVoteAverageTextView;
    @Bind(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.fab) FloatingActionButton mFavoriteFab;

    private Movie movie;
    private boolean isFabHidden = false;
    private static final String ARG_MOVIE = "movie";
    private final String DECIMAL_FORMAT = "#.0";
    private ArrayList<MovieVideo> videos;
    private ArrayList<MovieReview> reviews;
    private int ACTION_BAR_SIZE = 56;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (savedInstanceState != null) {
            movie = savedInstanceState.getParcelable(ARG_MOVIE);
        } else {
            movie = getArguments().getParcelable(ARG_MOVIE);
        }
        mFavoriteFab.setOnClickListener(onClickListener);
        loadData();
        fetchVideos();
        fetchReviews();
        prepareToolbar();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_MOVIE, movie);
        super.onSaveInstanceState(outState);
    }

    public static Fragment newInstance(Movie movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    private void prepareToolbar() {
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                final int expandRange = collapsingToolbarLayout.getHeight() - ViewCompat
                        .getMinimumHeight(collapsingToolbarLayout);

                boolean fabShouldBeVisible = expandRange + verticalOffset <=
                        getToolbarMinHeight();
                setFabVisibility(fabShouldBeVisible);
            }
        });
    }

    private void loadData() {
        mTitleTextView.setText(movie.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(movie.getRelease_date());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            mReleaseDataTextView.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT);
        String avg = df.format(Float.valueOf(movie.getVoteAverage()));
        mVoteAverageTextView.setText(getString(R.string.detail_vote_avergae, avg));
        mOverviewTextView.setText(movie.getOverview());

        Picasso.with(getContext()).load(MoviesViewHolder.baseUrl + movie
                .getBackdropPath()).into(mBackdropImageView);
        Picasso.with(getContext()).load(MoviesViewHolder.baseUrl + movie
                .getPosterPath()).into(mPosterImageView);

        if (checkIfFavorite()) {
            mFavoriteFab.setImageResource(R.drawable.favorite);
        }
    }

    private void fetchVideos() {
        TheMovieDB.api().getMovieVideos(movie.getId(), TheMovieDBAPIHelper.getApiKey()
                , new Callback<Base<MovieVideo>>() {
                    @Override
                    public void success(Base<MovieVideo> moviesBase, Response response) {
                        videos = new ArrayList<>();
                        videos.addAll(Arrays.asList(moviesBase.getResults()));
                        if (videos.size() != 0) {
                            for (int i = 0; i < videos.size(); i++) {
                                VideoLayout layout = new VideoLayout(getContext());
                                layout.setText(videos.get(i).getName());
                                layout.setOnClickListener(onClickListener);

                                mVideosLinearLayout.addView(layout);
                                if ((i + 1) != videos.size()) {
                                    View view = new View(getContext());
                                    view.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                    view.setBackgroundResource(R.color.light_gray);
                                    mVideosLinearLayout.addView(view);
                                }
                            }
                        } else {
                            mVideosCardView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            Snackbar snackbar = Snackbar.make(mCoordinatorLayout,
                                    getString(R.string.network_connection_error),
                                    Snackbar.LENGTH_INDEFINITE).setAction("Retry",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            fetchVideos();
                                        }
                                    });
                            snackbar.show();
                        }
                    }
                });
    }

    private void fetchReviews() {
        TheMovieDB.api().getMovieReviews(movie.getId(), TheMovieDBAPIHelper.getApiKey()
                , new Callback<Base<MovieReview>>() {
                    @Override
                    public void success(Base<MovieReview> moviesBase, Response response) {
                        reviews = new ArrayList<>();
                        reviews.addAll(Arrays.asList(moviesBase.getResults()));
                        if (reviews.size() != 0) {
                            for (int i = 0; i < reviews.size(); i++) {
                                ReviewLayout layout = new ReviewLayout(getContext());
                                layout.setAuthor(reviews.get(i).getAuthor());
                                layout.setContent(reviews.get(i).getContent());

                                mReviewsLinearLayout.addView(layout);
                                if ((i + 1) != reviews.size()) {
                                    View view = new View(getContext());
                                    view.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                    view.setBackgroundResource(R.color.light_gray);
                                    mReviewsLinearLayout.addView(view);
                                }
                            }
                        } else {
                            mReviewsCardView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            Snackbar snackbar = Snackbar.make(mCoordinatorLayout,
                                    getString(R.string.network_connection_error),
                                    Snackbar.LENGTH_INDEFINITE).setAction("Retry",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            fetchReviews();
                                        }
                                    });
                            snackbar.show();
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //TODO: finish app
                break;
        }
        return true;
    }

    protected void setFabVisibility(boolean visible) {
        if (visible) {
            if (!isFabHidden) {
                isFabHidden = true;
                mFavoriteFab.hide();
            }
        } else {
            if (isFabHidden) {
                isFabHidden = false;
                mFavoriteFab.show();
            }
        }
    }

    protected int getToolbarMinHeight() {
        return ViewCompat
                .getMinimumHeight(collapsingToolbarLayout) + ACTION_BAR_SIZE;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab:
                    // TODO: handle favorite
                    // we have to query all the favorites and then check by title if this movie is already added to favorites
                    // also at the beginning of the activity we have to check for that specific movie if its favorite or not
                    // maybe do this in splash activity - after loading

                    ContentResolver contentResolver = getContext().getContentResolver();
                    if (!checkIfFavorite()) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DbContract.Columns.FAV_ID_COLUMN, movie.getId());
                        contentValues.put(DbContract.Columns.FAV_TITLE_COLUMN, movie.getTitle());
                        contentValues.put(DbContract.Columns.FAV_OVERVIEW_COLUMN, movie.getOverview());
                        contentValues.put(DbContract.Columns.FAV_RELEASE_DATE_COLUMN, movie.getRelease_date());
                        contentValues.put(DbContract.Columns.FAV_VOTE_AVERAGE_COLUMN, movie.getVoteAverage());
                        contentValues.put(DbContract.Columns.FAV_BACKDROP_PATH_COLUMN, movie.getBackdropPath());
                        contentValues.put(DbContract.Columns.FAV_POSTER_PATH_COLUMN, movie.getPosterPath());
                        contentResolver.insert(DbProvider.CONTENT_URI_FAVORITES, contentValues);
                        mFavoriteFab.setImageResource(R.drawable.favorite);
                    } else {
                        String where = DbContract.Columns.FAV_ID_COLUMN + "=?";
                        String[] args = new String[]{movie.getId()};
                        contentResolver.delete(DbProvider.CONTENT_URI_FAVORITES, where, args);
                        mFavoriteFab.setImageResource(R.drawable.favorite_outline);
                    }
                    getLoaderManager().restartLoader(0, null, ((MainActivity) getActivity()));
                    break;
                case R.id.video_layout:
                    for (MovieVideo video : videos) {
                        String text = ((VideoLayout) view).getText();
                        if (video.getName().matches(text)) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + video.getKey()));
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.youtube.com/watch?v=" + video.getKey()));
                                startActivity(intent);
                            }
                        }
                    }
                    break;
            }
        }
    };

    private boolean checkIfFavorite() {
        ContentResolver resolver = getContext().getContentResolver();
        String[] projection = {DbContract.Columns.FAV_ID_COLUMN};
        String selection = DbContract.Columns.FAV_ID_COLUMN + " = ?";
        String[] selectionArgs = {movie.getId()};
        String sortOrder = null;

        Cursor cursor = resolver.query(
                DbProvider.CONTENT_URI_FAVORITES,
                projection,
                selection,
                selectionArgs,
                sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }
}
