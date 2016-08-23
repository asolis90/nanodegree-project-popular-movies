package com.asolis.popularmovies.util;

/**
 * Created by angelsolis on 8/14/16.
 */

public enum SortingType {

    POPULAR(0, "popularity.desc"),
    HIGHEST_RATE(1, "vote_average.desc");

    private int value;
    private String path;

    public int getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }

    SortingType(int value, String path) {
        this.value = value;
        this.path = path;
    }

    public static SortingType getEnumFromInt(int value) {
        for (SortingType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return POPULAR;
    }
}
