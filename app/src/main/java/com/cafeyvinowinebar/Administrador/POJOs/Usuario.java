package com.cafeyvinowinebar.Administrador.POJOs;

public class Usuario {
    String nombre;
    String mesa;
    String email;
    boolean isPresent;
    String token;
    String fechaDeNacimiento;
    Long bonos;
    String teléfono;

    public Usuario(){}

    public Usuario(String nombre, String mesa, String email, boolean isPresent, String token, String fechaDeNacimiento, Long bonos, String teléfono) {
        this.nombre = nombre;
        this.mesa = mesa;
        this.email = email;
        this.isPresent = isPresent;
        this.token = token;
        this.fechaDeNacimiento = fechaDeNacimiento;
        this.bonos = bonos;
        this.teléfono = teléfono;
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFechaDeNacimiento() {
        return fechaDeNacimiento;
    }

    public void setFechaDeNacimiento(String fechaDeNacimiento) {
        this.fechaDeNacimiento = fechaDeNacimiento;
    }

    public Long getBonos() {
        return bonos;
    }

    public void setBonos(Long bonos) {
        this.bonos = bonos;
    }

    public String getTeléfono() {
        return teléfono;
    }

    public void setTeléfono(String teléfono) {
        this.teléfono = teléfono;
    }
}
