package com.asolis.popularmovies.ui.base;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.asolis.popularmovies.R;

public class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    public void setContentView(int layoutResID)
    {
        super.setContentView(layoutResID);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // init toolbar
        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }
}
