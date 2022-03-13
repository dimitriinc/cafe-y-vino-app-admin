package com.cafeyvinowinebar.Administrador.POJOs;

public class Mesa {
    String user, mesa, userId, count;
    public boolean servido, servidoCocina, servidoBarra, isExpanded;

    public Mesa(String user, String mesa, String userId, String count, boolean servido, boolean servidoCocina, boolean servidoBarra, boolean isExpanded) {
        this.user = user;
        this.mesa = mesa;
        this.userId = userId;
        this.count = count;
        this.servido = servido;
        this.servidoCocina = servidoCocina;
        this.servidoBarra = servidoBarra;
        this.isExpanded = isExpanded;
    }

    public Mesa() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMesa() {
        return mesa;
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

    public boolean isServido() {
        return servido;
    }

    public void setServido(boolean servido) {
        this.servido = servido;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public boolean isServidoCocina() {
        return servidoCocina;
    }

    public void setServidoCocina(boolean servidoCocina) {
        this.servidoCocina = servidoCocina;
    }

    public boolean isServidoBarra() {
        return servidoBarra;
    }

    public void setServidoBarra(boolean servidoBarra) {
        this.servidoBarra = servidoBarra;
    }
}
