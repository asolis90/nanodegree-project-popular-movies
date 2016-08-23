package com.asolis.popularmovies.net.models.base;

/**
 * Created by angelsolis on 7/31/16.
 */
public class Base<Data> {
    private String page;
    private Data[] results;

    public String getPage() {
        return page;
    }

    public Data[] getResults() {
        return results;
    }
}
