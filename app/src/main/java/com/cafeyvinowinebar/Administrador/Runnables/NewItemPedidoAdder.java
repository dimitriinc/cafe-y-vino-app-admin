package com.cafeyvinowinebar.Administrador.Runnables;

import android.text.TextUtils;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.ItemShort;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Adds a new custom product to a pedido collection
 */
public class NewItemPedidoAdder implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final String name, path, category;
    private final long price, count;

    public NewItemPedidoAdder(String name, String path, long price, String category, long count) {
        this.name = name;
        this.path = path;
        this.price = price;
        this.category = category;
        this.count = count;
    }

    @Override
    public void run() {

        // with the constructor we receive the path of the meta doc for the order
        // to get the path of the collection itself we concatenate a little string to it
        String collectionPath = path + "/pedido";

        fStore.collection(collectionPath).add(new ItemShort(name, category, price, count));

        DocumentReference docReference = fStore.document(path);
        docReference.get().addOnSuccessListener(App.executor, snapshot -> {

            if (TextUtils.equals(category, Utils.BARRA)) {

                // the new product is a bar product
                if (snapshot.getBoolean(Utils.SERVIDO_BARRA)) {

                    // this means that it's the first bar product in the order
                    // so we update the 'servido' field in the meta doc to display it in the 'barra' mode
                    docReference.update(Utils.SERVIDO_BARRA, false);
                }
            } else if (TextUtils.equals(category, Utils.COCINA)) {

                // the new product is a kitchen product
                if (snapshot.getBoolean(Utils.SERVIDO_COCINA)) {

                    // it's the first kitchen product in the order
                    // update the 'servido' field
                    docReference.update(Utils.SERVIDO_COCINA, false);
                }
            }
        });
    }
}
