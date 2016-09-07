package com.asolis.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by angelsolis on 8/28/16.
 */

public class DbProvider extends ContentProvider {

    private static final String AUTHORITY = "com.asolis.popularmovies.data.DbProvider";

    private static final String PATH_FAVORITES = DbContract.FAVORITES_TABLE_NAME;

    private static final int FAVORITES = 0x1;

    private static final UriMatcher mUriMatcher;
    private DBHelper mDbHelper;

    public static final Uri CONTENT_URI_FAVORITES = Uri.parse("content://" + AUTHORITY + "/" + PATH_FAVORITES);

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, PATH_FAVORITES, FAVORITES);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                queryBuilder.setTables(DbContract.FAVORITES_TABLE_NAME);
                break;
            default:
                throw new IllegalStateException("Unknown Uri");
        }
        Cursor cursor = queryBuilder.query(mDbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        Uri path = null;

        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                id = database.insert(DbContract.FAVORITES_TABLE_NAME,
                        null, values);
                if (id > 0) {
                    path = ContentUris.withAppendedId(CONTENT_URI_FAVORITES, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;

            default:
                throw new IllegalStateException("Unknown Uri");
        }

        getContext().getContentResolver().notifyChange(path, null);
        return path;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteCount;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                deleteCount = db.delete(DbContract.FAVORITES_TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalStateException("Unknown Uri");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updatedCount;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                updatedCount = db.update(DbContract.FAVORITES_TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalStateException("Unknown Uri");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return updatedCount;
    }
}
