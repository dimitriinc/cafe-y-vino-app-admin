package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

/**
 * Decrements the count value of a product
 * If the value reaches 0, deletes the document
 */
public class ItemPedidoDecrementor implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final DocumentSnapshot snapshot;

    public ItemPedidoDecrementor(DocumentSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void run() {

        DocumentReference reference = snapshot.getReference();

        fStore.runTransaction((Transaction.Function<Void>) transaction -> {

            DocumentSnapshot snap = transaction.get(reference);

            long count = snap.getLong(Utils.KEY_COUNT) - 1;
            if (count != 0) {
                transaction.update(reference, Utils.KEY_COUNT, count);
            } else {
                transaction.delete(reference);
            }
            return null;

        });
    }

}
