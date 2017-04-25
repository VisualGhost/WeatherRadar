package com.weatherradar;


import android.app.Application;

import com.weatherradar.dinjection.ApiComponent;
import com.weatherradar.dinjection.DaggerApiComponent;
import com.weatherradar.networking.RestClientProvider;

public class CustomApplication extends Application {

    private ApiComponent mApiComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mApiComponent = DaggerApiComponent.builder().build();
        setRestClientBaseUrl(mApiComponent.getRestClient());
    }

    private void setRestClientBaseUrl(RestClientProvider restClientProvider) {
        restClientProvider.setBaseUrl(BuildConfig.BASE_URL);
    }

    public ApiComponent getApiComponent() {
        return mApiComponent;
    }
}
