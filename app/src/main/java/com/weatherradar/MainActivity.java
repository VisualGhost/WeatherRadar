package com.weatherradar;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.map_container);
        if (fragment == null) {

            FragmentTransaction transition = getFragmentManager().beginTransaction();
            transition.replace(R.id.map_container, new WeatherMap());
            transition.commit();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.map_container);
        WeatherMap weatherMap = (WeatherMap) fragment;
        weatherMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
