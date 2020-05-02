package com.urban.sisters.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.urban.sisters.R;
import com.urban.sisters.models.EmptyModel;
import com.urban.sisters.models.OrdersModel;
import com.urban.sisters.models.ProductsModel;
import com.urban.sisters.utils.Utils;

import java.util.ArrayList;

import static com.urban.sisters.utils.Utils.FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE;


public abstract class ProductsAdapter extends RecyclerView.Adapter {

    private int NO_DATA_VIEW = 1;
    private int SHOP_VIEW = 2;
    private int ORDER_VIEW = 3;
    private Context context;
    private ArrayList<Object> dataArray;

    private StorageReference imageStorage;
    private static final String TAG = "ProductsAdapter";

    public ProductsAdapter(Context context, ArrayList<Object> itemsArray) {
        this.context = context;
        this.dataArray = itemsArray;
    }

    @Override
    public int getItemViewType(int position) {

        if (dataArray.get(position) instanceof ProductsModel) {
            return SHOP_VIEW;
        }if (dataArray.get(position) instanceof OrdersModel) {
            return ORDER_VIEW;
        }

        return NO_DATA_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        Log.e(TAG, "onCreateViewHolder: "+viewType );
        if (viewType == SHOP_VIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_products_recyclyer, parent, false);
            viewHolder = new ShopHolder(view);
        }
        else if (viewType == ORDER_VIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_orders_recycler, parent, false);
            viewHolder = new OrdersHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_no_data_recycler, parent, false);
            viewHolder = new EmptyHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof OrdersHolder) {

            Utils utils = new Utils(context);

            final OrdersHolder ordersHolder = (OrdersHolder) holder;
            OrdersModel ordersData = (OrdersModel) dataArray.get(position);
            Log.e(TAG, "onCreateViewHolder: OrdersHolder"+ordersData.getTitle() );
            ordersHolder.sku.setText("sku "+ordersData.getSku());
            ordersHolder.title.setText(ordersData.getTitle());
            ordersHolder.price.setText("Ksh "+ordersData.getPrice());
            ordersHolder.date.setText("Date "+ordersData.getOrderDate());

            ordersHolder.completeOrder.setVisibility(View.GONE);
            if(utils.getUserLevel().equals("admin")) {
                ordersHolder.completeOrder.setVisibility(View.VISIBLE);

                if(ordersData.getOrderStatus().equals("Completed"))
                    ordersHolder.completeOrder.setVisibility(View.GONE);
            }

            ordersHolder.completeOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem(position);
                }
            });


        }
        else if (holder instanceof ShopHolder) {
            Log.e(TAG, "onCreateViewHolder: ShopHolder" );

            final ShopHolder shopHolder = (ShopHolder) holder;
            ProductsModel productData = (ProductsModel) dataArray.get(position);
            shopHolder.sku.setText(productData.getSku());
            shopHolder.title.setText(productData.getTitle());
            shopHolder.description.setText(productData.getDescription());
            shopHolder.price.setText("Ksh "+productData.getPrice());
            shopHolder.rating.setRating(Integer.valueOf(productData.getRating()));

            shopHolder.buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem(position);
                }
            });

            imageStorage = FirebaseStorage.getInstance().getReference().child(FIREBASE_DB_TBL_PRODUCTS_COLUMN_IMAGE).child(productData.getImage());

            imageStorage.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).into(shopHolder.image);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Glide.with(context).load(R.drawable.ic_launcher).into(shopHolder.image);
                        }
                    });
        }
        else if (holder instanceof EmptyHolder){
            Log.e(TAG, "onCreateViewHolder: EmptyHolder" );
            /*EmptyHolder emptyHolder = (EmptyHolder) holder;
            EmptyModel emptyModel = (EmptyModel) dataArray.get(position);
            emptyHolder.message.setText(emptyModel.getMessage());*/
        }
        else {
            Log.e(TAG, "onCreateViewHolder: other" );
//            EmptyHolder emptyHolder = (EmptyHolder) holder;
//            EmptyModel emptyModel = (EmptyModel) dataArray.get(position);
//            emptyHolder.message.setText(emptyModel.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return dataArray.size();
    }

    public abstract void selectedItem(int position);

    protected static class EmptyHolder extends RecyclerView.ViewHolder {
        private TextView message;

        EmptyHolder(View view) {
            super(view);
            message = view.findViewById(R.id.message);
        }
    }

    protected static class OrdersHolder extends RecyclerView.ViewHolder {
        private TextView title, sku, date, price;
        private Button completeOrder;

        OrdersHolder(View view) {
            super(view);

            title = view.findViewById(R.id.title);
            sku = view.findViewById(R.id.sku);
            date = view.findViewById(R.id.date);
            price = view.findViewById(R.id.price);
            completeOrder = view.findViewById(R.id.complete);
        }
    }

    protected class ShopHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView sku, title, description, price;
        private RatingBar rating;
        private Button buy;

        public ShopHolder(View view) {
            super(view);

            image = view.findViewById(R.id.image);
            sku = view.findViewById(R.id.sku);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            price = view.findViewById(R.id.price);
            rating = view.findViewById(R.id.rating);
            buy = view.findViewById(R.id.buy);
        }
    }
    protected class OrderHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView sku, title, description, price;
        private RatingBar rating;
        private Button buy;

        public OrderHolder(View view) {
            super(view);

            image = view.findViewById(R.id.image);
            sku = view.findViewById(R.id.sku);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            price = view.findViewById(R.id.price);
            rating = view.findViewById(R.id.rating);
            buy = view.findViewById(R.id.buy);
        }
    }
}

