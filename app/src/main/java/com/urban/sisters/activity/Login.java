package com.urban.sisters.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.urban.sisters.R;
import com.urban.sisters.fcm.NotificationUtils;
import com.urban.sisters.lang.LanguageHelper;
import com.urban.sisters.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_DESCRIPTION;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_RATING;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_USERS;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_USERS_COLUMN_NAME;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_USERS_COLUMN_PHONE;
import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_USERS_COLUMN_USERLEVEL;
import static com.urban.sisters.utils.Utils.FIREBASE_NOTIFICATION_RECEIVER;
import static com.urban.sisters.utils.Utils.PREF_DATA_EMAIL;
import static com.urban.sisters.utils.Utils.PREF_DATA_NAME;
import static com.urban.sisters.utils.Utils.PREF_DATA_PHONE_NO;
import static com.urban.sisters.utils.Utils.PREF_DATA_USERLEVEL;
import static com.urban.sisters.utils.Utils.PUSH_NOTIFICATION;
import static com.urban.sisters.utils.Utils.REGISTRATION_COMPLETE;
import static com.urban.sisters.utils.Utils.showChangeLanguageDialog;
import static com.urban.sisters.utils.Utils.validateEmail;

public class Login extends AppCompatActivity {

    private EditText etxt_name, etxt_mobile_number, etxt_email_address, etxt_password, etxt_confirm_password;

    private TextView txt_welcome, hint_etxt_name, hint_etxt_mobile_number, hint_etxt_email_address,
            hint_etxt_password, hint_etxt_confirm_password;

    private ProgressBar progressBar;

    private RelativeLayout rlLoginWithUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference dbTblUsers;

    private String customer = "customer", admin = "admin";
    private String userLevel = customer;
    private RadioGroup radioGroup;

