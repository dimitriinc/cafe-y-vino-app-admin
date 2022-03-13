package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The task is performed when admin cancels a gift request
 * We return the spent bonus points to the client
 * And delete the document for the gift canceled
 */
public class GiftBackoff implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final DocumentSnapshot snapshot;

    public GiftBackoff(DocumentSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void run() {

        String userId = snapshot.getString(Utils.KEY_USER_ID);
        Long precio = snapshot.getLong(Utils.KEY_PRECIO);
        assert userId != null && precio != null;
        fStore.collection(Utils.USUARIOS)
                .document(userId)
                .update(Utils.KEY_BONOS, FieldValue.increment(precio));
        snapshot.getReference().delete();
    }
}
