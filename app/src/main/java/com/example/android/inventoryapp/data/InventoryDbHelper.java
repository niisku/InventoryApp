package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by Niina on 18.7.2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";
    public static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //When the db is created FIRST time:
    @Override
    public void onCreate(SQLiteDatabase db) {

        //String that contains the SQL statement to create the pets table:
        String SQL_CREATE_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME +
                " (" + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_BRAND + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                ProductEntry.COLUMN_IMAGE + " TEXT NOT NULL);";
        //Executing that SQL statement:
        db.execSQL(SQL_CREATE_TABLE);
    }

    //This is called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
