package com.cafeyvinowinebar.Administrador;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cafeyvinowinebar.Administrador.Interfaces.MesaDao;
import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;

@Database(entities = {MesaEntity.class}, version = 1)
public abstract class MesasDatabase extends RoomDatabase {

    private static MesasDatabase instance;
    public abstract MesaDao mesaDao();
    public static synchronized MesasDatabase getInstance(Context context) {
        if (instance == null) {

            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MesasDatabase.class, "mesas_database")
                    .addCallback(roomCallback)
                    .addMigrations(MIGRATION_1_2)
                    .build();

        }
        return instance;

    }

    private static final Migration MIGRATION_1_2 = new Migration(2, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // we don't want anything to happen on migration
        }
    };

    /**
     * This callback will execute when the database is created
     */
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            App.executor.submit(new PopulateDb(instance));
        }
    };

    /**
     * Initiates the database with 12 fixed tables on a background thread
     */
    @SuppressLint("DefaultLocale")
    private static class PopulateDb implements Runnable {

        private final MesaDao mesaDao;
        private PopulateDb(MesasDatabase db) {
            mesaDao = db.mesaDao();
        }
        @Override
        public void run() {
            for (int i = 0; i < 12; i++) {

                // the name must be of a certain format
                mesaDao.insert(new MesaEntity(String.format("%02d", i + 1), false));
            }
        }
    }

}
