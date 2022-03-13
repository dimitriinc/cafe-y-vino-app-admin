package com.cafeyvinowinebar.Administrador.Interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.cafeyvinowinebar.Administrador.POJOs.MesaEntity;

import java.util.List;

@Dao
public interface MesaDao {

    @Insert
    void insert(MesaEntity mesa);

    @Delete
    void delete(MesaEntity mesa);

    @Query("DELETE FROM mesas WHERE mesa = :mesa")
    void deleteByName(String mesa);

    @Query("SELECT * FROM mesas ORDER BY id ASC")
    LiveData<List<MesaEntity>> getAllMesas();
}
