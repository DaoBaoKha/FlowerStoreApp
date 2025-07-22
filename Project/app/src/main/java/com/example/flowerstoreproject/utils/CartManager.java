package com.example.flowerstoreproject.utils;

import com.example.flowerstoreproject.model.CartItem;
import com.example.flowerstoreproject.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                if (item.getQuantity() < product.getStock()) {
                    item.increaseQuantity();
                }
                updateCartIcon();
                return;
            }
        }
        cartItems.add(new CartItem(product, 1));
        updateCartIcon();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public int getCartSize() {
        return cartItems.size(); // số loại mặt hàng
    }

    public void clearCart() {
        cartItems.clear();
        updateCartIcon();
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
        updateCartIcon();
    }

    private void updateCartIcon() {
        // Bạn có thể gọi lại MainActivity để cập nhật UI nếu muốn
    }

    public int getCartItemCount() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

}
