package com.urban.sisters.activity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.urban.sisters.R;
import com.urban.sisters.models.ProductsModel;
import com.urban.sisters.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS;
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
import static com.urban.sisters.utils.Utils.createDialog;

public class AdminPanel extends AppCompatActivity {

    private ImageButton image;
    private static final int GALLERY_REQUEST_CODE = 2;
    private Uri imageFromSDCard = null;
    private EditText title, description, price;
    private TextView hint_title, hint_description, hint_price;
    private Button submit;
    private StorageReference imageStorage;
    private DatabaseReference dbTblProducts;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private Dialog pDialog;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_admin);

        utils = new Utils(this);

        setTitleBar(utils.getStringResource(R.string.admin_panel));

        //initializing layout
        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        hint_title = findViewById(R.id.hint_title);
        description = findViewById(R.id.description);
        hint_description = findViewById(R.id.hint_description);
        price = findViewById(R.id.price);
        hint_price = findViewById(R.id.hint_price);
        submit = findViewById(R.id.submit);

        price.setTransformationMethod(null);
        utils.onEditTextChangeListener(title, hint_title);
        utils.onEditTextChangeListener(description, hint_description);
        utils.onEditTextChangeListener(price, hint_price);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        imageStorage = FirebaseStorage.getInstance().getReference().child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE);
        dbTblProducts = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_TBL_PRODUCTS);

        //picking image from gallery
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        // posting to Firebase
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndPostData();
            }
        });
    }

    void validateAndPostData(){

        Long stamp = System.currentTimeMillis()/1000;
        final String SKU = stamp.toString();

        final String Title = title.getText().toString().trim();
        final String Description = description.getText().toString().trim();
        final String Price = price.getText().toString().trim();
        final String Rating = "1";
        final String PostedBy = mCurrentUser.getUid();


        if (imageFromSDCard == null) {
            Toast.makeText(getApplicationContext(), utils.getStringResource(R.string.image_required), Toast.LENGTH_SHORT).show();
        }
        else if (utils.isEmptyString(Title)) {
            title.setError(utils.getStringResource(R.string.title_required));
        }
        else if (utils.isEmptyString(Price)) {
            price.setError(utils.getStringResource(R.string.price_required));
        }
        else if (utils.isEmptyString(Description)) {
            description.setError(utils.getStringResource(R.string.description_required));
        }
        else {

            pDialog = createDialog(AdminPanel.this, R.layout.dialog_progress);
            pDialog.setCancelable(false);
            pDialog.show();

            final String imageName = setImageName(imageFromSDCard);
            imageStorage.child(imageName).putFile(imageFromSDCard).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String pushId = dbTblProducts.push().getKey();
                    DatabaseReference insertData = dbTblProducts.child(pushId);

                    Map<String, String> param = new HashMap<>();
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE, imageName);
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_SKU, SKU);
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_TITLE, Title);
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_DESCRIPTION, Description);
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_PRICE, Price);
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_RATING, Rating);
                    param.put(FIREBASE_DB_TBL_PRODUCTS_COLUMN_POSTED_BY, PostedBy);
                    insertData.setValue(param);

                    if(pDialog != null)
                        pDialog.dismiss();

                    final Dialog responseDialog = createDialog(AdminPanel.this, R.layout.dialog_response);
                    responseDialog.setCancelable(false);
                    responseDialog.show();
                    ImageView iv = responseDialog.findViewById(R.id.image);
                    TextView tv = responseDialog.findViewById(R.id.message);
                    Button btn = responseDialog.findViewById(R.id.btn_ok);

                    iv.setBackgroundResource(R.drawable.success);
                    tv.setText(utils.getStringResource(R.string.uploaded_successfully));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            responseDialog.dismiss();
                            image.setImageURI(null);
                            image.setImageResource(R.drawable.upload);
                            title.setText(null);
                            description.setText(null);
                            price.setText(null);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    pDialog.dismiss();
                    Log.e("TAG-ERROR", e.getMessage());
                }
            });
        }
    }

    private String setImageName(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String imageName = System.currentTimeMillis() + "." + mime.getExtensionFromMimeType(cr.getType(uri));
        return imageName;
    }

    @Override
    // image from gallery result
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            imageFromSDCard = data.getData();
            image.setImageURI(imageFromSDCard);
        }
    }

    private void setTitleBar(String title){
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
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
