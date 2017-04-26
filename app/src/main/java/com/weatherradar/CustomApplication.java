package com.weatherradar;


import android.app.Application;

import com.weatherradar.dinjection.ApiComponent;
import com.weatherradar.dinjection.DaggerApiComponent;
import com.weatherradar.networking.RestClientProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomApplication extends Application {

    private ApiComponent mApiComponent;
    private Executor mDbExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        mApiComponent = DaggerApiComponent.builder().build();
        setRestClientBaseUrl(mApiComponent.getRestClient());
        mDbExecutor = Executors.newSingleThreadExecutor();
    }

    private void setRestClientBaseUrl(RestClientProvider restClientProvider) {
        restClientProvider.setBaseUrl(BuildConfig.BASE_URL);
    }

    public ApiComponent getApiComponent() {
        return mApiComponent;
    }

    public Executor getDbExecutor() {
        return mDbExecutor;
    }

}
