package com.weatherradar.content_provider;


import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.weatherradar.database.DBContract;
import com.weatherradar.database.DBHelper;

import java.util.ArrayList;

import static com.weatherradar.content_provider.ProviderContract.CONTENT_AUTHORITY;
import static com.weatherradar.content_provider.ProviderContract.PATH_FORECAST;
import static com.weatherradar.content_provider.ProviderContract.PATH_LOCATION;

public class WeatherContentProvider extends ContentProvider {

    private static final int LOCATIONS = 1;
    private static final int LOCATION_ID = 2;
    private static final int FORECASTS = 3;
    private static final int FORECAST_ID = 4;

    private static final UriMatcher MATCHER;

    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        MATCHER.addURI(CONTENT_AUTHORITY, PATH_LOCATION, LOCATIONS);
        MATCHER.addURI(CONTENT_AUTHORITY, PATH_LOCATION + "/#", LOCATION_ID);
        MATCHER.addURI(CONTENT_AUTHORITY, PATH_FORECAST, FORECASTS);
        MATCHER.addURI(CONTENT_AUTHORITY, PATH_FORECAST + "/#", FORECAST_ID);
    }

    private DBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            mDBHelper = new DBHelper(getContext());
        }
        return mDBHelper != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String orderBy) {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;

        int match = MATCHER.match(uri);
        switch (match) {
            case FORECASTS:
                cursor = db.query(DBContract.Table.FORECAST, projection, selection, selectionArgs, null, null, orderBy);
                break;
            case FORECAST_ID:
                selection = DBContract.Forecast.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DBContract.Table.FORECAST, projection, selection, selectionArgs, null, null, orderBy);
                break;
            case LOCATIONS:
                cursor = db.query(DBContract.Table.LOCATION, projection, selection, selectionArgs, null, null, orderBy);
                break;
            case LOCATION_ID:
                selection = DBContract.Location.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DBContract.Table.LOCATION, projection, selection, selectionArgs, null, null, orderBy);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int matcher = MATCHER.match(uri);
        switch (matcher) {
            case FORECASTS:
                return String.format("%s/vnd.%s.%s",
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        PATH_FORECAST
                );
            case LOCATIONS:
                return String.format("%s/vnd.%s.%s",
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        PATH_LOCATION
                );
            case FORECAST_ID:
                return String.format("%s/vnd.%s.%s",
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        PATH_FORECAST
                );
            case LOCATION_ID:
                return String.format("%s/vnd.%s.%s",
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        CONTENT_AUTHORITY,
                        PATH_LOCATION
                );
        }


        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        long id;
        int matcher = MATCHER.match(uri);

        switch (matcher) {
            case FORECASTS:
                id = db.insertOrThrow(DBContract.Table.FORECAST, null, values);
                break;
            case LOCATIONS:
                id = db.insertOrThrow(DBContract.Table.LOCATION, null, values);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int match = MATCHER.match(uri);

        int rowCount;

        switch (match) {
            case FORECASTS:
                rowCount = db.delete(DBContract.Table.FORECAST, selection, selectionArgs);
                break;
            case FORECAST_ID:
                selection = DBContract.Forecast.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowCount = db.delete(DBContract.Table.FORECAST, selection, selectionArgs);
                break;
            case LOCATIONS:
                rowCount = db.delete(DBContract.Table.LOCATION, selection, selectionArgs);
                break;
            case LOCATION_ID:
                selection = DBContract.Location.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowCount = db.delete(DBContract.Table.LOCATION, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int matcher = MATCHER.match(uri);
        int rowCount;

        switch (matcher) {
            case FORECASTS:
                rowCount = db.update(DBContract.Table.FORECAST, values, selection, selectionArgs);
                break;
            case FORECAST_ID:
                selection = DBContract.Forecast.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowCount = db.update(DBContract.Table.FORECAST, values, selection, selectionArgs);
                break;
            case LOCATIONS:
                rowCount = db.update(DBContract.Table.LOCATION, values, selection, selectionArgs);
                break;
            case LOCATION_ID:
                selection = DBContract.Location.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowCount = db.update(DBContract.Table.LOCATION, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);

        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }


        return rowCount;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void shutdown() {
        mDBHelper.close();
        super.shutdown();
    }
}
