package com.weatherradar.dinjection;

import com.weatherradar.networking.RestClientProvider;
import com.weatherradar.networking.ServerApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiModule {

    @Provides
    @Singleton
    public ServerApi provideServerApi(RestClientProvider restClientProvider) {
        return restClientProvider.getRetrofit().create(ServerApi.class);
    }
}
