package com.cafeyvinowinebar.Administrador.POJOs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "products")
public class ProductEntity {

    @PrimaryKey
    @NonNull
    private String name;
    private String category;
    private long count;
    private long price;

    public ProductEntity(@NonNull String name, String category, long count, long price) {
        this.name = name;
        this.category = category;
        this.count = count;
        this.price = price;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
