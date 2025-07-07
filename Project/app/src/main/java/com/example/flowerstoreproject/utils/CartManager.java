package com.example.flowerstoreproject.utils;

import com.example.flowerstoreproject.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<Product> cartItems = new ArrayList<>();
    private int cartItemCount = 0; // Số lượng sản phẩm trong giỏ để hiển thị trên icon

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product) {
        boolean found = false;
        for (Product item : cartItems) {
            if (item.getId().equals(product.getId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(product);
            cartItemCount++; // Tăng số lượng khi thêm sản phẩm mới
        }
        // Gọi phương thức để cập nhật giao diện (sẽ triển khai trong MainActivity)
        updateCartIcon();
    }

    public List<Product> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getCartItemCount() {
        return cartItemCount;
    }

    public void clearCart() {
        cartItems.clear();
        cartItemCount = 0;
        updateCartIcon();
    }

    // Phương thức để thông báo cập nhật icon giỏ hàng
    private void updateCartIcon() {
        // Sẽ được gọi từ MainActivity để cập nhật giao diện
    }
}