package com.cafeyvinowinebar.Administrador.POJOs;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "mesas")
public class MesaEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String mesa;

    // the mesa is blocked if occupied by a user of the client app (has pedidos or cuentas assigned to it)
    boolean blocked;

    boolean isPresent;

    public MesaEntity(String mesa, boolean blocked) {
        this.mesa = mesa;
        this.blocked = blocked;
        // when a mesa is created, it still doesn't have any pedidos, so the presence status must be false
        isPresent = false;
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

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }
}
