package com.asolis.popularmovies.recyclerview.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.recyclerview.adapter.MoviesAdapter;

/**
 * Created by angelsolis on 7/31/16.
 */
public class RetryViewHolder extends RecyclerView.ViewHolder {
    public Button mButton;

    public RetryViewHolder(View itemView, final MoviesAdapter.OnRetryClickListener listener) {
        super(itemView);
        mButton = (Button) itemView.findViewById(R.id.retry_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view);
            }
        });

    }
}
