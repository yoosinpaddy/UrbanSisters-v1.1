package com.urban.sisters.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.urban.sisters.R;
import com.urban.sisters.adapter.ProductsAdapter;
import com.urban.sisters.fcm.NotificationUtils;
import com.urban.sisters.lang.LanguageHelper;
import com.urban.sisters.models.EmptyModel;
import com.urban.sisters.models.ProductsModel;
import com.urban.sisters.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_ORDERS;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_ORDERS_COLUMN_DATE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_ORDERS_COLUMN_STATUS;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_DESCRIPTION;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_RATING;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE;
import static com.urban.sisters.utils.Utils.FIREBASE_NOTIFICATION_RECEIVER;
import static com.urban.sisters.utils.Utils.PUSH_NOTIFICATION;
import static com.urban.sisters.utils.Utils.REGISTRATION_COMPLETE;
import static com.urban.sisters.utils.Utils.showChangeLanguageDialog;
import static com.urban.sisters.utils.Utils.showResponseDialog;


public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    Utils utils;
    ProductsAdapter productsAdapter;
    RecyclerView recyclerView;
    private DatabaseReference dbTblProducts, dbTblOrder;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    NavigationView navigationView;
    ArrayList <Object> productList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_page);

        utils = new Utils(this);

        setTitleBar(utils.getStringResource(R.string.hair_extension));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.menu_admin).setVisible(false);
        if(utils.getUserLevel().equals("admin"))
            nav_Menu.findItem(R.id.menu_admin).setVisible(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        mAuth = FirebaseAuth.getInstance();
        dbTblProducts = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_TBL_PRODUCTS);
        dbTblOrder = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_TBL_ORDERS);
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbTblProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                productList.clear();
                int gridViewSpanCount = 2;
                if(dataSnapshot.hasChildren()) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    //loop through the kids
                    for (DataSnapshot childSnapShot : children) {
                        ProductsModel pModel = childSnapShot.getValue(ProductsModel.class);
                        pModel.setImage(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE).getValue().toString());
                        pModel.setSku(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU).getValue().toString());
                        pModel.setTitle(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE).getValue().toString());
                        pModel.setDescription(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_DESCRIPTION).getValue().toString());
                        pModel.setPrice(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE).getValue().toString());
                        pModel.setRating(childSnapShot.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_RATING).getValue().toString());
                        productList.add(pModel);
                    }
                }
                else{
                    EmptyModel emptyModel = new EmptyModel();
                    emptyModel.setMessage(utils.getStringResource(R.string.no_products_available));
                    productList.add(emptyModel);
                    gridViewSpanCount= 1;
                }

                GridLayoutManager gridLayoutManager = new GridLayoutManager(HomePage.this,
                        gridViewSpanCount, LinearLayoutManager.VERTICAL, false);
                recyclerView = findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(gridLayoutManager);
                productsAdapter = new ProductsAdapter(HomePage.this, productList){
                    @Override
                    public void selectedItem(int position) {

                        ProgressBar progressBar = findViewById(R.id.progress);
                        progressBar.setVisibility(View.VISIBLE);
                        ProductsModel pModel = (ProductsModel)productList.get(position);

                        final DatabaseReference pushData = dbTblOrder.push();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currDateTime = sdf.format(new Date());

                        mCurrentUser = mAuth.getCurrentUser();
                        pushData.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU).setValue(pModel.getSku());
                        pushData.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE).setValue(pModel.getTitle());
                        pushData.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE).setValue(pModel.getPrice());
                        pushData.child(FIREBASE_DB_TBL_ORDERS_COLUMN_DATE).setValue(currDateTime);
                        pushData.child(FIREBASE_DB_TBL_ORDERS_COLUMN_STATUS).setValue("Pending");
                        pushData.child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY).setValue(mCurrentUser.getUid());
                        progressBar.setVisibility(View.GONE);
                        showResponseDialog(HomePage.this, 1, "Order placed successfully");
                    }
                };
                recyclerView.setAdapter(productsAdapter);
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

    void logout(){
        startIntentActivity(Login.class, null);
    }

    public void startIntentActivity(Class <?> mClass, Object extras){
        Intent intent = new Intent(HomePage.this, mClass);

        if(extras != null)
            intent.putExtra("action", (int) extras);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_rigth_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else
                drawerLayout.openDrawer(GravityCompat.START);
        }
        else if (id == R.id.action_change_language) {
            showChangeLanguageDialog(this);
        }
        else if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_hair_extension) {
            startIntentActivity(HomePage.class, null);
        }
        else if (id == R.id.menu_orders) {
            startIntentActivity(Orders.class, null);
        }
        else if (id == R.id.menu_admin) {
            startIntentActivity(AdminPanel.class, null);
        }
        else if (id == R.id.menu_change_language) {
            showChangeLanguageDialog(this);
        }
        else if (id == R.id.menu_profile) {
        }
        else if (id == R.id.menu_logout) {
            logout();
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        /*utils.registerFCMReceiver();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(FIREBASE_NOTIFICATION_RECEIVER,
                new IntentFilter(REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(FIREBASE_NOTIFICATION_RECEIVER,
                new IntentFilter(PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());*/
    }

    @Override
    protected void onPause() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(FIREBASE_NOTIFICATION_RECEIVER);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        //load the user language from preference
        super.attachBaseContext(LanguageHelper.onAttach(base));
    }

}