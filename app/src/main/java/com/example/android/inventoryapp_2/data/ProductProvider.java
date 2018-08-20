package com.example.android.inventoryapp_2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.security.Provider;

import com.example.android.inventoryapp_2.data.ProductRecord.ProductEntry;

public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the product table
     */
    private static final int PRODUCTS = 100;
    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 101;
    /**
     * Create an UriMatcher object to match a content URI to corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // All of the content URI patterns that the provide should recognize are added with addURI.

        sUriMatcher.addURI(ProductRecord.CONTENT_AUTHORITY, ProductRecord.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductRecord.CONTENT_AUTHORITY, ProductRecord.PATH_PRODUCTS +
                "/#", PRODUCT_ID);
    }

    /**
     * Initialize DbHelper
     */
    public ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get a readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Query the products table directly with the given projection, selection,
                // selection arguments, and sort order. The cursor could contain multiple
                // rows of the table.

                cursor = database.query(ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);

                break;
            case PRODUCT_ID:
                // Extract out the ID from the URI.

                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the product table to return a Cursor containing the
                // specific row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductRecord.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductRecord.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a new product into the database and return the new content URI for the specific
     * row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        //Get a database to write information into it.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Check if valuable information has been entered.
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name.length() == 0) {
            throw new IllegalArgumentException("No name entered for product");
        }
        //Check if a valuable price has been entered into the database.

        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Insert the new product with the given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        //Get a writeable database.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Check if a valiated uri was given.
        if (uri == null) {
            throw new IllegalArgumentException("Error erasing product");
        }

        //Delete a single product from the inventory database.
        s = ProductEntry._ID + "=?";


        strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
        getContext().getContentResolver().notifyChange(uri, null);
        return database.delete(ProductEntry.TABLE_NAME, s, strings);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String
            selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // Extract the ID from the URI, so we know which row to update.
                // Selection will be "_id=?" and selection arguments will be a String array
                // containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the product name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("No name entered for product");
            }
        }

        // Check that the price value is valid. It should neither be null nur below 0.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of database rows affected by the update statement
        return database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
