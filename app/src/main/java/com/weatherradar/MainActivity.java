package com.weatherradar;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.weatherradar.content_provider.ProviderContract;
import com.weatherradar.database.DBContract;
import com.weatherradar.dinjection.ApiComponent;
import com.weatherradar.map.UserLocationUtils;
import com.weatherradar.map.WeatherMap;
import com.weatherradar.networking.ApiUtils;
import com.weatherradar.networking.ServerApi;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int WEATHER_LOADER_ID = 1;

    private static final String LATITUDE_KEY = "latitudeKey";
    private static final String LONGITUDE_KEY = "longitudeKey";

    @Inject
    public ServerApi mServerApi;

    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dependencyInject();
        showMap();
        restoreUserPosition(savedInstanceState);
        initLoader();
    }

    private void dependencyInject() {
        ApiComponent apiComponent = ((CustomApplication) getApplication())
                .getApiComponent();
        apiComponent.inject(this);
    }

    private void showMap() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.map_container);
        if (fragment == null) {

            FragmentTransaction transition = getFragmentManager().beginTransaction();
            transition.replace(R.id.map_container, new WeatherMap());
            transition.commit();
        }
    }

    private void restoreUserPosition(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLatitude = savedInstanceState.getDouble(LATITUDE_KEY);
            mLongitude = savedInstanceState.getDouble(LONGITUDE_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(LATITUDE_KEY, mLatitude);
        outState.putDouble(LONGITUDE_KEY, mLongitude);
    }

    private void initLoader() {
        getSupportLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.map_container);
        WeatherMap weatherMap = (WeatherMap) fragment;
        weatherMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ProviderContract.LOCATION_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            double currentLatitude = data.getDouble(data.getColumnIndex(DBContract.Location.LATITUDE));
            double currentLongitude = data.getDouble(data.getColumnIndex(DBContract.Location.LONGITUDE));

            if (isLocationTheSame(currentLatitude, currentLongitude)) {
                return;
            } else {
                rememberNewLocation(currentLatitude, currentLongitude);
            }
            getForecast(currentLatitude, currentLongitude);
        }
    }

    private boolean isLocationTheSame(double currentLatitude, double currentLongitude) {
        return UserLocationUtils.doubletEquals(mLatitude, currentLatitude) &&
                UserLocationUtils.doubletEquals(mLongitude, currentLongitude);
    }

    private void rememberNewLocation(double currentLatitude, double currentLongitude) {
        mLatitude = currentLatitude;
        mLongitude = currentLongitude;
    }

    private void getForecast(double latitude, double longitude) {
        ApiUtils.getForecast(mServerApi, getApplicationContext(), latitude, longitude);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // empty
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                getForecast(mLatitude, mLongitude);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
