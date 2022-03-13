package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Sets a new document in the cuenta collection
 */
public class NewItemCuentaAdder implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final String name, path;
    private final long count, price;

    public NewItemCuentaAdder(String name, String path, long count, long price) {
        this.name = name;
        this.path = path;
        this.count = count;
        this.price = price;
    }

    @Override
    public void run() {

        // note that instead of adding to the collection, we set on a document with the name as its id
        // the IDs as names are important for the bill items
        fStore.document(path + "/cuenta/" + name).set(new CuentaItem(name, count, price, count * price));
    }
}
