package com.cafeyvinowinebar.Administrador.Runnables;

import android.os.Handler;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCuentasNested;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class CuentaItemPlusOne implements Runnable {
    DocumentSnapshot snapshot;
    AdapterCuentasNested adapter;
    int position;
    DocumentReference reference;
    Handler mainHandler;
    final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public CuentaItemPlusOne(DocumentSnapshot snapshot, AdapterCuentasNested adapter, int position, Handler mainHandler) {
        this.snapshot = snapshot;
        this.adapter = adapter;
        this.position = position;
        this.mainHandler = mainHandler;
    }

    @Override
    public void run() {
        reference = snapshot.getReference();

        fStore.runTransaction((Transaction.Function<Void>) transaction -> {

            DocumentSnapshot snap = transaction.get(reference);
            long price = snap.getLong(Utils.PRICE);
            long count = snap.getLong(Utils.KEY_COUNT) + 1;
            transaction.update(reference, Utils.KEY_COUNT, count);
            transaction.update(reference, Utils.TOTAL, count * price);
            return null;

        });

    }
}
