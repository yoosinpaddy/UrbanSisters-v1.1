package com.urban.sisters.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.urban.sisters.R;
import com.urban.sisters.adapter.ProductsAdapter;
import com.urban.sisters.models.EmptyModel;
import com.urban.sisters.models.OrdersModel;
import com.urban.sisters.models.ProductsModel;
import com.urban.sisters.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_ORDERS;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_ORDERS_COLUMN_DATE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_ORDERS_COLUMN_STATUS;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_DESCRIPTION;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_RATING;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_ROW_ID;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE;
import static com.urban.sisters.utils.Utils.showChangeLanguageDialog;
import static com.urban.sisters.utils.Utils.showResponseDialog;

public class Orders extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference dbTblOrder;
    private FirebaseUser mCurrentUser;
    ProductsAdapter ordersAdapter;
    RecyclerView recyclerView;
    ArrayList<Object> orderList = new ArrayList<>();
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_orders);

        utils = new Utils(this);

        setTitleBar(utils.getStringResource(R.string.orders));
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        dbTblOrder = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_TBL_ORDERS);

    }

    @Override
    protected void onStart() {
        super.onStart();
       dbTblOrder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                orderList.clear();
                if(dataSnapshot.hasChildren()) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    dbTblOrder.push().getKey();
                    //loop through the kids

                    for (DataSnapshot childSnapShot : children) {
                        String userId = childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY).getValue().toString();

                        OrdersModel oModel = childSnapShot.getValue(OrdersModel.class);
                        oModel.setRowId(childSnapShot.getKey());
                        oModel.setSku(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU).getValue().toString());
                        oModel.setTitle(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE).getValue().toString());
                        oModel.setPrice(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE).getValue().toString());
                        oModel.setOrderDate(childSnapShot.child(FIREBASE_DB_TBL_ORDERS_COLUMN_DATE).getValue().toString());
                        oModel.setOrderStatus(childSnapShot.child(FIREBASE_DB_TBL_ORDERS_COLUMN_STATUS).getValue().toString());

                        if (mCurrentUser.getUid().equals(userId) && utils.getUserLevel().equals("customer")) {
                            orderList.add(oModel);
                        } else if (utils.getUserLevel().equals("admin")) {
                            orderList.add(oModel);
                        }
                    }
                }
                else{
                    EmptyModel emptyModel = new EmptyModel();
                    emptyModel.setMessage(utils.getStringResource(R.string.no_order_placed));
                    orderList.add(emptyModel);
                }


                GridLayoutManager gridLayoutManager = new GridLayoutManager(Orders.this,
                        1, LinearLayoutManager.VERTICAL, false);
                recyclerView = findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(gridLayoutManager);
                ordersAdapter = new ProductsAdapter(Orders.this, orderList){
                    @Override
                    public void selectedItem(int position) {
                        OrdersModel oModel = (OrdersModel) orderList.get(position);
                        completeOrder(oModel.getRowId());
                    }
                };
                recyclerView.setAdapter(ordersAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTitleBar(String title){
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    private void completeOrder(String rowId) {
        dbTblOrder.child(rowId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ProgressBar progressBar = findViewById(R.id.progress);
                progressBar.setVisibility(View.VISIBLE);
                dataSnapshot.getRef().child(FIREBASE_DB_TBL_ORDERS_COLUMN_STATUS).setValue("Completed");
                progressBar.setVisibility(View.GONE);
                showResponseDialog(Orders.this, 1, "Order completed successfully");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Order", databaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_rigth_menu, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
