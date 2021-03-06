package com.asolis.popularmovies.ui.home;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.net.TheMovieDB;
import com.asolis.popularmovies.net.TheMovieDBAPIHelper;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.net.models.base.Base;
import com.asolis.popularmovies.recyclerview.adapter.MoviesAdapter;
import com.asolis.popularmovies.ui.moviedetails.MovieDetailsActivity;
import com.asolis.popularmovies.ui.moviedetails.MovieDetailsFragment;
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

/**
 * Created by angelsolis on 8/6/16.
 */

public class HomeFragment extends Fragment {

    @Bind(R.id.fragment_home_rv)
    RecyclerView recyclerView;
    private MoviesAdapter moviesAdapter;
    private final String ARG_MOVIES = "movies";
    private final String ARG_PAGE = "page";
    private final int DEFAULT_PAGE = 1;
    private int page = 1;
    private boolean firstLoad = true;
    private boolean mIsTwoPane;
    private static String ARG_IS_TWO_PANE = "is_two_pane";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(ARG_PAGE, DEFAULT_PAGE);
            mIsTwoPane = savedInstanceState.getBoolean(ARG_IS_TWO_PANE, false);
            prepareAdapter(savedInstanceState);
        } else {
            mIsTwoPane = getArguments().getBoolean(ARG_IS_TWO_PANE, false);
            prepareAdapter(getArguments());

            if (mIsTwoPane) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_fragment_details,
                                MovieDetailsFragment.newInstance((Movie) getArguments().getParcelableArrayList(ARG_MOVIES).get(0)))
                        .commit();
            }
        }

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(
                    Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(5, 5, 5, 5);
            }
        });

        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(onSpanSizeLookup);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!firstLoad) {
                    if (!moviesAdapter.isLoading() && (recyclerView.getLayoutManager().getItemCount() - 1) ==
                            (((GridLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition())) {
                        if (moviesAdapter.getLoadMoreListener() != null &&
                                PreferenceManager.getSortingType(getContext()) != SortingType.FAVORITES) {
                            moviesAdapter.getLoadMoreListener().onLoad();
                            moviesAdapter.setLoadingState(true);
                        }
                    }
                }
                firstLoad = false;
            }
        });
        recyclerView.setAdapter(moviesAdapter);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_PAGE, moviesAdapter.getCurrentPage());
        outState.putParcelableArrayList(ARG_MOVIES, moviesAdapter.getData());
        outState.putBoolean(ARG_IS_TWO_PANE, mIsTwoPane);
        super.onSaveInstanceState(outState);
    }

    GridLayoutManager.SpanSizeLookup onSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            return moviesAdapter.getItemViewType(position) == 0 ? 1 : 2;
        }
    };

    private MoviesAdapter.OnclickListener onclickListener = new MoviesAdapter.OnclickListener() {
        @Override
        public void onClick(View view, Movie movie) {
            if (mIsTwoPane) {
                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(view, getResources()
                                .getString(R.string.detail_iv_poster_transition))
                        .replace(R.id.activity_main_fragment_details,
                                MovieDetailsFragment.newInstance(movie))
                        .commit();
            } else {
                MovieDetailsActivity.launchForResult(getActivity(), movie, view);
            }
        }
    };

    private MoviesAdapter.OnRetryClickListener onRetryClickListener = new MoviesAdapter.OnRetryClickListener() {
        @Override
        public void onClick(View view) {
            fetchMore();
        }
    };

    private void prepareAdapter(Bundle bundle) {
        final ArrayList<Movie> movies = bundle.getParcelableArrayList(ARG_MOVIES);
        moviesAdapter = new MoviesAdapter(getContext(), movies, page);
        moviesAdapter.setOnclickListener(onclickListener);
        moviesAdapter.setOnRetryClickListener(onRetryClickListener);
        moviesAdapter.setLoadMoreListener(new MoviesAdapter.LoadMoreListener() {
            @Override
            public void onLoad() {
                moviesAdapter.addItem(null);
                fetchMore();
            }
        });
    }

    private void fetchMore() {
        TheMovieDB.api().getMovies(PreferenceManager.getSortingType(getContext()).getPath(),
                TheMovieDBAPIHelper.getApiKey(),
                String.valueOf((moviesAdapter.getCurrentPage() + 1)),
                new Callback<Base<Movie>>() {
                    @Override
                    public void success(Base<Movie> moviesBase, Response response) {
                        moviesAdapter.removeLastItem();
                        moviesAdapter.setCurrentPage(moviesAdapter.getCurrentPage() + 1);
                        ArrayList<Movie> movies = new ArrayList<>();
                        movies.addAll(Arrays.asList(moviesBase.getResults()));
                        moviesAdapter.addItems(movies);
                        moviesAdapter.setLoadingState(false);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            moviesAdapter.removeLastItem();
                            moviesAdapter.setIsRetry(true);
                            moviesAdapter.addItem(null);
                        }
                    }
                });
    }

    public void setData(List<Movie> data) {
        moviesAdapter.setCurrentPage(DEFAULT_PAGE);
        moviesAdapter.clear();
        moviesAdapter.addItems(data);
        firstLoad = true;
    }
}
