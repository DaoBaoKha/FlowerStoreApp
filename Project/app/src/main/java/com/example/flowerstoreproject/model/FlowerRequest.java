package com.example.flowerstoreproject.model;

import com.google.gson.annotations.SerializedName;

public class FlowerRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;

    @SerializedName("description")
    private String description;

    @SerializedName("image")
    private String image;

    @SerializedName("category")
    private String category; // categoryId for POST request

    @SerializedName("stock")
    private int stock;

    public FlowerRequest(String name, double price, String description, String image, String category, int stock) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
        this.category = category;
        this.stock = stock;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}