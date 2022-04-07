package com.cafeyvinowinebar.Administrador.POJOs;

public class Mesa {

    boolean isBlocked, isFixed, isPresent;
    String name;

    public Mesa(boolean isBlocked, boolean isFixed, boolean isPresent, String name) {
        this.isBlocked = isBlocked;
        this.isFixed = isFixed;
        this.isPresent = isPresent;
        this.name = name;
    }

    public Mesa() {}

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
