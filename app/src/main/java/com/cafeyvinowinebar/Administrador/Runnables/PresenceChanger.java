package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Updates the customer's status to 'not present'
 * Unblocks their table in the mesas collection
 */
public class PresenceChanger implements Runnable{

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final DocumentSnapshot snapshot;

    public PresenceChanger(DocumentSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void run() {
        snapshot.getReference().update(Utils.KEY_MESA, "00");
        snapshot.getReference().update(Utils.IS_PRESENT, false);

        fStore.collection("mesas").whereEqualTo(Utils.KEY_NAME, snapshot.getString(Utils.KEY_MESA)).get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().update("blocked", false);
                    }
                });
    }
}
