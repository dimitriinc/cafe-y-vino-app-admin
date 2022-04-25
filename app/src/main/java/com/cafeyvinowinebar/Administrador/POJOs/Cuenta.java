package com.cafeyvinowinebar.Administrador.POJOs;

public class Cuenta {
    String name, mesa, userId;
    public boolean isExpanded;

    public Cuenta() {
    }

    public Cuenta(String name, String mesa, String userId, boolean isExpanded) {
        this.name = name;
        this.mesa = mesa;
        this.userId = userId;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
