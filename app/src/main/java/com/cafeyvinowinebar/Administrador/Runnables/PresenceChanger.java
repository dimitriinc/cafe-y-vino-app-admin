package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Updates the customer's status to 'not present'
 */
public class PresenceChanger implements Runnable{

    private final DocumentSnapshot snapshot;

    public PresenceChanger(DocumentSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void run() {
        snapshot.getReference().update(Utils.KEY_MESA, "00");
        snapshot.getReference().update(Utils.IS_PRESENT, false);
    }
}
