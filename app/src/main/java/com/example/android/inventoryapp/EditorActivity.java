package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.inventoryapp.data.InventoryContract.ProductEntry;


/**
 * Created by Niina on 19.7.2017.
 */
//Creating or editing data:
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //Product data loader's id:
    private static final int EXISTING_PRODUCT_LOADER = 0;
    static final int PRODUCT_IMAGE = 1;
    Uri imageUri;
    String currentPhotoPath;


    public int quantity;
    //Product's Content Uri; 'null' if new product:
    private Uri mCurrentProductUri;
    //EditText fields for the product's data; name, brand, price, quantity
    private EditText mNameEditText;
    private EditText mBrandEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private Button mIncrementButton;
    private Button mDecrementButton;
    private Button mOrderButton;

    private ImageView mImageView;

    private String nameString;
    private String brandString;
    private Double priceString;
    private int quantityString;
    private String imageString;

    //Checking if the product data is edited ('true' = yes, 'false' = no):
    private boolean mProductHasChanged = false;

    //OnTouchListener for monitoring possible 'touches' = modifications -> 'mProductHasChanged' = true:
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting the view to be the activity_editor.xml (the one with details):
        setContentView(R.layout.activity_editor);

        //Checking the intent to see if we're creating a new product or editing an existing one:
        Intent intent = getIntent();
        //This has value if existing product, 'null' if new product:
        mCurrentProductUri = intent.getData();

        //So following = new product in question:
        if (mCurrentProductUri == null) {
            setTitle("Add a product");
            //Hiding options menu_editor:
            invalidateOptionsMenu();
        } else {
            //And here editing an existing product:
            setTitle("Edit product");

            //Initializing loader to read the product's data & display values in the editor:
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        }

        //Find the EditTexts from where to read the user's data:
        mNameEditText = (EditText) findViewById(R.id.name_edittext);
        mBrandEditText = (EditText) findViewById(R.id.brand_edittext);
        mPriceEditText = (EditText) findViewById(R.id.price_edittext);
        mPriceEditText.setText("0");
        mQuantityEditText = (EditText) findViewById(R.id.amount_edittext);
        mQuantityEditText.setText("0");
        mImageView = (ImageView) findViewById(R.id.image_imageview);

        // Set placeholder image for image view
        mImageView.setImageResource(R.drawable.image_placeholder);


        //Then add OnTouchListeners to know if the user has touched / edited them.
        mNameEditText.setOnTouchListener(mTouchListener);
        mBrandEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

        mIncrementButton = (Button) findViewById(R.id.increment_button);
        mDecrementButton = (Button) findViewById(R.id.decrement_button);

        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
            }
        });

        //This is setting the 'order' button visible if an existing product in question:
        if (mCurrentProductUri != null) {
            mOrderButton = (Button) findViewById(R.id.submit_order_button);
            mOrderButton.setVisibility(View.VISIBLE);
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitOrder();
                }
            });
        }

        /**
         * on click listener for launching method to capture image
         */
        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // check there is a camera available
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // create new image file
                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                    } catch (IOException exception) {

                    }
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.android.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(takePictureIntent, PRODUCT_IMAGE);
                    }
                }
            }
        });

    }

    /**
     * Create image file for each image
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMMMdd_hhmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (imageUri != null) {
            mImageView.setImageURI(imageUri);
        } else {
            mImageView.setImageResource(R.drawable.image_placeholder);
        }
    }

    //Saving the product: Get user input & save it into db:
    private void saveProduct() {

        if (checkInputsOk()) {

            String nameString = mNameEditText.getText().toString().trim();
            String brandString = mBrandEditText.getText().toString().trim();
            Double priceString = Double.parseDouble(mPriceEditText.getText().toString().trim());

            int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());

            if (imageUri != null) {
                imageString = imageUri.toString();
            } else {
                imageString = null;
                mImageView.setImageResource(R.drawable.image_placeholder);


                //ContentValues object: Column names = keys, values = editor's attributes:
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_NAME, nameString);
                values.put(ProductEntry.COLUMN_BRAND, brandString);
                values.put(ProductEntry.COLUMN_PRICE, priceString);
                values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                values.put(ProductEntry.COLUMN_IMAGE, imageString);

                //Checking if URI is null = new product:
                if (mCurrentProductUri == null) {
                    //New product; insert new product into provider + return its cont.Uri:
                    Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                    // Toast message of insertion successful / failed:
                    if (newUri == null) {
                        // New product's URI = null = error with adding
                        Toast.makeText(this, getString(R.string.toast_problem_adding_product),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.toast_adding_product_ok),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    //This is when URI is not null = product is existing.
                    //Therefore updating it with content uri; mCurrentProductUri + new values
                    int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                    // Toast message of update being successful / failed:
                    if (rowsAffected == 0) {
                        // Updated product's rows nonchanged = null = error with updating
                        Toast.makeText(this, getString(R.string.toast_problem_updating_product),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.toast_updating_product_ok),
                                Toast.LENGTH_SHORT).show();

                    }

                }
                finish();

            }

        }
    }

    // Adding menu items to the bar (save & delete):
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor,
                menu);
        return true;
    }

//Called after 'invalidateOptionsMenu();
    //Some items to visible/invisible

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //New product = hide 'delete':
        if (mCurrentProductUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    //This is for the the menu bar's (save/delete) buttons:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //'Save' button clicked:
            case R.id.action_save:
                if (mProductHasChanged) {
                    saveProduct();

                } else {

                    Toast.makeText(this, "Nothing changed", Toast.LENGTH_SHORT).show();
                }
                return true;
            //'Delete' button clicked:
            case R.id.action_delete:
                //Shows the 'are you sure...' message:
                showDeleteConfirmationDialog();
                return true;
            //'Up - arrow' button clicked:
            case android.R.id.home:
                //If no changes made:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                } else {
                    //If changes HAS BEEN made: onClickListener for user's responses
                    DialogInterface.OnClickListener discardButtonListener =
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //'Discard' button clicked:
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };
                    //Dialog showed about unsaved changes:
                    showUnsavedChangesDialog(discardButtonListener);
                    return true;

                }
        }
        return super.onOptionsItemSelected(item);
    }

    //'Back' button clicked:
    @Override
    public void onBackPressed() {

        //No changes made:
        if (!mProductHasChanged) {
            super.onBackPressed();
        }
        //Here changes HAS BEEN made; same as above: inform about unsaved changes:
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //'Discard' button clicked; close the curr. activity:
                        finish();
                    }
                };
        //Dialog showed about unsaved changes:
        showUnsavedChangesDialog(discardButtonClickListener);


    }

    //Dialog about unsaved changes with 'discardButtonClickListener':

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("There are unsaved changes; are you sure you want to exit editing without saving? ");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //'Keep editing' button clicked:
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog:
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Dialog about deleting product from db:
    private void showDeleteConfirmationDialog() {
        //Create and show the AlertDialog:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete product? ");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //'Delete' button clicked:
                deleteProduct();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //'Cancel' button clicked: Continue editing:
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Deleting product from the database:
    private void deleteProduct() {
        //Checking if product's Uri exists = existing product that can be deleted:
        if (mCurrentProductUri != null) {
            //Deleting the uri, null for selection + selectArgs (because the Uri deletes the whole row anyway):
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            //Toast messages whether delete was successful / not:
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.toast_problem_deleting_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_deleting_product_ok), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity:
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Editor has all the attributes, therefore projection will have too:
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_BRAND,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_IMAGE};
        //Loader that executes ContentProvider's query method on a background thread:
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    //Setting data on the EditText views:
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Exit if no data in cursor / less than 1 row:
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        //Data exists. Now starting to read data from the cursor:
        if (cursor.moveToFirst()) {
            //Finding the columns:
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
            int brandColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_BRAND);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE);


            //Extract their values:
            String name = cursor.getString(nameColumnIndex);
            String brand = cursor.getString(brandColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            //Update the fields with their data:
            mNameEditText.setText(name);
            mBrandEditText.setText(brand);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));

            // null exception for image; update if image provided
            if (image != null) {
                imageUri = Uri.parse(image);
                mImageView.setImageURI(imageUri);
            } else {
                mImageView.setImageResource(R.drawable.image_placeholder);
            }


        }
    }

    //Clearing all the data:
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBrandEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");


    }


    public void increment() {
        quantity = Integer.valueOf(mQuantityEditText.getText().toString());

        quantity++;
        mQuantityEditText.setText(String.valueOf(quantity));
    }

    public void decrement() {
        quantity = Integer.valueOf(mQuantityEditText.getText().toString());

        if (quantity > 0) {
            quantity--;
            mQuantityEditText.setText(String.valueOf(quantity));
        } else {
            Toast.makeText(this, getString(R.string.toast_quantity_0), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitOrder() {

        String nameMessage = mNameEditText.getText().toString();
        String brandMessage = mBrandEditText.getText().toString();
        String priceMessage = mPriceEditText.getText().toString();
        int quantityMessage = Integer.valueOf(mQuantityEditText.getText().toString());

        String message = getString(R.string.order_message_part1) + nameMessage + ", "
                + brandMessage
                + getString(R.string.order_message_part2) + quantityMessage
                + getString(R.string.order_message_part3) + priceMessage
                + getString(R.string.order_message_part4);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_subject) + nameMessage);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);

        }

    }

    public boolean checkInputsOk() {
        //Read the input + trim possible white spaces:
        String nameString = mNameEditText.getText().toString().trim();
        String brandString = mBrandEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        String quantityString = mQuantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.toast_invalid_name), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(brandString)) {
            Toast.makeText(this, getString(R.string.toast_invalid_brand), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(priceString) || (Double.valueOf(priceString)) <= 0.0) {
            Toast.makeText(this, getString(R.string.toast_invalid_price), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(quantityString) || Integer.valueOf(quantityString) <= 0) {
            Toast.makeText(this, getString(R.string.toast_invalid_quantity), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}

