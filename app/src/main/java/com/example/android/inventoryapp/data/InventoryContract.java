package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Niina on 18.7.2017.
 */

public class InventoryContract {

    //For the Content Provider:
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    //The scheme; will be shared by every URI associated with InventoryContract:
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Path for each of the tables:
    public static final String PATH_PRODUCTS = "products";

    private InventoryContract() {
    }

    public static final class ProductEntry implements BaseColumns {

        //Table name:
        public static final String TABLE_NAME = "products";
        //Table's column names:
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BRAND = "brand";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_IMAGE = "image";


        //A full URI for the class:
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // The MIME type of the 'CONTENT_URI' for a all product:
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // The MIME type of the CONTENT_URI for one item:
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    }
}



