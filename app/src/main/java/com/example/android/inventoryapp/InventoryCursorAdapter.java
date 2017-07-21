package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.inventoryapp.data.InventoryContract.ProductEntry;


/**
 * Created by Niina on 19.7.2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //Inflating list item view with list_item.xml:
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //Binding together the data to its correct views (and vice versa)
    //view = newView()'s returned view
    //context = app context
    //cursor = has the data

    //Reminder: list_view has following views to populate: name, price, & amount:
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //First find the views:
        TextView nameView = (TextView) view.findViewById(R.id.name_data_textview_main);
        TextView priceView = (TextView) view.findViewById(R.id.price_data_textview_main);
        TextView amountView = (TextView) view.findViewById(R.id.amount_data_textview_main);

        //Find their columns from db:
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int amountColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);

        //Get the values of columns:
        String productName = cursor.getString(nameColumnIndex);
        double productPrice = cursor.getDouble(priceColumnIndex);
        final int productAmount = cursor.getInt(amountColumnIndex);

        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID)));

        //Bind the views & their values together:
        nameView.setText(productName);
        priceView.setText(Double.toString(productPrice));
        amountView.setText(Integer.toString(productAmount));

        //'Sell' button; reduce 1 from quantity (as long as the number is > 0:
        Button sellButton = (Button) view.findViewById(R.id.sale_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productAmount > 0) {
                    int newAmount = productAmount - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, newAmount);
                    context.getContentResolver().update(uri, values, null, null);

                } else {
                    Toast.makeText(context, context.getString(R.string.toast_quantity_0), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}