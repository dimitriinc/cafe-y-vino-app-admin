package com.cafeyvinowinebar.Administrador.POJOs;

public class Reserva {

    private String nombre;
    private String telefono;
    private String pax;
    private String hora;
    private String userId;
    private String fecha;
    private String mesa;
    private String comentario;
    private boolean llegado;
    private boolean confirmado;

    public Reserva() {}

    public Reserva(String nombre, String telefono, String pax, String hora, String comentario, boolean llegado, String userId, boolean confirmado) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.pax = pax;
        this.hora = hora;
        this.comentario = comentario;
        this.llegado = llegado;
        this.userId = userId;
        this.confirmado = confirmado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPax() {
        return pax;
    }

    public String getHora() {
        return hora;
    }

    public String getMesa() {
        return mesa;
    }

    public String getUserId() {
        return userId;
    }

    public String getFecha() {
        return fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public boolean isLlegado() {
        return llegado;
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}
