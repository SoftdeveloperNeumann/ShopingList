package com.example.softdeveloper.shopinglist;

/**
 * Created by softdeveloper on 28.02.2017.
 */

public class ShoppingMemo {
    private String product;
    private int quantity;
    private long id;
    private boolean checked;

    public ShoppingMemo(long id, String product, int quantity, boolean checked) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.checked = checked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return quantity + "   " + product ;
    }
}
