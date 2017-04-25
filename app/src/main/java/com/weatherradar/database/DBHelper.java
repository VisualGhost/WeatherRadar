package com.weatherradar.database;


import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weatherdata.db";
    private static final int SCHEMA_VERSION = 1;

    private final Context mContext;

    public DBHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, SCHEMA_VERSION);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 1; i <= SCHEMA_VERSION; i++) {
            applySqlFile(db, i);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = (oldVersion + 1); i <= newVersion; i++) {
            applySqlFile(db, i);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void applySqlFile(SQLiteDatabase db, int version) {
        DBUtils.getSQLStatement(db, mContext, DATABASE_NAME, version);
    }
}
