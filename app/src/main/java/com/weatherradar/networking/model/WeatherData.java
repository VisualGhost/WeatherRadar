package com.weatherradar.networking.model;


import com.google.gson.annotations.SerializedName;

public class WeatherData {

    @SerializedName("temp")
    private double mTemperature;

    public double getTemperature() {
        return mTemperature;
    }
}
