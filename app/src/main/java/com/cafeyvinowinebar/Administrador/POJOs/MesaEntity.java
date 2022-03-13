package com.cafeyvinowinebar.Administrador.POJOs;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "mesas")
public class MesaEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String mesa;

    boolean blocked;

    public MesaEntity(String mesa, boolean blocked) {
        this.mesa = mesa;
        this.blocked = blocked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
