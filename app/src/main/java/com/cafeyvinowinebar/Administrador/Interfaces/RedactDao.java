package com.cafeyvinowinebar.Administrador.Interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;

import java.util.List;

@Dao
public interface RedactDao {

    @Insert
    void insert(RedactEntity product);

    @Query("DELETE FROM products_to_redact")
    void deleteAll();

    @Query("SELECT * FROM products_to_redact ORDER BY name ASC")
    LiveData<List<RedactEntity>> getAllProducts();

    @Query("UPDATE products_to_redact SET count = :count WHERE name = :name")
    void redact(String name, long count);
}