    Utils utils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        dbTblUsers = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_TBL_USERS);

        utils = new Utils(this);
        switchToLogin();
    }

    private void initViews(){
        //user login
        rlLoginWithUserID = findViewById(R.id.rl_email_address);
        txt_welcome = findViewById(R.id.txt_welcome);
        etxt_email_address = findViewById(R.id.etxt_email_address);
        hint_etxt_email_address = findViewById(R.id.hint_etxt_email_address);
        etxt_password = findViewById(R.id.etxt_password);
        hint_etxt_password = findViewById(R.id.hint_etxt_password);
        progressBar = findViewById(R.id.progress);

        utils.onEditTextChangeListener(etxt_email_address, hint_etxt_email_address);
        utils.onEditTextChangeListener(etxt_email_address, hint_etxt_email_address);
        utils.onEditTextChangeListener(etxt_password, hint_etxt_password);

        String welcomeUser = utils.getStringResource(R.string.welcome);
        rlLoginWithUserID.setVisibility(View.VISIBLE);

        if(utils.getUserName() != null){
            welcomeUser += " "+ utils.getUserName();
            rlLoginWithUserID.setVisibility(View.GONE);
        }
        txt_welcome.setText(welcomeUser);

    }

    private void initUserRegViews(){
        //user registration
        radioGroup = findViewById(R.id.radio_group);

        etxt_name = findViewById(R.id.etxt_name);
        hint_etxt_name = findViewById(R.id.hint_etxt_name);

        etxt_mobile_number = findViewById(R.id.etxt_mobile_number);
        hint_etxt_mobile_number = findViewById(R.id.hint_etxt_mobile_number);
        etxt_mobile_number.setTransformationMethod(null);

        etxt_email_address = findViewById(R.id.etxt_email_address);
        hint_etxt_email_address = findViewById(R.id.hint_etxt_email_address);

        etxt_password = findViewById(R.id.etxt_password);
        hint_etxt_password = findViewById(R.id.hint_etxt_password);

        etxt_confirm_password = findViewById(R.id.etxt_confirm_password);
        hint_etxt_confirm_password = findViewById(R.id.hint_etxt_confirm_password);
        progressBar = findViewById(R.id.progress);

        utils.onEditTextChangeListener(etxt_name, hint_etxt_name);
        utils.onEditTextChangeListener(etxt_mobile_number, hint_etxt_mobile_number);
        utils.onEditTextChangeListener(etxt_email_address, hint_etxt_email_address);
        utils.onEditTextChangeListener(etxt_password, hint_etxt_password);
        utils.onEditTextChangeListener(etxt_confirm_password, hint_etxt_confirm_password);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.customer) {
                    userLevel = customer;
                } else if(checkedId == R.id.admin) {
                    userLevel = admin;
                }
            }

        });
    }

    private void setTitleBar(String title){
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        if(title.equals(utils.getStringResource(R.string.register))) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //set the toggle menu icon
        }
    }

    void switchToLogin(){
        setContentView(R.layout.layout_login);
        setTitleBar(utils.getStringResource(R.string.login));
        initViews();
    }

    void switchToRegister(){
        setContentView(R.layout.layout_register);
        setTitleBar(utils.getStringResource(R.string.register));
        initUserRegViews();
    }

    public void onClickLogin(View v) {
        validateAndLogin();
    }

    public void onClickForgotPassword(View v){
        Toast.makeText(Login.this, utils.getStringResource(R.string.server_not_responding), Toast.LENGTH_LONG).show();
    }

    public void onClickRegister(View v){
        switchToRegister();
    }

    public void onClickRegisterUser(View v){
        validateAndRegister();
    }

    private void validateAndLogin() {
        final String emailAddress = (utils.getUserEmail() != null)?
                utils.getUserEmail() :  etxt_email_address.getText().toString();
        String password =etxt_password.getText().toString();

        if (utils.getUserEmail() == null && utils.isEmptyString(emailAddress)) {
            etxt_email_address.setError(utils.getStringResource(R.string.email_address_required));
        }
        else if (utils.getUserEmail() == null && !validateEmail(emailAddress)) {
            etxt_email_address.setError(utils.getStringResource(R.string.invalid_email_address));
        }
        else if (utils.isEmptyString(password)) {
            etxt_password.setError(utils.getStringResource(R.string.password_required));
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(emailAddress,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        if(utils.getUserEmail() == null){
                            dbTblUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String userID = mAuth.getCurrentUser().getUid();
                                    String userName = dataSnapshot.child(userID).child(FIREBASE_DB_TBL_USERS_COLUMN_NAME).getValue().toString();
                                    String userPhone = dataSnapshot.child(userID).child(FIREBASE_DB_TBL_USERS_COLUMN_PHONE).getValue().toString();
                                    String userLevel = dataSnapshot.child(userID).child(FIREBASE_DB_TBL_USERS_COLUMN_USERLEVEL).getValue().toString();
                                    createLocalAccount(userName, userPhone, emailAddress,userLevel);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        verifyUser();

                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void verifyUser(){
        final String user_id = mAuth.getCurrentUser().getUid();
        dbTblUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.hasChild(user_id)){
                    startActivity(new Intent(Login.this, HomePage.class));
                }else {
                    Toast.makeText(Login.this, utils.getStringResource(R.string.not_registered), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void validateAndRegister() {

        //get user inputs
        final String name = etxt_name.getText().toString(),
                phone = etxt_mobile_number.getText().toString(),
                emailAddress = etxt_email_address.getText().toString(),
                password = etxt_password.getText().toString(),
                confirmPassword = etxt_confirm_password.getText().toString();


        //validate user input
        if (utils.isEmptyString(name))
            etxt_name.setError(utils.getStringResource(R.string.name_required));
        else if (utils.isEmptyString(phone))
            etxt_name.setError(utils.getStringResource(R.string.phone_required));
        else if (utils.isEmptyString(emailAddress))
            etxt_email_address.setError(utils.getStringResource(R.string.email_address_required));
        else if (!validateEmail(emailAddress))
            etxt_email_address.setError(utils.getStringResource(R.string.invalid_email_address));
        else if (password.length() < 6)
            etxt_password.setError(utils.getStringResource(R.string.password_characters));
        else if (utils.isEmptyString(password))
            etxt_password.setError(utils.getStringResource(R.string.password_required));
        else if (!password.equals(confirmPassword))
            etxt_confirm_password.setError(utils.getStringResource(R.string.password_did_not_match));
        else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()) {

                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference insertData = dbTblUsers.child(userId);

                        Map<String, String> param = new HashMap<>();
                        param.put(FIREBASE_DB_TBL_USERS_COLUMN_NAME, name);
                        param.put(FIREBASE_DB_TBL_USERS_COLUMN_PHONE, phone);
                        param.put(FIREBASE_DB_TBL_USERS_COLUMN_USERLEVEL, userLevel);
                        insertData.setValue(param);

                        createLocalAccount(name, phone, emailAddress,userLevel);
                        progressBar.setVisibility(View.GONE);
                        switchToLogin();
                        Toast.makeText(Login.this, utils.getStringResource(R.string.registered_successfully), Toast.LENGTH_LONG).show();
                    }
                    else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }

    void createLocalAccount(String name, String phone, String email, String userLevel){
        utils.writeStringPref(PREF_DATA_NAME, name);
        utils.writeStringPref(PREF_DATA_PHONE_NO, phone);
        utils.writeStringPref(PREF_DATA_EMAIL, email);
        utils.writeStringPref(PREF_DATA_USERLEVEL, userLevel);
    }

    void deleteLocalAccount(){
        utils.deletePref(PREF_DATA_NAME);
        utils.deletePref(PREF_DATA_PHONE_NO);
        utils.deletePref(PREF_DATA_EMAIL);
        txt_welcome.setText(utils.getStringResource(R.string.welcome));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the orders_product_items; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_rigth_menu, menu);
        menu.getItem(1).setVisible(false);
        if(!utils.isEmptyString(utils.getUserEmail())) {
            menu.getItem(1).setVisible(true);
            menu.getItem(1).setIcon(utils.getDrawableResource(R.drawable.ic_lock));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            switchToLogin();
        }
        else if (id == R.id.action_logout) {
            deleteLocalAccount();
            rlLoginWithUserID.setVisibility(View.VISIBLE);
            item.setVisible(false);
        }
        else if (id == R.id.action_change_language) {
            showChangeLanguageDialog(Login.this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {}

    @Override
    protected void onResume() {
        super.onResume();

//        // register GCM registration complete receiver
//        LocalBroadcastManager.getInstance(this).registerReceiver(FIREBASE_NOTIFICATION_RECEIVER,
//                new IntentFilter(REGISTRATION_COMPLETE));
//
//        // register new push message receiver
//        // by doing this, the activity will be notified each time a new message arrives
//        LocalBroadcastManager.getInstance(this).registerReceiver(FIREBASE_NOTIFICATION_RECEIVER,
//                new IntentFilter(PUSH_NOTIFICATION));
//
//        // clear the notification area when the app is opened
//        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(FIREBASE_NOTIFICATION_RECEIVER);
        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context base) {
        //load the user language from preference
        super.attachBaseContext(LanguageHelper.onAttach(base));
    }

}



