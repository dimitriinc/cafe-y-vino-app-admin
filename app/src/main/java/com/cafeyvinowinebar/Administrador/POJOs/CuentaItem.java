package com.cafeyvinowinebar.Administrador.POJOs;

public class CuentaItem {
    String name;
    long count, price, total;

    public CuentaItem() {
    }

    public CuentaItem(String name, long count, long price, long total) {
        this.name = name;
        this.count = count;
        this.price = price;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public long getPrice() {
        return price;
    }

    public long getTotal() {
        return total;
    }
}
