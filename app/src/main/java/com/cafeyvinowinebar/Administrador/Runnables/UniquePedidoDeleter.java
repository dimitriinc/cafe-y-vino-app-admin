package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * In case administrator deletes a pedido, and it's the only pedido, assigned to a client of the app,
 * We should unblock the table in the mesas collection
 */
public class UniquePedidoDeleter implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final DocumentSnapshot snapshot;

    public UniquePedidoDeleter(DocumentSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void run() {

        Query queryPedidos = fStore.collection("pedidos")
                .document(Utils.getCurrentDate())
                .collection("pedidos enviados")
                .whereEqualTo(Utils.KEY_USER_ID, snapshot.getString(Utils.KEY_USER_ID));
        Query queryCuentas = fStore.collection("cuentas")
                .document(Utils.getCurrentDate())
                .collection("cuentas corrientes")
                .whereEqualTo(Utils.KEY_USER_ID, snapshot.getString(Utils.KEY_USER_ID));

        queryPedidos.get().addOnSuccessListener(App.executor, pedidosQuerySnapshot -> {

            if (pedidosQuerySnapshot.isEmpty()) {

                // client has no other pedidos, so we check if they have a cuenta to their name
                queryCuentas.get().addOnSuccessListener(App.executor, cuentaQuerySnapshot -> {

                    if (cuentaQuerySnapshot.isEmpty()) {

                        // there is no cuentas either
                        // we unblock the table
                        fStore.collection("mesas").whereEqualTo(Utils.KEY_NAME, snapshot.getString(Utils.KEY_MESA))
                                .get().addOnSuccessListener(App.executor, queryDocumentSnapshots1 -> {

                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots1) {
                                        doc.getReference().update("blocked", false);
                                        doc.getReference().update("present", false);
                                    }
                                });

                        // and we close the session if the pedido or cuenta belongs to a user of the app
                        fStore.collection(Utils.USUARIOS).document(snapshot.getString(Utils.KEY_USER_ID))
                                .get().addOnSuccessListener(App.executor, documentSnapshot -> {

                                    if (documentSnapshot.exists()) {
                                        documentSnapshot.getReference().update(Utils.IS_PRESENT, false);
                                        documentSnapshot.getReference().update(Utils.KEY_MESA, "00");
                                    }
                                });
                    }
                });
            }
        });

    }
}
