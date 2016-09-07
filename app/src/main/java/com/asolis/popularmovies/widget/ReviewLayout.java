package com.asolis.popularmovies.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asolis.popularmovies.R;

/**
 * Created by angelsolis on 8/21/16.
 */

public class ReviewLayout extends LinearLayout {

    private TextView contentTextView;
    private TextView authorTextView;

    public ReviewLayout(Context context) {
        super(context);
        initViews();
    }

    public ReviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ReviewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public ReviewLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.review_linear_layout, this);
        setId(R.id.review_layout);
        authorTextView = (TextView) this.findViewById(R.id.review_author);
        contentTextView = (TextView) this.findViewById(R.id.review_content);
    }

    public void setAuthor(String text) {
        authorTextView.setText(text);
    }

    public String getAuthor() {
        return authorTextView.getText().toString();
    }

    public void setContent(String text) {
        contentTextView.setText(text);
    }

    public String getContent() {
        return contentTextView.getText().toString();
    }
}
