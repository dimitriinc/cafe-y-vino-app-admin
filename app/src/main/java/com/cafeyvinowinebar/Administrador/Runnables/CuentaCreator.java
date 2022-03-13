package com.cafeyvinowinebar.Administrador.Runnables;


import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaItem;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.concurrent.CountDownLatch;

/**
 * In the process of creating/updating the cuenta collection
 * When a pedido product is on its way to become a cuenta product
 * Checks if a cuenta product of the same name already exists in the cuenta collection
 * If it does, increments the count
 * If it doesn't creates a new document, and submits a background task to populate the cuenta meta doc
 */
public class CuentaCreator implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final DocumentReference reference;
    private final CountDownLatch latch;
    private final String name;
    public String userName, userMesa, userId, currentDate;
    private final long price, count;

    public CuentaCreator(DocumentReference reference, CountDownLatch latch, String name,
                         String userName, String userMesa, String userId, String currentDate,
                         long price, long count) {

        this.reference = reference;
        this.latch = latch;
        this.name = name;
        this.userName = userName;
        this.userMesa = userMesa;
        this.userId = userId;
        this.currentDate = currentDate;
        this.price = price;
        this.count = count;
    }

    @Override
    public void run() {
        reference.get()
                .addOnSuccessListener(App.executor, documentSnapshot -> {

                    // get a snapshot of the reference to see if an item of the name is present in the cuenta collection
                    reference.get().addOnSuccessListener(App.executor, snapshot1 -> {
                        if (snapshot1.exists()) {

                            // an item of the name is in the cuenta, we increment its count and update the total price using a transaction
                            fStore.runTransaction((Transaction.Function<Void>) transaction -> {
                                DocumentSnapshot snap = transaction.get(reference);
                                long count1 = snap.getLong(Utils.KEY_COUNT) + count;
                                transaction.update(reference, Utils.KEY_COUNT, count1);
                                transaction.update(reference, Utils.TOTAL, count1 * price);
                                return null;
                            });

                            // release the latch
                            latch.countDown();

                        } else {

                            // there is no item of the name in the cuenta collection, we create one, and on success we release the latch
                            // and create a meta document for the cuenta, but only if the item is the first to enter the collection
                            reference.set(new CuentaItem(name, count, price, count * price))
                                    .addOnSuccessListener(App.executor, unused -> latch.countDown());
                            App.executor.submit(new CuentaMetaDocSetter(userName, userMesa, userId, currentDate));
                        }
                    });

                });
    }
}
