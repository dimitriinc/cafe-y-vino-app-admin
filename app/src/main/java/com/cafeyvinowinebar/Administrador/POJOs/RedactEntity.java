package com.cafeyvinowinebar.Administrador.POJOs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "products_to_redact")
public class RedactEntity {

    @PrimaryKey
    @NonNull
    String name;
    String category;
    long price, count;

    public RedactEntity(@NonNull String name, String category, long price, long count) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.count = count;
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

}
