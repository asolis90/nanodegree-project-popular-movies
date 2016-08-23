package com.asolis.popularmovies.util;

import android.content.Context;

/**
 * Created by angelsolis on 8/14/16.
 */

public class PreferenceManager {

    private static final String PREFERENCE_FILE = "app-preference";
    private static final String PREF_SORT_TYPE = "total-sound-recordings";

    public static void setSortingType(Context context, SortingType type) {
        context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE).edit()
                .putInt(PREF_SORT_TYPE, type.getValue()).apply();
    }

    public static SortingType getSortingType(Context context) {
        return SortingType.getEnumFromInt(context.getSharedPreferences(PREFERENCE_FILE,
                Context.MODE_PRIVATE).getInt(PREF_SORT_TYPE, SortingType.POPULAR.getValue()));
    }
}
