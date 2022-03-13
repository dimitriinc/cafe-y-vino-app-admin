package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Updates the mesa field in the user's document
 */
public class MesaSetter implements Runnable{

    private final DocumentSnapshot snapshot;
    private final String mesa;

    public MesaSetter(DocumentSnapshot snapshot, String mesa) {
        this.snapshot = snapshot;
        this.mesa = mesa;
    }

    @Override
    public void run() {
        snapshot.getReference().update(Utils.KEY_MESA, mesa);
    }
}
