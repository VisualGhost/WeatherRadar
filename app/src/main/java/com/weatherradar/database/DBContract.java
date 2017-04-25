package com.weatherradar.database;


public class DBContract {

    private DBContract() {
        // hide
    }

    public interface Table {
        String LOCATION = "location";
        String FORECAST = "beer";
    }

    public interface Location {
        String ID = "id";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
    }

    public interface Forecast {
        String ID = "id";
        String NAME = "name";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String TEMPERATURE = "temperature";
    }

}
