package com.weatherradar.networking;


import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.weatherradar.BuildConfig;
import com.weatherradar.CustomApplication;
import com.weatherradar.content_provider.ProviderContract;
import com.weatherradar.database.DBContract;
import com.weatherradar.networking.model.Forecast;
import com.weatherradar.networking.model.GeoSpot;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class ApiUtils {

    private static final String TAG = ApiUtils.class.getSimpleName();

    private static final String METRIC = "metric";
    private static final String SPOTS_NUMBER = "50";

    private ApiUtils(){
        // hide
    }

    public static void getForecast(ServerApi serverApi, final Context context, double latitude, double longitude) {
        serverApi.getForecastObservable(
                String.valueOf(latitude),
                String.valueOf(longitude),
                METRIC,
                SPOTS_NUMBER,
                BuildConfig.API_KEY
        ).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(((CustomApplication) context).getDbExecutor()))
                .subscribe(new ResourceObserver<Forecast>() {
                    @Override
                    public void onNext(Forecast forecast) {
                        try {
                            applyBatch(context, forecast);
                        } catch (RemoteException | OperationApplicationException e) {
                            Log.e(TAG, e.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // empty
                    }

                    @Override
                    public void onComplete() {
                        // empty
                    }
                });

    }

    private static void applyBatch(Context context, Forecast forecast) throws RemoteException, OperationApplicationException {
        if (forecast != null) {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation
                    .newDelete(ProviderContract.FORECAST_URI).build());

            List<GeoSpot> geoSpotList = forecast.getGeoSpotList();
            if (geoSpotList != null && geoSpotList.size() > 0) {
                for (GeoSpot geoSpot : geoSpotList) {
                    ContentProviderOperation contentProviderOperation =
                            ContentProviderOperation.newInsert(ProviderContract.FORECAST_URI)

                                    .withValue(DBContract.Forecast.NAME, geoSpot.getName())
                                    .withValue(DBContract.Forecast.LATITUDE, geoSpot.getCoordinates().getLatitude())
                                    .withValue(DBContract.Forecast.LONGITUDE, geoSpot.getCoordinates().getLongitude())
                                    .withValue(DBContract.Forecast.TEMPERATURE, geoSpot.getWeatherData().getTemperature())
                                    .build();

                    operations.add(contentProviderOperation);
                }
            }

            context.getContentResolver().applyBatch(ProviderContract.CONTENT_AUTHORITY, operations);

        }
    }

}
