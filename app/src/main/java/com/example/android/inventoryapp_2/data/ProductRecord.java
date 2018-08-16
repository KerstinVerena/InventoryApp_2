package com.example.android.inventoryapp_2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * The table and column names to set up the database.
 */

public final class ProductRecord {

    /**
     * The "Content authority" is a string referring to the  package name of the app.
     */

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp_2";
    /**
     * Combine the CONTENT_AUTHORITY to create a string used as base for all URI's which apps
     * will use to contact the content provider.
     */

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * This app only has one path called "products". The paths are appended to the BASE_CONTEN_URI.
     */
    public static final String PATH_PRODUCTS = "products";

    private ProductRecord() {
    }

    public static abstract class ProductEntry implements BaseColumns {

        /**
         * Appended path to base content URI for possible URI's).
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for the list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_PRODUCT_SUPPLIER_CONTACT = "supplier_contact_number";
    }

}
