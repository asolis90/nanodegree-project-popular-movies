package com.asolis.popularmovies.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asolis.popularmovies.R;

/**
 * Created by angelsolis on 8/21/16.
 */

public class VideoLayout extends LinearLayout {

    private TextView textView;

    public VideoLayout(Context context) {
        super(context);
        initViews();
    }

    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.video_linear_layout, this);
        setId(R.id.video_layout);
        textView = (TextView) this.findViewById(R.id.custom_text);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public String getText() {
        return textView.getText().toString();
    }
}
