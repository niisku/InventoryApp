package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.android.inventoryapp.data.InventoryContract.ProductEntry;


/**
 * Created by Niina on 18.7.2017.
 */

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = ProductEntry.class.getSimpleName();

    //URI matcher code for the whole table:
    private static final int PRODUCTS = 100;

    //URI matcher code for a single item in table:
    private static final int PRODUCT_ID = 101;

    //UriMatcher object: Input = Code returned for the root URI
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //Uri matcher codes:
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    //Db helper object:
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Set db to readable:
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        //Query results:
        Cursor cursor;
        //Checking the match for query; to the whole table, to a single item, or none.
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                //Code for whole table:
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                //1st: Extract the ID; will be the 'selection' = '_id=?':
                selection = ProductEntry._ID + "=?";
                //2nd: Selection argument: String array of value of the '?':
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                //Same as before, but now more 'detailed'; with seletion & selectionArgs:
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            //If match is not found to either of above:
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor = the queried results:
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        //Again: Checking the match for query; to the whole table, to a single item, or none.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //Again: Checking the match for query; to the whole table, to a single item, or none.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    //Inserting a product with given content values; Returning it's row:
    private Uri insertProduct(Uri uri, ContentValues values) {

        //Checking always first that the product's information is not missing:
        String name = values.getAsString(ProductEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product's name is missing. ");
        }

        String brand = values.getAsString(ProductEntry.COLUMN_BRAND);
        if (brand == null) {

            throw new IllegalArgumentException("Product's brand is missing. ");
        }

        Float price = values.getAsFloat(ProductEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Product's price is missing. ");
        }

        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product's price is missing or below 0. ");
        }

        String image = values.getAsString(ProductEntry.COLUMN_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Product's image is missing. ");
        }
        //Get writable db:
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Inserting a new product with its values:
        long id = db.insert(ProductEntry.TABLE_NAME, null, values);
        //If id = -1, the insertion has failed. Log error + return null:
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notifying all listeners that the data has changed in the product's content URI:
        getContext().getContentResolver().notifyChange(uri, null);
        //Returning the new URI with the row's ID:
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //Again: Db to writable:
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //Number of deleted rows:
        int rowsDeleted;

        //Again: Checking the match for query; to the whole table, to a single item, or none.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                //Delete rows based on seletion & selection args:
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
            case PRODUCT_ID:
                //Delete one row based on the ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Not able to delete " + uri);
        }

        //Notifying all listeners about changes (if at least 1 row was deleted):
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //Returning number of affected rows:
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //Again: Checking the match for query; to the whole table, to a single item, or none.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not successful for " + uri);
        }
    }

    //Update db, values based on selection & selectionArgs
    //Return = int of affected rows:
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //Checking that the product's keys are not null:
        if (values.containsKey(ProductEntry.COLUMN_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product's name is missing. ");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_BRAND)) {
            String brand = values.getAsString(ProductEntry.COLUMN_BRAND);
            if (brand == null) {
                throw new IllegalArgumentException("Product's brand is missing. ");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Product's price is missing. ");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Product's price is missing or below 0. ");
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_IMAGE)) {
            String image = values.getAsString(ProductEntry.COLUMN_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Product's image is missing. ");
            }
        }
        //If no values to update (= 0 rows affected), no updating happens:
        if (values.size() == 0) {
            return 0;
        }

        //Otherwise, start updating.
        //First again, get writable db:
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Make the update & return value is number of rows affected:
        int rowsAffected = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        //Informing all listeners about update (if at least 1 row was affected):
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //Returning number of affected rows:
        return rowsAffected;

    }
}
