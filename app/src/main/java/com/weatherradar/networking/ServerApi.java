package com.weatherradar.networking;


import com.weatherradar.networking.model.Forecast;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServerApi {

    @GET("data/2.5/find")
    Observable<Forecast> getForecastObservable(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("units") String units,
            @Query("cnt") String cityNumber,
            @Query("APPID") String apiKey
    );
}
