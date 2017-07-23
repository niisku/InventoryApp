package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import static com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

//Displays the stored items:
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Loader id:
    private static final int PRODUCT_LOADER = 0;

    //ListView's adapter:
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup 'Add new' button to open EditorActivity:
        Button addNew = (Button) findViewById(R.id.add_new_button);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView:
        ListView productListView = (ListView) findViewById(R.id.list);

        //Find the empty view + set it on the ListView:
        View emptyView = (View) findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //Adapter: creates list item for a data row:
        //NOTE! 'null' is for the cursor, but is null because in the beginning there's no data:
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Setup item click listener for when products are clicked:
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //New intent to go to Editor Activity page:
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                //New Uri for the product, specified with the 'id' clicked
                //(= If clicked 2nd list item, Uri would be "content://com.example.android.inventoryapp/products/2":
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //Set the product's Uri to the intent's data field:
                intent.setData(currentProductUri);

                //Launch the EditorActivity page with the product's information:
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    //Define the projection to this page. Note that here showing only data defined in list_item:
    //name, price, quantity:
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_IMAGE
        };
        //ContentManager's query:
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    //Updating the adapter with new data:
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    //Delete loader's data:
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}
