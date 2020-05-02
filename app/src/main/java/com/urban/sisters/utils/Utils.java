package com.urban.sisters.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.urban.sisters.lang.LanguageHelper;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.urban.sisters.R;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

    public static BroadcastReceiver FIREBASE_NOTIFICATION_RECEIVER;

    public static final String
            CONSUMER_KEY = "GA2qub6l4RJaNtilkYFQ7ey85olH0T5g",
            CONSUMER_SECRET = "IJQcFchU5kAMwH7D";

    //mpesa STKPush Properties
    public static final String BUSINESS_SHORT_CODE = "174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";

    public static final String
            APP_SHARED_PREF = "app_pref";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    //languages swa/englo
    public static final String
            USER_LANGUAGE = "language",
            USER_LANGUAGE_EN = "EN",
            USER_LANGUAGE_SW = "SW";

    //firebase push notifications
    public static final String
            FIREBASE_DEVICE_TOKEN = "firebase_token_id",
            FIREBASE_NOTIFICATION_TOPIC_GLOBAL = "global",

            FIREBASE_DB_TBL_USERS = "Users",
            FIREBASE_DB_TBL_USERS_COLUMN_NAME = "name",
            FIREBASE_DB_TBL_USERS_COLUMN_PHONE = "phone",
            FIREBASE_DB_TBL_USERS_COLUMN_USERLEVEL = "userlevel",

            FIREBASE_DB_TBL_PRODUCTS = "Products",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_ROW_ID= "rowId",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE = "image",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU = "sku",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE = "title",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_DESCRIPTION = "description",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE = "price",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_RATING = "rating",
            FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY = "postedBy",

            FIREBASE_DB_TBL_ORDERS = "Orders",
            FIREBASE_DB_TBL_ORDERS_COLUMN_DATE = "orderDate",
            FIREBASE_DB_TBL_ORDERS_COLUMN_STATUS = "orderStatus",

    //preference data
            PREF_DATA_NAME = "name",
            PREF_DATA_PHONE_NO = "phone",
            PREF_DATA_EMAIL = "email",
            PREF_DATA_USERLEVEL = "userlevel";

    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private SharedPreferences sharedPreferences;

    private Context appContext;

    public Utils(Context context){
        super();
        appContext = context;
        sharedPreferences = appContext.getSharedPreferences(APP_SHARED_PREF, MODE_PRIVATE);
    }

    public String getStringResource(int resID){
        return appContext.getResources().getString(resID);
    }

    public Drawable getDrawableResource(int resID){
        return appContext.getDrawable(resID);
    }

    public String sanitizePhoneNumber(String phone) {

        if (phone.equals("")) {
            return "";
        }

        if (phone.length() < 11 & phone.startsWith("0")) {
            String p = phone.replaceFirst("^0", "254");
            return p;
        }
        if (phone.length() == 13 && phone.startsWith("+")) {
            String p = phone.replaceFirst("^+", "");
            return p;
        }
        return phone;
    }

    public static boolean validateEmail(final String hex) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(hex);
        return (matcher.matches());
    }

    public void onEditTextChangeListener(final EditText eTxt, final TextView txtView){
        eTxt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                txtView.setVisibility(View.VISIBLE);
                if(isEmptyString(eTxt.getText().toString())) {
                    txtView.setVisibility(View.GONE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(!isEmptyString(eTxt.getText().toString())) {
                    txtView.setVisibility(View.VISIBLE);
                }
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

    }

    public void registerFCMReceiver(){

        FIREBASE_NOTIFICATION_RECEIVER = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction()!=null){
                    if (intent.getAction().equals(REGISTRATION_COMPLETE)) {
                        // gcm successfully registered
                        // now subscribe to `global` topic to receive app wide notifications
                        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_NOTIFICATION_TOPIC_GLOBAL);
                        getDeviceFcmID();

                    } else if (intent.getAction().equals(PUSH_NOTIFICATION)) {
                    /*String title = intent.getStringExtra(NOTIFICATION_TITLE);
                    String message = intent.getStringExtra(NOTIFICATION_MESSAGE);
                    int dialog_type = Integer.parseInt(intent.getStringExtra(NOTIFICATION_DIALOG_TYPE));

                    NotificationUtils.createNotification(appContext.getApplicationContext(), message);
                    showFeedbackSweetDialog(title, message, dialog_type, null);*/
                    }

                }
            }
        };
        Log.e("FCM-TOKEN",getDeviceFcmID());
    }

    public static boolean isEmptyString(String str){
        return TextUtils.isEmpty(str);
    }

    public static Dialog createDialog(Context context, int layout){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(layout);
        dialog.setCancelable(true);

        return dialog;
    }


    //TODO: Read/Write prefrerence------------------------------------------------------------------
    public void writeStringPref(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String readStringPref(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void deletePref(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public String getUserName() {
        return readStringPref(PREF_DATA_NAME);
    }

    public String getUserPhone() {
        return readStringPref(PREF_DATA_PHONE_NO);
    }

    public String getUserEmail() {
        return readStringPref(PREF_DATA_EMAIL);
    }

    public String getUserLevel() {
        return readStringPref(PREF_DATA_USERLEVEL);
    }

    //TODO: Device infos tags-----------------------------------------------------------------------
    public String getDeviceFcmID() {
        String mFireBaseToken = readStringPref(FIREBASE_DEVICE_TOKEN);
        if (isEmptyString(mFireBaseToken)) {
            mFireBaseToken = FirebaseInstanceId.getInstance().getToken();
            writeStringPref(FIREBASE_DEVICE_TOKEN, mFireBaseToken);
        }
        //Log.e("TAG-FCM-TOKEN", mFireBaseRegId);
        return mFireBaseToken;
    }

    public static void showResponseDialog(final Context context, int code, final String msg){

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_response);
        dialog.setCancelable(false);
        dialog.show();

        ImageView image = dialog.findViewById(R.id.image);
        TextView message = dialog.findViewById(R.id.message);
        Button btnOk = dialog.findViewById(R.id.btn_ok);

        image.setBackgroundResource(R.drawable.error);
        if(code == 1){
            image.setBackgroundResource(R.drawable.success);
        }
        message.setText(msg);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public static String formatAmount(double amount){
        return NumberFormat.getNumberInstance().format(amount);
    }

    public static void showChangeLanguageDialog(final Activity appActivity) {
        final Dialog dialog = createDialog(appActivity, R.layout.dialog_change_language);
        dialog.show();

        final RadioButton chk_english = (RadioButton) dialog.findViewById(R.id.chk_english);
        final RadioButton chk_swahili = (RadioButton) dialog.findViewById(R.id.chk_swahili);

        String userLanguage = LanguageHelper.getLanguage(appActivity);
        chk_english.setChecked(userLanguage.equals(USER_LANGUAGE_EN));
        chk_swahili.setChecked(userLanguage.equals(USER_LANGUAGE_SW));


        chk_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chk_english.setChecked(true);
                chk_swahili.setChecked(false);
                changeLanguage(appActivity, USER_LANGUAGE_EN);
                dialog.dismiss();
            }
        });

        chk_swahili.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chk_swahili.setChecked(true);
                chk_english.setChecked(false);
                changeLanguage(appActivity, USER_LANGUAGE_SW);
                dialog.dismiss();
            }
        });
    }

    private static void changeLanguage(Activity appActivity, String LanguageCode){
        LanguageHelper.setLocale(appActivity, LanguageCode);
        //It is required to recreate the activity to reflect the change in UI.
        appActivity.recreate();
    }

}
