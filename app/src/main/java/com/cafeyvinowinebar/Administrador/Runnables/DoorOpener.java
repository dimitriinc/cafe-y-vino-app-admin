package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoorOpener implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final String userId, mesa;

    public DoorOpener(String userId, String mesa) {
        this.userId = userId;
        this.mesa = mesa;
    }

    @Override
    public void run() {
        DocumentReference doc = fStore.collection(Utils.USUARIOS)
                .document(userId);
        doc.update(Utils.IS_PRESENT, true);
        doc.update(Utils.KEY_MESA, mesa);
    }
}
