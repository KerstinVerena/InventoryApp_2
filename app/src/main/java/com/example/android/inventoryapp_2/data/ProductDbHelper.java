package com.example.android.inventoryapp_2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper class for the Inventory app. Creates and manages the database.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    // --Commented out by Inspection (20.08.2018 19:43):public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    //The name of the database.
    private static final String DATABASE_NAME = "store.db";


    //Database version. If you change the database schema, you must increment the database version.

    private static final int DATABASE_VERSION = 1;


    /**
     * Construct a new instance of the DbHelper
     *
     * @param context of the app
     */

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductRecord.ProductEntry.TABLE_NAME + " ("
                + ProductRecord.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductRecord.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductRecord.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ProductRecord.ProductEntry.COLUMN_PRODUCT_QUANTITY +
                " INTEGER NOT NULL DEFAULT 0, "
                + ProductRecord.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT, "
                + ProductRecord.ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT + " TEXT);";


        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

}
