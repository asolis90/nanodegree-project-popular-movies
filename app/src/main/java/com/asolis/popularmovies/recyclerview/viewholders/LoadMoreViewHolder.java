package com.asolis.popularmovies.recyclerview.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.asolis.popularmovies.R;

/**
 * Created by angelsolis on 7/31/16.
 */
public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;

    public LoadMoreViewHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.item_loading_progressBar);
    }
}
