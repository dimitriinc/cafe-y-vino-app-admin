package com.cafeyvinowinebar.Administrador.POJOs;

public class Usuario {
    String nombre;
    String mesa;
    String email;
    public Usuario(){}

    public Usuario(String nombre, String mesa) {
        this.nombre = nombre;
        this.mesa = mesa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMesa() {
        return mesa;
    }

    public String getEmail() {
        return email;
    }
}
