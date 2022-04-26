package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

/**
 * If the customer has unserved orders or an open bill
 * We change the table value on those documents
 */
public class MesaInCuentaChanger implements Runnable {

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

                            // for each open order we change the table value
                            snapshot.getReference().update(Utils.KEY_MESA, newMesa);

                            // if the pedido is a custom one, we also need to change the userId field
                            // since this field must have the value of the mesa's name in case of custom pedidos
                            if (Objects.equals(snapshot.getString(Utils.KEY_USER), "Cliente")) {
                                snapshot.getReference().update(Utils.KEY_USER_ID, newMesa);
                            }
                        }
                    }
                });

        cuentaReference.get().addOnSuccessListener(App.executor, snapshot -> {

            if (snapshot.exists()) {

                // if the customer already has an open bill, we update the table there as well
                snapshot.getReference().update(Utils.KEY_MESA, newMesa);

                // if the cuenta belongs to a customized table, we update its userId field
                // we copy the meta document and its collection of products
                // and delete the old one (we need the cuenta stored under its new id
                if (Objects.equals(snapshot.getString(Utils.KEY_NAME), "Cliente")) {

                    DocumentReference newCuentaMetaDoc = fStore.collection(Utils.CUENTAS)
                            .document(Utils.getCurrentDate())
                            .collection("cuentas corrientes")
                            .document(newMesa);

                    newCuentaMetaDoc.set(Objects.requireNonNull(snapshot.getData()))
                            .addOnSuccessListener(App.executor, unused ->
                                    fStore.collection(snapshot.getReference().getPath() + "/cuenta")
                                            .get()
                                            .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                                                CollectionReference newCuentaCollection =
                                                        fStore.collection(newCuentaMetaDoc.getPath() + "/cuenta");

                                                for (QueryDocumentSnapshot oldProduct : queryDocumentSnapshots) {

                                                    fStore.collection(newCuentaCollection.getPath())
                                                            .document(oldProduct.getId())
                                                            .set(oldProduct.getData());
                                                }

                                                snapshot.getReference().delete();

                                                // update the mesa and userId fields on the new MetaDoc
                                                newCuentaMetaDoc.update(Utils.KEY_MESA, newMesa);
                                                newCuentaMetaDoc.update(Utils.KEY_USER_ID, newMesa);
                                            }));

                }
            }
        });
    }
}
