package com.weatherradar.networking.model;


import com.google.gson.annotations.SerializedName;

public class GeoSpot {

    @SerializedName("name")
    private String mName;

    @SerializedName("coord")
    private Coordinates mCoordinates;

    @SerializedName("main")
    private WeatherData mWeatherData;

    public String getName() {
        return mName;
    }

    public Coordinates getCoordinates() {
        return mCoordinates;
    }

    public WeatherData getWeatherData() {
        return mWeatherData;
    }
}
