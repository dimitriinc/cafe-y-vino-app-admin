package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * If the customer has unserved orders or an open bill
 * We change the table value on those documents
 */
public class MesaInCuentaChanger implements  Runnable {
    
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final String currentDate, userId, newMesa;

    public MesaInCuentaChanger(String currentDate, String userId, String newMesa) {
        this.currentDate = currentDate;
        this.userId = userId;
        this.newMesa = newMesa;
    }

    @Override
    public void run() {

        DocumentReference cuentaReference = fStore.collection("cuentas")
                .document(currentDate)
                .collection("cuentas corrientes")
                .document(userId);

        Query pedidosDelUser = fStore.collectionGroup("pedidos enviados")
                .whereEqualTo(Utils.KEY_USER_ID, userId)
                .whereEqualTo(Utils.SERVIDO, false);

        pedidosDelUser.get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

            // we check if the user has orders
            if (!queryDocumentSnapshots.isEmpty()) {

                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    if (!snapshot.getBoolean(Utils.SERVIDO))

                    // for each open order we change the table value
                    snapshot.getReference().update(Utils.KEY_MESA, newMesa);
                }
            }
        });

        cuentaReference.get().addOnSuccessListener(App.executor, snapshot -> {

            if (snapshot.exists()) {

                // if the customer already has an open bill, we update the table there as well
                snapshot.getReference().update(Utils.KEY_MESA, newMesa);
            }
        });
    }
}
