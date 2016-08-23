package com.asolis.popularmovies.recyclerview.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.asolis.popularmovies.R;
import com.asolis.popularmovies.net.models.Movie;
import com.asolis.popularmovies.recyclerview.adapter.MoviesAdapter;
import com.squareup.picasso.Picasso;

/**
 * Created by angelsolis on 7/31/16.
 */
public class MoviesViewHolder extends RecyclerView.ViewHolder {

    public ImageView movieImageView;
    public FrameLayout frameLayout;
    public static String baseUrl = "https://image.tmdb.org/t/p/w300_and_h450_bestv2/";
    private Movie movie;

    public MoviesViewHolder(View itemView, final MoviesAdapter.OnclickListener listener) {
        super(itemView);
        movieImageView = (ImageView) itemView.findViewById(R.id.item_movie_iv);
        frameLayout = (FrameLayout) itemView.findViewById(R.id.item_movie_fl);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(movieImageView, movie);
            }
        });
    }

    public void bind(Context context, Movie movie) {
        this.movie = movie;
        Picasso.with(context).load(baseUrl + movie
                .getPosterPath()).into(movieImageView);
    }

}
