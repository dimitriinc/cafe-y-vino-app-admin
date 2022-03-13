package com.cafeyvinowinebar.Administrador.Runnables;

import android.os.Handler;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCuentasNested;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class CuentaItemDecremenator implements Runnable {
    DocumentReference reference;
    DocumentSnapshot snapshot;
    int position;
    AdapterCuentasNested adapter;
    Handler mainHandler;
    final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public CuentaItemDecremenator(DocumentSnapshot snapshot, int position, AdapterCuentasNested adapter, Handler mainHandler) {
        this.snapshot = snapshot;
        this.position = position;
        this.adapter = adapter;
        this.mainHandler = mainHandler;
    }

    @Override
    public void run() {
        reference = snapshot.getReference();

        fStore.runTransaction((Transaction.Function<Void>) transaction -> {

            DocumentSnapshot snap = transaction.get(reference);
            long price = snap.getLong(Utils.PRICE);
            long count = snap.getLong(Utils.KEY_COUNT) - 1;
            if (count != 0) {
                transaction.update(reference, Utils.KEY_COUNT, count);
                transaction.update(reference, Utils.TOTAL, count * price);
            } else {
                transaction.delete(reference);
            }
            return null;

        });
    }
}
