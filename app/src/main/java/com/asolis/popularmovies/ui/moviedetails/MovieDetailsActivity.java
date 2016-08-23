package com.asolis.popularmovies.ui.moviedetails;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.net.TheMovieDB;
import com.asolis.popularmovies.net.TheMovieDBAPIHelper;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.MovieVideo;
import com.asolis.popularmovies.net.models.base.Base;
import com.asolis.popularmovies.recyclerview.viewholders.MoviesViewHolder;
import com.asolis.popularmovies.ui.base.BaseActivity;
import com.asolis.popularmovies.widget.VideoLayout;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MovieDetailsActivity extends BaseActivity {

    private CoordinatorLayout mCoordinatorLayout;
    private LinearLayout mVideosLinearLayout;
    private CardView mVideosCardView;
    private ImageView mBackdropImageView;
    private ImageView mPosterImageView;
    private TextView mTitleTextView;
    private TextView mReleaseDataTextView;
    private TextView mOverviewTextView;
    private TextView mVoteAverageTextView;
    private AppBarLayout mAppBarLayout;
    private FloatingActionButton mFavoriteFab;
    private Movie movie;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private boolean isFabHidden = false;
    private static final String ARG_MOVIE = "movie";
    private final String DECIMAL_FORMAT = "#.0";
    private ArrayList<MovieVideo> videos;

    public static void launch(Activity activity, Movie movie, View view) {

        Intent intent = new Intent(activity, MovieDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_MOVIE, movie);
        intent.putExtras(bundle);
        ActivityOptionsCompat options;
        if (view != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, new Pair<>(view, activity.getResources()
                            .getString(R.string.detail_iv_poster_transition)));
        } else {
            // set options without shared element transition
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        }
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        prepareViews();
        movie = getIntent().getExtras().getParcelable(ARG_MOVIE);

        mFavoriteFab.setOnClickListener(onClickListener);
        prepareToolbar();
        loadData();
        fetchVideos();
    }

    private void prepareViews() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mFavoriteFab = (FloatingActionButton) findViewById(R.id.fab);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mBackdropImageView = (ImageView) findViewById(R.id.activity_details_iv_backdrop);
        mPosterImageView = (ImageView) findViewById(R.id.activity_details_iv_poster);
        mTitleTextView = (TextView) findViewById(R.id.activity_details_tv_title);
        mReleaseDataTextView = (TextView) findViewById(R.id.activity_details_tv_release_date);
        mOverviewTextView = (TextView) findViewById(R.id.activity_details_tv_overview);
        mVoteAverageTextView = (TextView) findViewById(R.id.activity_details_tv_vote_avg);
        mVideosLinearLayout = (LinearLayout) findViewById(R.id.activity_details_ll_videos);
        mVideosCardView = (CardView) findViewById(R.id.activity_details_cv_videos);
    }

    private void prepareToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                final int expandRange = collapsingToolbarLayout.getHeight() - ViewCompat
                        .getMinimumHeight(collapsingToolbarLayout);
                // Toggling toolbar
                boolean titleShouldBeVisible = expandRange + verticalOffset <=
                        getToolbarMinHeight();
                setToolbarTitleVisible(titleShouldBeVisible);
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

        Picasso.with(getApplicationContext()).load(MoviesViewHolder.baseUrl + movie
                .getBackdropPath()).into(mBackdropImageView);
        Picasso.with(getApplicationContext()).load(MoviesViewHolder.baseUrl + movie
                .getPosterPath()).into(mPosterImageView);
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
                                VideoLayout layout = new VideoLayout(getApplicationContext());
                                layout.setText(videos.get(i).getName());
                                layout.setOnClickListener(onClickListener);

                                mVideosLinearLayout.addView(layout);
                                if ((i + 1) != videos.size()) {
                                    View view = new View(getApplicationContext());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    protected void setToolbarTitleVisible(boolean visible) {
        if (visible) {
            collapsingToolbarLayout.setTitle(movie.getTitle());
            if (!isFabHidden) {
                isFabHidden = true;
                mFavoriteFab.hide();
            }
        } else {
            collapsingToolbarLayout.setTitle("");
            if (isFabHidden) {
                isFabHidden = false;
                mFavoriteFab.show();
            }
        }
    }

    protected int getToolbarMinHeight() {
        return ViewCompat
                .getMinimumHeight(collapsingToolbarLayout);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab:
                    // TODO: handle favorite
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
}
