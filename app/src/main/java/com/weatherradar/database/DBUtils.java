package com.weatherradar.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.weatherradar.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class DBUtils {

    private DBUtils() {
        // hide
    }

    private static final String TAG = DBUtils.class.getSimpleName();

    /**
     * Executes on db the SQL statement from the file "filePrefix.db.version.sql"
     *
     * @param databaseName e.g. "mydatabase.db"
     * @param version      The version of database
     */
    public static void getSQLStatement(SQLiteDatabase database, Context context, String databaseName, int version) {
        BufferedReader reader = null;

        try {
            String filename = String.format(Locale.US, "%s.%d.sql", databaseName, version);
            final InputStream inputStream = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            final StringBuilder statement = new StringBuilder();

            for (String line; (line = reader.readLine()) != null; ) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Reading line -> " + line);
                }

                // Ignore empty lines
                if (!TextUtils.isEmpty(line) && !line.startsWith("--")) {
                    statement.append(line.trim());
                }

                if (line.endsWith(";")) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Running statement " + statement);
                    }

                    database.execSQL(statement.toString());
                    statement.setLength(0);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not apply SQL file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "Could not close reader", e);
                }
            }
        }
    }
}
