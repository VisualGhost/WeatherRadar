package com.weatherradar.dinjection;


import com.weatherradar.MainActivity;
import com.weatherradar.WeatherMap;
import com.weatherradar.networking.RestClientProvider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApiModule.class)
public interface ApiComponent {

    RestClientProvider getRestClient();

    void inject(WeatherMap weatherMap); // TODO change injectable

}
