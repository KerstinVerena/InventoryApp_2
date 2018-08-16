package com.example.android.inventoryapp_2;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.example.android.inventoryapp_2.data.ProductRecord.ProductEntry;


public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Implement the button to get to the EditActivity.
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editActivityIntent = new Intent(ListActivity.this,
                        EditActivity.class);

                //Check if EditActivity actually exists before starting it.
                if (editActivityIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(editActivityIntent);
                }
            }
        });

        //Find the ListView that shall be populated with the data of the products.
        ListView productsListView = findViewById(R.id.inventory_list);

        //Set an Empty View if there are no entries in the list view.
        View emptyView = findViewById(R.id.empty_view);
        productsListView.setEmptyView(emptyView);

        //Setup an adapter to create a list item for each row of the cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productsListView.setAdapter(mCursorAdapter);

        productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to the EditActivity
                Intent editProductIntent = new Intent(ListActivity.this, EditActivity.class);

                // Get the content URI and append the "id" to find the product that was clicked on.

                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                editProductIntent.setData(currentProductUri);

                //Check if EditActivity actually exists before starting it.
                if (editProductIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(editProductIntent);
                }
            }
        });

        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Instantiate the subclass of SQLiteOpenHelper to access the database
        // and pass the context of the current activity.

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT
        };

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mCursorAdapter.swapCursor(null);
    }
}
