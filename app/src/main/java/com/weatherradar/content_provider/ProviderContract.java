package com.weatherradar.content_provider;


import android.net.Uri;

import com.weatherradar.BuildConfig;
import com.weatherradar.database.DBContract;

public class ProviderContract {

    private ProviderContract() {
        // hide
    }

    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + "." + WeatherContentProvider.class.getSimpleName();
    public static final String PATH_LOCATION = DBContract.Table.LOCATION;
    public static final String PATH_FORECAST = DBContract.Table.FORECAST;
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri LOCATION_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LOCATION);
    public static final Uri FORECAST_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FORECAST);


}
