package com.cafeyvinowinebar.Administrador.POJOs;

import com.google.firebase.Timestamp;

public class Gift {

    String nombre, user, userId, mesa;
    boolean servido;
    long precio;
    Timestamp timestamp;

    public Gift(){}

    public String getNombre() {
        return nombre;
    }

    public String getUser() {
        return user;
    }

    public String getUserId() {
        return userId;
    }

    public String getMesa() {
        return mesa;
    }

    public boolean isServido() {
        return servido;
    }

    public long getPrecio() {
        return precio;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
