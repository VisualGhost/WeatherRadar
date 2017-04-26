package com.weatherradar;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.weatherradar.content_provider.ProviderContract;
import com.weatherradar.database.DBContract;
import com.weatherradar.dinjection.ApiComponent;
import com.weatherradar.map.UserLocationManager;
import com.weatherradar.map.WeatherMap;
import com.weatherradar.networking.ServerApi;
import com.weatherradar.networking.model.Forecast;
import com.weatherradar.networking.model.GeoSpot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WEATHER_LOADER_ID = 1;
    private static final String METRIC = "metric";
    private static final String SPOTS_NUMBER = "50";

    private static final String LATITUDE_KEY = "latitudeKey";
    private static final String LONGITUDE_KEY = "longitudeKey";

    @Inject
    public ServerApi mServerApi;

    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiComponent apiComponent = ((CustomApplication) getApplication())
                .getApiComponent();
        apiComponent.inject(this);

        setContentView(R.layout.activity_main);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.map_container);
        if (fragment == null) {

            FragmentTransaction transition = getFragmentManager().beginTransaction();
            transition.replace(R.id.map_container, new WeatherMap());
            transition.commit();
        }

        if (savedInstanceState != null) {
            mLatitude = savedInstanceState.getDouble(LATITUDE_KEY);
            mLongitude = savedInstanceState.getDouble(LONGITUDE_KEY);
        }

        getSupportLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(LATITUDE_KEY, mLatitude);
        outState.putDouble(LONGITUDE_KEY, mLongitude);
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

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Current location: " + currentLatitude + ", " + currentLongitude);
            }

            if (isLocationTheSame(currentLatitude, currentLongitude)) {
                return;
            } else {
                rememberNewLocation(currentLatitude, currentLongitude);
            }
            getForecast(currentLatitude, currentLongitude);
        }
    }

    private boolean isLocationTheSame(double currentLatitude, double currentLongitude) {
        return UserLocationManager.doubletEquals(mLatitude, currentLatitude) &&
                UserLocationManager.doubletEquals(mLongitude, currentLongitude);
    }

    private void rememberNewLocation(double currentLatitude, double currentLongitude) {
        mLatitude = currentLatitude;
        mLongitude = currentLongitude;
    }

    private void getForecast(double latitude, double longitude) {
        mServerApi.getForecastObservable(
                String.valueOf(latitude),
                String.valueOf(longitude),
                METRIC,
                SPOTS_NUMBER,
                BuildConfig.API_KEY
        ).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(((CustomApplication) getApplication()).getDbExecutor()))
                .subscribe(new ResourceObserver<Forecast>() {
                    @Override
                    public void onNext(Forecast forecast) {
                        try {
                            applyBatch(forecast);
                        } catch (RemoteException | OperationApplicationException e) {
                            Log.e(TAG, e.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // empty
                    }

                    @Override
                    public void onComplete() {
                        // empty
                    }
                });

    }

    private void applyBatch(Forecast forecast) throws RemoteException, OperationApplicationException {
        if (forecast != null) {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation
                    .newDelete(ProviderContract.FORECAST_URI).build());

            List<GeoSpot> geoSpotList = forecast.getGeoSpotList();
            if (geoSpotList != null && geoSpotList.size() > 0) {
                for (GeoSpot geoSpot : geoSpotList) {
                    ContentProviderOperation contentProviderOperation =
                            ContentProviderOperation.newInsert(ProviderContract.FORECAST_URI)

                                    .withValue(DBContract.Forecast.NAME, geoSpot.getName())
                                    .withValue(DBContract.Forecast.LATITUDE, geoSpot.getCoordinates().getLatitude())
                                    .withValue(DBContract.Forecast.LONGITUDE, geoSpot.getCoordinates().getLongitude())
                                    .withValue(DBContract.Forecast.TEMPERATURE, geoSpot.getWeatherData().getTemperature())
                                    .build();

                    operations.add(contentProviderOperation);
                }
            }

            getApplicationContext().getContentResolver().applyBatch(ProviderContract.CONTENT_AUTHORITY, operations);

        }
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
