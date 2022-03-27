package com.cafeyvinowinebar.Administrador;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cafeyvinowinebar.Administrador.Interfaces.RedactDao;
import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;

@Database(entities = {RedactEntity.class}, version = 1)
public abstract class RedactDatabase extends RoomDatabase {

    private static RedactDatabase instance;
    public abstract RedactDao redactDao();
    public static synchronized RedactDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    RedactDatabase.class, "redact_databse")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
