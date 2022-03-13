package com.cafeyvinowinebar.Administrador;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cafeyvinowinebar.Administrador.Interfaces.ProductDao;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;

@Database(entities = {ProductEntity.class}, version = 1)
public abstract class ProductsDatabase extends RoomDatabase {

    private static ProductsDatabase instance;
    public abstract ProductDao productDao();
    public static synchronized ProductsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    ProductsDatabase.class, "products_database")
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build();
        }
        return instance;
    }
}
