package com.weatherradar.map;


import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.weatherradar.R;
import com.weatherradar.content_provider.ProviderContract;
import com.weatherradar.database.DBContract;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class WeatherMap extends MapFragment implements OnMapReadyCallback,
        LocationSource, LocationListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_IN_PERMISSION = "inPermission";
    private static final int REQUEST_PERMS = 1337;
    private static final int FORECAST_LOADER_ID = 1;

    private boolean isInPermission;
    private OnLocationChangedListener mapLocationListener;
    private Criteria mCriteria;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private List<MarkerData> mMarkerDataList;
    private LatLngBounds.Builder mBuilder;


    public WeatherMap() {
        mCriteria = new Criteria();
        mMarkerDataList = new ArrayList<>();
        mBuilder = new LatLngBounds.Builder();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isInPermission =
                    savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);
        }
        applyPermissions(canGetLocation());

        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        isInPermission = false;

        if (requestCode == REQUEST_PERMS) {
            if (canGetLocation()) {
                applyPermissions(true);
            } else {
                getActivity().finish();
            }
        }
    }

    private boolean canGetLocation() {
        return (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private void applyPermissions(boolean canGetLocation) {
        if (canGetLocation) {
            getMapAsync(this);

        } else if (!isInPermission) {
            isInPermission = true;


            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMS);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        mLocationManager = (LocationManager) getContext()
                .getSystemService(LOCATION_SERVICE);
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        requestLocationUpdates();

        map.setLocationSource(this);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        if (mMarkerDataList != null) {
            for (MarkerData data : mMarkerDataList) {
                addMarker(map, data.latitude, data.longitude, data.name, data.temperature);
            }
            mMarkerDataList.clear();
        }

        showAllMarkers(map);

    }

    private void showAllMarkers(final GoogleMap map) {
        if (getView() != null) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int minLength = Math.min(width, height);
                    int padding = minLength >> 2;
                    CameraUpdate cameraUpdate = CameraUpdateFactory
                            .newLatLngBounds(mBuilder.build(), width, height, padding);
                    map.moveCamera(cameraUpdate);
                }
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(0L, 0.0f, mCriteria, this, null);
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mapLocationListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mapLocationListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mapLocationListener != null) {
            mapLocationListener.onLocationChanged(location);

            UserLocationManager.storeCurrentLocation(getContext().getApplicationContext(), location);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // unused
    }

    @Override
    public void onProviderEnabled(String s) {
        // unused
    }

    @Override
    public void onProviderDisabled(String s) {
        // unused
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        requestLocationUpdates();

        if (mMap != null) {
            mMap.setLocationSource(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            mMap.setLocationSource(null);
        }

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), ProviderContract.FORECAST_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (mMap != null) {
            mMap.clear();
            if (cursor != null) {
                convertCursorToMarkers(cursor);
            }
        } else {
            convertCursorToList(cursor);
        }
    }

    private void convertCursorToMarkers(Cursor cursor) {
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            double latitude = getLatitude(cursor);
            double longitude = getLongitude(cursor);
            String name = getName(cursor);
            String temperature = getTemperature(cursor);
            addMarker(mMap, latitude, longitude, name, temperature);
        }
    }

    private double getLatitude(Cursor cursor) {
        return cursor.getDouble(cursor.getColumnIndex(DBContract.Forecast.LATITUDE));
    }

    private double getLongitude(Cursor cursor) {
        return cursor.getDouble(cursor.getColumnIndex(DBContract.Forecast.LONGITUDE));
    }

    private String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(DBContract.Forecast.NAME));
    }

    private String getTemperature(Cursor cursor) {
        String temperature = cursor.getString(cursor.getColumnIndex(DBContract.Forecast.TEMPERATURE));
        return getString(R.string.celsius, temperature);
    }

    private void convertCursorToList(Cursor cursor) {
        mMarkerDataList.clear();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            double latitude = getLatitude(cursor);
            double longitude = getLongitude(cursor);
            String name = getName(cursor);
            String temperature = getTemperature(cursor);

            MarkerData markerData = new MarkerData();
            markerData.latitude = latitude;
            markerData.longitude = longitude;
            markerData.name = name;
            markerData.temperature = temperature;

            mMarkerDataList.add(markerData);
        }
    }

    private static class MarkerData {
        double latitude;
        double longitude;
        String name;
        String temperature;
    }

    private void addMarker(GoogleMap map, double lat,
                           double lon, String title, String snippet) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title(title)
                .snippet(snippet));
        mBuilder.include(marker.getPosition());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // empty
    }
}
