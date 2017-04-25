package com.weatherradar.networking.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecast {

    @SerializedName("list")
    private List<GeoSpot> mGeoSpotList;

    public List<GeoSpot> getGeoSpotList() {
        return mGeoSpotList;
    }
}
