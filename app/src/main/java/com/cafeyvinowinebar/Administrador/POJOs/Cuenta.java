package com.cafeyvinowinebar.Administrador.POJOs;

public class Cuenta {
    String name, mesa;
    public boolean isExpanded;

    public Cuenta() {
    }

    public Cuenta(String name, String mesa, boolean isExpanded) {
        this.name = name;
        this.mesa = mesa;
        this.isExpanded = isExpanded;
    }

    public String getName() {
        return name;
    }

    public String getMesa() {
        return mesa;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
