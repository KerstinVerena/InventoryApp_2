package com.example.android.inventoryapp_2;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp_2.data.ProductDbHelper;
import com.example.android.inventoryapp_2.data.ProductRecord;
import com.example.android.inventoryapp_2.data.ProductRecord.ProductEntry;

import java.text.DecimalFormat;


/**
 * The {@link ProductCursorAdapter}.
 * is a customized CursorAdapter to display the data of the Product Database in a ListView.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The app context
     * @param cursor  The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }


    /**
     * This method mnakes an empty List Item View without binding any data to it.
     * <p>
     * It takes in the following parameters:
     *
     * @param context   The app context
     * @param cursor    The cursor from which to get the data. It is already moved to the respctive
     *                  position.
     * @param viewGroup The parent of the views to which the data will later on be attached.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup,
                false);
    }

    /**
     * In this method, the product data is bound to the different views of the parent viewGroup.
     * It will always be the data taken from the current view that is displayed.
     *
     * @param view    Existing view, returned by newView() method
     * @param context The pp context
     * @param cursor  The cursor providing the data moved to the current row.
     */
    @SuppressLint("StringFormatInvalid")
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //Find the views in the list_item layout.
        TextView productNameTextView = view.findViewById(R.id.product_name_list);
        TextView productPriceTextView = view.findViewById(R.id.product_price_list);
        Button buyButton = view.findViewById(R.id.buy_button);
        TextView inStockView = view.findViewById(R.id.in_stock);

        // Find the respective columns of the product database which we want to display at the
        // current cursor position.
        int productNameColumnIndex = cursor.getColumnIndex
                (ProductRecord.ProductEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex
                (ProductRecord.ProductEntry.COLUMN_PRODUCT_PRICE);

        int productQuantityColumnIndex = cursor.getColumnIndex
                (ProductRecord.ProductEntry.COLUMN_PRODUCT_QUANTITY);


        // Read the data from the columns and get it as strings.
        final String productName = cursor.getString(productNameColumnIndex);
        double productPrice = cursor.getDouble(productPriceColumnIndex) / 100;

        String formattedPrice = formatPrice(productPrice) + " " +
                context.getResources().getString(R.string.currency);

        final int productQuantity = cursor.getInt(productQuantityColumnIndex);

        //Create variables for the ID of the color of the buy-button, the text if a product is in
        // stock and different colors according to whether it is in stock or not.
        int buyButtonColor;
        String inStockText;
        int inStockTextColor;

        final boolean isInStock;

        //Initialize a boolean to check if the product is in stock (quantity higher than 0) or not.
        isInStock = checkInStock(productQuantity);

        //Set colors and text according to whether item is in stock or not.
        if (isInStock) {
            buyButtonColor = ContextCompat.getColor(context, R.color.availableButton);
            inStockText = context.getString(R.string.product_in_stock,
                    String.valueOf(productQuantity));
            inStockTextColor = ContextCompat.getColor(context, R.color.productAvailable);

        } else {
            buyButtonColor = ContextCompat.getColor(context, R.color.notAvailableButton);
            inStockText = context.getString(R.string.product_out_of_stock);
            inStockTextColor = ContextCompat.getColor(context, R.color.productOutOfStock);
        }

        //Set text and colors to the respective views.
        productNameTextView.setText(productName);
        productPriceTextView.setText(formattedPrice);
        buyButton.setBackgroundColor(buyButtonColor);
        inStockView.setText(inStockText);
        inStockView.setTextColor(inStockTextColor);

        //Declare a final int productId to find the id of the current product.
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductRecord.ProductEntry._ID));
        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);

        //Set an onItemClickListener on the buy-button.
        buyButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onClick(View view) {
                //Check if the item is still in stock.
                if (isInStock) {
                    //If the item is still in check, sell one by calling the buyItem-method.
                    buyItem(context, currentProductUri, productQuantity, productName);
                } else {
                    //Show a toast message to tell the user that the product is currently not
                    // available.
                    Toast.makeText(context, context.getString(R.string.currently_not_available,
                            productName), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //Format the price from Cent to Euro.
    private String formatPrice(double price) {
        DecimalFormat priceFormat = new DecimalFormat("0.00");
        return priceFormat.format(price);
    }


    //A method to check whether a product is in stock (quantity > zero) or not. Returns a boolean.
    private boolean checkInStock(int quantity) {
        int iQuantity = quantity;

        if (iQuantity > 0) {
            return true;
        } else {
            return false;
        }
    }

    //Handle selling a prodcut via the buy-button.
    @SuppressLint("StringFormatInvalid")
    private void buyItem(Context context, Uri uri, int productQuantity, String productName) {
        //Reduce quantity by one
        productQuantity = productQuantity - 1;

        //Enter the new quantity into the database.
        ContentValues cV = new ContentValues();
        cV.put(ProductRecord.ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        int rowsChanged = context.getContentResolver().update(uri, cV, null, null);

        //Check if the content was updated properly if not give an error message.
        if (rowsChanged == 0) {
            //Show a toast message so that the user know, he successfully bought the product.
            Toast.makeText(context, context.getString(R.string.error_buying_product,
                    productName), Toast.LENGTH_SHORT).show();
        } else {
            //else show a string to tell the user that the item was successfully purchased.
            Toast.makeText(context, context.getString(R.string.successful_purchase,
                    productName), Toast.LENGTH_SHORT).show();
        }
    }
}
