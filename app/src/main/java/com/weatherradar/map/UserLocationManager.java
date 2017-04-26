package com.weatherradar.map;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.weatherradar.BuildConfig;
import com.weatherradar.CustomApplication;
import com.weatherradar.content_provider.ProviderContract;
import com.weatherradar.database.DBContract;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class UserLocationManager {

    private static final String TAG = UserLocationManager.class.getSimpleName();
    private static final double DELTA = 0.01;

    private UserLocationManager() {
        // hide
    }

    static void storeCurrentLocation(final Context context, final Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, location.getLatitude() + ", " + location.getLongitude());
        }
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {

                Cursor cursor = getCursor(context);
                ContentValues contentValues = getContentValues(location);
                if (cursor != null) {
                    if (cursor.getCount() == 0) {
                        insertCurrentLocation(context, contentValues);
                    } else {
                        boolean isNewLocation = isNewLocation(cursor, location);
                        if (isNewLocation) {
                            updateCurrentLocation(context, contentValues);
                        }
                    }
                    cursor.close();
                }

            }
        }).subscribeOn(Schedulers.from(((CustomApplication) context).getDbExecutor()))
                .subscribe();
    }

    private static Cursor getCursor(Context context) {
        return context.getContentResolver().query(ProviderContract.LOCATION_URI,
                new String[]{DBContract.Location.ID, DBContract.Location.LATITUDE, DBContract.Location.LONGITUDE},
                DBContract.Location.ID + "=?", new String[]{"0"}, null);
    }

    private static boolean isNewLocation(Cursor cursor, Location currentLocation) {

        if (cursor != null && cursor.moveToFirst()) {
            double oldLatitude = cursor.getDouble(cursor.getColumnIndex(DBContract.Location.LATITUDE));
            double oldLongitude = cursor.getDouble(cursor.getColumnIndex(DBContract.Location.LONGITUDE));

            return doubleNotEquals(currentLocation.getLatitude(), oldLatitude, DELTA) ||
                    doubleNotEquals(currentLocation.getLongitude(), oldLongitude, DELTA);
        } else {
            return false;
        }
    }

    private static boolean doubleNotEquals(double a, double b, double delta) {
        return Math.abs(a - b) > delta;
    }

    public static boolean doubletEquals(double a, double b) {
        return a == b || Math.abs(a - b) < DELTA;
    }

    private static ContentValues getContentValues(Location location) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.Location.ID, 0);
        contentValues.put(DBContract.Location.LATITUDE, location.getLatitude());
        contentValues.put(DBContract.Location.LONGITUDE, location.getLongitude());
        return contentValues;
    }

    private static void insertCurrentLocation(Context context, ContentValues contentValues) {
        context.getContentResolver().insert(ProviderContract.LOCATION_URI, contentValues);
    }

    private static void updateCurrentLocation(Context context, ContentValues contentValues) {
        context.getContentResolver().update(ProviderContract.LOCATION_URI, contentValues,
                DBContract.Location.ID + "=?", new String[]{"0"});
    }

}
