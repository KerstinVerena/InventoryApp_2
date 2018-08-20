package com.example.android.inventoryapp_2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp_2.data.ProductDbHelper;
import com.example.android.inventoryapp_2.data.ProductRecord;
import com.example.android.inventoryapp_2.data.ProductRecord.ProductEntry;

/**
 * This activity gives the user the possibility to add a new product to the database as well
 * as to check the details and modify existing products.
 */
public class EditActivity extends AppCompatActivity implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditActivity.class.getName();


    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    //String to get the information entered in the dialogue when raising or lowering the quantity
    //via the buttons.
    String mQuantityAmountRaise;
    String mQuantityAmountLower;
    //String in which the supplier contact is stored.
    String productSupplierContact;
    //Integer for the product quantity.
    int productQuantity;
    // Content URI for already existing products (null if a new product is added).
    private Uri mCurrentProductUri;
    //EditText field to enter the product's name.
    private EditText mNameEditText;
    //EditText field to enter the product's price.
    private EditText mPriceEditText;
    //EditText field to enter the product's quantity.
    private TextView mQuantityEditText;
    //EditText field to enter the name of the product's supplier.
    private EditText mSupplierNameEditText;
    //EditText field to enter the phone number of the product's supplier.
    private EditText mSupplierContactEditText;
    //Check if entered information passed the sanity check.
    private Boolean mPassedSanityCheck = false;
    //Boolean to check if a product has been changed.
    private boolean mProductHasChanged = false;

    //Set up an onTouchListener to check if the user has changed any of the fields and if this is
    //the case set mProdcutHasChanged to true.
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
        setContentView(R.layout.activity_edit);

        //Check which intent was used to get to the activity, and retrieve data in case it was
        // provided.

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //If the intent does not contain a product content URI, we are creating a new product.
        // If it does, we edit an existing one.

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.insert_new_prodcut));
        } else {
            setTitle(getString(R.string.update_prodcut));

            //Get the information about the existing product.
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null,
                    this);
        }

        //Find all the relevant views for the data input.

        mNameEditText = findViewById(R.id.product_name);
        mPriceEditText = findViewById(R.id.product_price);
        mQuantityEditText = findViewById(R.id.product_quantity);
        mSupplierNameEditText = findViewById(R.id.product_supplier);
        mSupplierContactEditText = findViewById(R.id.product_supplier_contact);

        //Set onTouchListeners on the TextFields to check if any information was changed before
        // leafing the EditActivity.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierContactEditText.setOnTouchListener(mTouchListener);


        //Implement the button to save a product.
        Button saveProductButton = findViewById(R.id.save_product_button);
        saveProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
            }
        });

        //Implement the button to delete a product.
        Button deleteProductButton = findViewById(R.id.delete_product_button);

        deleteProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        //Implement the button to raise and lower prodcut quantity.
        ImageButton raiseQuantityButton = findViewById(R.id.raise_quantity_button);
        raiseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRaiseQuantityDialogue();
            }
        });

        ImageButton lowerQuantityButton = findViewById(R.id.lower_quantity_button);
        lowerQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLowerQuantityDialogue();
            }
        });

        //Implement the button to contact the supplier for ordering more product supplies.
        final Button contactSupplierButton = findViewById(R.id.contact_supplier_button);

        if(mCurrentProductUri == null) {
            contactSupplierButton.setVisibility(View.GONE);
        } else {
            contactSupplierButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Check if a supplier contact number has been entered. If not, return early.
                    if (productSupplierContact == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_supplier_contact_saved),
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        contactSupplier(productSupplierContact);
                    }

                }
            });
        }

    }

    // Get the Strings and integers from the information entered into the EditText fields.
    private void saveProduct() {
        String productName = mNameEditText.getText().toString().trim();
        String productPriceInput = mPriceEditText.getText().toString().trim();
        String productQuantityInput = mQuantityEditText.getText().toString().trim();
        String productSupplierName = mSupplierNameEditText.getText().toString().trim();
        productSupplierContact = mSupplierContactEditText.getText().toString().trim();


        int productPrice = 0;
        productQuantity = 0;


        sanityCheck(productName, productPriceInput, productPrice, productQuantityInput,
                productSupplierName, productSupplierContact);

        //Return early if the sanityCheck is not passed.
        if (!mPassedSanityCheck) {
            return;
        }

        //Parse the product price and quantity into integers.
        productPrice = Integer.parseInt(productPriceInput);
        productQuantity = Integer.parseInt(productQuantityInput);


        //Convert Strings with letters to capitalize first letter and write all further letters in
        //lower case. This also makes it more difficult for users to enter SQL commands
        //(like DROP TABLE) into the code, as they are case sensitive.
        //Adjusting the strings uses code from: cagdasalagoz
        // (https://stackoverflow.com/questions/2375649/converting-to-upper-and-lower-case-in-java)

        productName = productName.substring(0, 1).toUpperCase() +
                productName.substring(1).toLowerCase();

        productSupplierName = productSupplierName.substring(0, 1).toUpperCase() +
                productSupplierName.substring(1).toLowerCase();


        ContentValues cV = new ContentValues();

        //Add the user input to the database
        cV.put(ProductRecord.ProductEntry.COLUMN_PRODUCT_NAME, productName);
        cV.put(ProductRecord.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        cV.put(ProductRecord.ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        cV.put(ProductRecord.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, productSupplierName);
        cV.put(ProductRecord.ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT, productSupplierContact);

        //Check if the entered product is a new one or if it already has a productURI
        if (mCurrentProductUri == null) {
            //This is a new product, therefore a new product has to be insert and a new productURI
            // has to be provided.

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, cV);

            //Show a toast message according to whether the product was successfully saved or not.
            if (newUri == null) {
                //If the new URI is null, there was an error inserting the product.
                Toast.makeText(this, R.string.error_saving_new_product,
                        Toast.LENGTH_SHORT).show();
            } else {
                //If a new URI was created, the product has been saved correctly.
                Toast.makeText(this, R.string.new_product_saved,
                        Toast.LENGTH_SHORT).show();

                Intent listActivityIntent = new Intent(EditActivity.this,
                        ListActivity.class);

                //Check if EditActivity actually exists before starting it.
                if (listActivityIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(listActivityIntent);
                }
            }

        } else {
            //In case of an existing product, update the database.
            int rowsChanged = getContentResolver().update(mCurrentProductUri, cV, null, null);

            //Check if the content in any rows was changed.
            if (rowsChanged == 0) {
                //If no row was changed send a message that an error occured updating the product.
                Toast.makeText(this, R.string.error_updating_product, Toast.LENGTH_SHORT)
                        .show();
            } else {
                //Display a toast when the product was updated correctly.
                Toast.makeText(this, R.string.update_prodcut, Toast.LENGTH_SHORT).show();

                Intent listActivityIntent = new Intent(EditActivity.this,
                        ListActivity.class);

                //Check if EditActivity actually exists before starting it.
                if (listActivityIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(listActivityIntent);
                }
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        //As the editor shows all information entered about each product, the projection needs to
        //include all the columns from the product table.

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT
        };

        Log.e(LOG_TAG, "Loader Initiated");

        //Initiate a loader and execute the query on a background thread.
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or the cursor count of rows is below 1.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //Read the first row from the cursor. This should be the only row loaded.
        if (cursor.moveToFirst()) {

            //Find the columns of the database we want to display.
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex
                    (ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierContactColumnIndex = cursor.getColumnIndex
                    (ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT);

            //Extract the information from the columns.

            String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            productQuantity = cursor.getInt(quantityColumnIndex);
            String productSupplierName = cursor.getString(supplierNameColumnIndex);
            productSupplierContact = cursor.getString(supplierContactColumnIndex);

            //Update the views on the screen with the received data.
            mNameEditText.setText(productName);
            mPriceEditText.setText(Integer.toString(productPrice));
            mQuantityEditText.setText(Integer.toString(productQuantity));
            mSupplierNameEditText.setText(productSupplierName);
            mSupplierContactEditText.setText(productSupplierContact);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //Clear out data from all the fields if the loader is invalidated.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierContactEditText.setText("");
    }

    //Check if information has been entered into the mandatory fields name and price.
    private void sanityCheck(String productName, String productPriceInput, int productPrice,
                             String productQuantityInput, String productSupplierName,
                             String productSupplierContact) {

        //Return early without storing the product if no product name was entered.
        if (productName.isEmpty()) {
            Toast.makeText(this, R.string.product_name_missing, Toast.LENGTH_SHORT).show();

            return;
        }

        //Return early without storing the product if no price entered.
        if (productPriceInput.isEmpty()) {
            Toast.makeText(this, R.string.product_price_missing, Toast.LENGTH_SHORT).show();

            return;
        }

        //Parse the product price into an integer.
        productPrice = Integer.parseInt(productPriceInput);

        //Check if the price is over 0. If not, return early without storing the product.
        if (productPrice <= 0) {
            Toast.makeText(this, R.string.product_price_wrong, Toast.LENGTH_SHORT).show();

            return;
        }

        //Check if a quantity has been entered.
        if (productQuantityInput.isEmpty()) {
            Toast.makeText(this, R.string.product_quantity_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        //Parse the product price into an integer.
        productQuantity = Integer.parseInt(productQuantityInput);

        //Make sure the quantity is not a negative value. If it is, return early without storing the product.
        if (productQuantity < 0) {
            Toast.makeText(this, R.string.product_quantity_wrong, Toast.LENGTH_SHORT).show();
            return;
        }

        //Check if a supplier name has been entered.
        if (productSupplierName.isEmpty()) {
            Toast.makeText(this, R.string.product_supplier_name_missing,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (productSupplierContact.isEmpty()) {
            Toast.makeText(this, R.string.product_supplier_contact_missing,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mPassedSanityCheck = true;
    }

    //Check for unsaved changes when back press is used.
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press as normal
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // If there are unsaved changes, start a dialogue and ask the user if they truly want to
        // discard them.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //Modify the up button and check if the user entered unsaved data. If unsaved data is detected,
    //show an alert and ask the user if they want to discard the data or continue editing.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //If the product hasn't been changed, navigate up as normally.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);

                    return true;
                }
                //If the product has been changed, show the unsaved changes alert dialogue.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, close the current activity.
                                finish();
                            }
                        };

                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Create a dialogue for when a user wants to leave the activity without saving updated ata.

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener) {
        //Create an alert dialogue to ask the user if they want to leave the activity and thereby
        //discard unsaved information or stay.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_alert_message);
        builder.setPositiveButton(R.string.unsaved_changes_discard_button,
                discardButtonClickListener);
        builder.setNegativeButton(R.string.unsaved_changes_dont_discard_button, new DialogInterface.OnClickListener() {
            //If the user clicked the keep editing button, stay in the Activity.
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create and show the dialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    //Create an alert dialogue to confirm if a product shall indeed be deleted.
    private void showDeleteConfirmationDialog() {
        //Create the alert dialogue and set the message that shall be displayed.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_product_confirmation);

        //Set the positive button for the dialogue and starte the deleteProduct method when it is
        //clicked.
        builder.setPositiveButton(R.string.delete_product_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
            }
        });

        builder.setNegativeButton(R.string.delete_product_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //A method to delete a product from the inventory database.
    private void deleteProduct() {
        //Check if the delete action is called on an existing product.
        if (mCurrentProductUri != null) {
            //The content resolver is called to delete the product and the given product URI.
            //Selection and Selection args are null, because the product is identified by the uri.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            //Show a post message depending on whether the product was successfully deleted or not.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_product_failure),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_product_success),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            //If the product doesn't exist yet, just set all fields to zero.
            mNameEditText.setText("");
            mPriceEditText.setText("");
            mQuantityEditText.setText("");
            mSupplierNameEditText.setText("");
            mSupplierContactEditText.setText("");
        }

        finish();
    }

    //Create an alert dialogue to give the user the possibility to raise the quantity of the product.
    //For more information refer to:
    //https://stackoverflow.com/questions/10903754/input-text-dialog-android
    private void showRaiseQuantityDialogue() {
        //Create an alert dialogue to ask the user if they want to raise the quantity of the product
        // and if so by how much.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.raise_product_quantity);

        //Set up and specify the enter text field to enter the number by which the quantity shall be
        //raised.
        final EditText raiseAmount = new EditText(this);

        raiseAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        //Set a default values of 1 in the edittext.
        raiseAmount.setText("1");
        builder.setView(raiseAmount);

        //Add the quantity to the value.
        builder.setPositiveButton(R.string.add_product_quantity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int addedQuantity;
                mQuantityAmountRaise = raiseAmount.getText().toString().trim();
                try {
                    addedQuantity = Integer.parseInt(mQuantityAmountRaise);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_quantity_invalid_input),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                addQuantity(addedQuantity);
                mQuantityEditText.setText(Integer.toString(productQuantity));

                //Set the boolean mProductHasChanged to true so that the SaveChanges show up should
                // the user leave the Activity without saving.
                mProductHasChanged = true;
            }
        });

        builder.setNegativeButton(R.string.cancel_adding_product_quantity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Create an alert dialogue to give the user the possibility to raise the quantity of the product.
    //For more information refer to:
    //https://stackoverflow.com/questions/10903754/input-text-dialog-android
    private void showLowerQuantityDialogue() {
        //Create an alert dialogue to ask the user if they want to raise the quantity of the product
        // and if so by how much.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.lower_product_quantity);

        //Set up and specify the enter text field to enter the number by which the quantity shall be
        //raised.
        final EditText lowerAmount = new EditText(this);

        lowerAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        //Set a default values of 1 in the edittext.
        lowerAmount.setText("1");
        builder.setView(lowerAmount);

        //Reduce the quantity by the value.
        builder.setPositiveButton(R.string.reduce_product_quantity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int reducedQuantity;
                mQuantityAmountLower = lowerAmount.getText().toString().trim();
                try {
                    reducedQuantity = Integer.parseInt(mQuantityAmountLower);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.lower_quantity_invalid_input),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //Quantity can never be below 0. Check if the amount reduced is higher than the
                // current quantity and if so return early.

                if (reducedQuantity > productQuantity) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.quantity_cannot_be_below_zero),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                lowerQuantity(reducedQuantity);
                mQuantityEditText.setText(Integer.toString(productQuantity));

                //Set the boolean mProductHasChanged to true so that the SaveChanges show up should
                // the user leave the Activity without saving.
                mProductHasChanged = true;
            }
        });

        builder.setNegativeButton(R.string.cancel_lowering_product_quantity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Handle raising the product quanity. The change will not be stored into the database right away.
    //The user has to confirm the change via the save function.

    int addQuantity(int addedQuantity) {
        productQuantity += addedQuantity;
        return productQuantity;
    }

    //Handle lowering the product quantity. The change will not be stored into the database right away.
    //The user has to confirm the change via the save function.
    int lowerQuantity(int loweredQuantity) {

        productQuantity -= loweredQuantity;
        return productQuantity;

    }

    //Open a phone app with the number stored for the supplier. Use dial instead of call so that the
    //user can explicitly start the call in the phone app.
    private void contactSupplier(String phoneNumber) {
        Uri contactNumber = Uri.parse("tel:" + phoneNumber);

        //Check if the contact number has been set up successfully, if not return early.
        if (contactNumber == null) {
            Toast.makeText(this, getString(R.string.error_contact_number),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Set up the intent.
        Intent supplierCall = new Intent(Intent.ACTION_DIAL, contactNumber);

        //Check if there is a phone app on the device before starting the intent.
        if (supplierCall.resolveActivity(getPackageManager()) != null) {
            startActivity(supplierCall);

        }
    }

}

