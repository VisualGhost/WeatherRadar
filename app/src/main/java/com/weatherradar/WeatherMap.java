package com.weatherradar;


import android.Manifest;
import android.content.pm.PackageManager;
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

import static android.content.Context.LOCATION_SERVICE;

public class WeatherMap extends MapFragment implements OnMapReadyCallback,
        LocationSource, LocationListener {

    private static final String STATE_IN_PERMISSION = "inPermission";
    private static final int REQUEST_PERMS = 1337;

    private boolean isInPermission;
    private OnLocationChangedListener mapLocationListener;
    private Criteria mCriteria;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    public WeatherMap() {
        mCriteria = new Criteria();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isInPermission =
                    savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);
        }
        applyPermissions(canGetLocation());
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
                getActivity().finish(); // denied permission, so we're done
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

            LatLng latlng =
                    new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cu = CameraUpdateFactory.newLatLng(latlng);

            mMap.animateCamera(cu);
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
}
