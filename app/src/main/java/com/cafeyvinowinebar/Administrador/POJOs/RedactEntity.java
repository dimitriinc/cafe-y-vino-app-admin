package com.cafeyvinowinebar.Administrador.POJOs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity (tableName = "products_to_redact")
public class RedactEntity {

    @PrimaryKey
    @NonNull
    String name;
    String category;
    long price, count, total;

    public RedactEntity(@NonNull String name, String category, long price, long count) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.count = count;
        total = 0;
    }
    @Ignore
    public RedactEntity(@NonNull String name, long price, long count, long total) {
        this.name = name;
        this.price = price;
        this.count = count;
        this.total = total;
        category = "";
    }

    @NonNull
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public long getPrice() {
        return price;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
