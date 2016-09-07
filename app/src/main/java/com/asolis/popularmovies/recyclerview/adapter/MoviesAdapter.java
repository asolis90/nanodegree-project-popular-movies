package com.asolis.popularmovies.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.recyclerview.viewholders.LoadMoreViewHolder;
import com.asolis.popularmovies.recyclerview.viewholders.MoviesViewHolder;
import com.asolis.popularmovies.recyclerview.viewholders.RetryViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelsolis on 7/31/16.
 */
public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Movie> data = new ArrayList<>();
    private Context context;
    private boolean loadingState = false;
    private boolean isRetry = false;
    private LoadMoreListener loadMoreListener;
    private int VIEW_TYPE_MOVIE = 0;
    private int VIEW_TYPE_LOADING = 1;
    private int VIEW_TYPE_RETRY = 2;
    private int page;

    private OnclickListener onclickListener;
    private OnRetryClickListener onRetryClickListener;


    public MoviesAdapter(Context context, List<Movie> data, int page) {
        this.context = context;
        this.data.addAll(data);
        this.page = page;
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) == null) {
            if (isRetry) {
                return VIEW_TYPE_RETRY;
            }
            return VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_MOVIE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_MOVIE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
            return new MoviesViewHolder(view, onclickListener);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadMoreViewHolder(view);
        } else if(viewType == VIEW_TYPE_RETRY){
            View view = LayoutInflater.from(context).inflate(R.layout.item_retry, parent, false);
            return new RetryViewHolder(view, onRetryClickListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MoviesViewHolder) {
            ((MoviesViewHolder) holder).bind(context, data.get(position));
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public boolean isLoading() {
        return loadingState;
    }

    public void setLoadingState(boolean loadingState) {
        this.loadingState = loadingState;
    }

    public int getCurrentPage() {
        return page;
    }

    public void setCurrentPage(int page) {
        this.page = page;
    }

    public LoadMoreListener getLoadMoreListener() {
        return loadMoreListener;
    }

    public void setLoadMoreListener(LoadMoreListener listener) {
        loadMoreListener = listener;
    }

    public void clear() {
        data.clear();
    }

    public void setIsRetry(boolean isRetry) {
        this.isRetry = isRetry;
    }

    public interface LoadMoreListener {
        void onLoad();
    }

    public ArrayList<Movie> getData() {
        return data;
    }

    public void addItem(Movie item) {
        data.add(item);
        notifyItemInserted(data.size() - 1);
    }

    public void addItems(List<Movie> list) {
        if(list != null){
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void removeLastItem() {
        data.remove(data.size() - 1);
        notifyItemRemoved(data.size());
    }

    public void setOnclickListener(OnclickListener onclickListener) {
        this.onclickListener = onclickListener;
    }

    public void setOnRetryClickListener(OnRetryClickListener listener) {
        this.onRetryClickListener = listener;
    }

    public interface OnclickListener {
        void onClick(View view, Movie movie);
    }

    public interface OnRetryClickListener {
        void onClick(View view);
    }
}
