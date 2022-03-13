package com.cafeyvinowinebar.Administrador.Interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(ProductEntity product);

    @Update
    void update(ProductEntity product);

    @Delete
    void delete(ProductEntity product);

    @Query("DELETE FROM products")
    void deleteAll();

    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<ProductEntity>> getAllProducts();

    @Query("SELECT * FROM products WHERE name = :name")
    ProductEntity getProduct(String name);

    @Query("UPDATE products SET count = :count WHERE name = :name")
    void increment(String name, long count);

}
