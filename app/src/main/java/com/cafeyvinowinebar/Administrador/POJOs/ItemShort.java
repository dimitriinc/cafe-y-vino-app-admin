package com.cafeyvinowinebar.Administrador.POJOs;

public class ItemShort {
    String name, category;
    long price, count;


    public ItemShort(String name, String category, long price, long count) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.count = count;
    }

    public ItemShort() {}


    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public long getPrice() {
        return price;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
