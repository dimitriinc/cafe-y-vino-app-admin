package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Deletes the pedido collection and the meta doc
 */
public class CollectionDeleter implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final DocumentSnapshot snapshot;
    private final String mode;

    public CollectionDeleter(DocumentSnapshot snapshot, String mode) {
        this.snapshot = snapshot;
        this.mode = mode;
    }

    @Override
    public void run() {

        // get a collection reference for the pedido collection
        fStore.collection(snapshot.getReference().getPath() + mode)
                .get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                    // and iterate though its documents, deleting them
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    // and delete the meta doc as well
                    snapshot.getReference().delete();
                });
    }
}
