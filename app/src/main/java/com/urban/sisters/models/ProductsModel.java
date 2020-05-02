package com.urban.sisters.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ProductsModel {
    public  String image, sku, title, description, price, rating, postedBy;

    public ProductsModel(){
    }

    public ProductsModel(String image, String sku, String title, String description, String price, String rating, String postedBy){
        this.image = image;
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.postedBy = postedBy;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
       return image;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostedBy() {
        return postedBy;
    }
}

