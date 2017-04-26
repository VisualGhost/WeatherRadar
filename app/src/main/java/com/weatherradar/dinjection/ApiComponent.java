package com.weatherradar.dinjection;


import com.weatherradar.MainActivity;
import com.weatherradar.networking.RestClientProvider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApiModule.class)
public interface ApiComponent {

    RestClientProvider getRestClient();

    void inject(MainActivity mainActivity); // TODO change injectable

}
