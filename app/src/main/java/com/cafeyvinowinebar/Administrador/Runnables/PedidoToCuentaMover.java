package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.CountDownLatch;

/**
 * Iterates through the pedido collection
 * And moves each product to the cuenta collection
 */
public class PedidoToCuentaMover implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final DocumentSnapshot documentSnapshot;
    public String currentDate;
    private final String mode;


    public PedidoToCuentaMover(DocumentSnapshot documentSnapshot, String currentDate, String mode) {
        this.documentSnapshot = documentSnapshot;
        this.currentDate = currentDate;
        this.mode = mode;

    }

    @Override
    public void run() {

        Query query;

        // retrieve some user data from the meta doc
        // with which we'll create a cuenta meta doc
        String userId = documentSnapshot.getString(Utils.KEY_USER_ID);
        String userName = documentSnapshot.getString(Utils.KEY_USER);
        String userMesa = documentSnapshot.getString(Utils.KEY_MESA);
        String mesaId = documentSnapshot.getString(Utils.MESA_ID);

        // get a reference to the pedido's collection
        CollectionReference pedido = fStore.collection(documentSnapshot.getReference().getPath() + "/pedido");

        // initialize the query based on the 'mode' value
        switch (mode) {
            case Utils.TODO:
                query = pedido;
                break;
            case Utils.BARRA:
                query = pedido.whereEqualTo(Utils.KEY_CATEGORY, Utils.BARRA);
                break;
            case Utils.COCINA:
                query = pedido.whereEqualTo(Utils.KEY_CATEGORY, Utils.COCINA);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + mode);
        }

        query.get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                    // iterate through the pedido collection
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                        // store the name, price and count of the pedido item
                        String name = snapshot.getString(Utils.KEY_NAME);
                        long price = snapshot.getLong(Utils.PRICE);
                        long cantidad = snapshot.getLong(Utils.KEY_COUNT);

                        // get a reference for the item of the stored name in the cuenta collection
                        assert name != null && userId != null;
                        DocumentReference reference = fStore.collection("cuentas")
                                .document(currentDate)
                                .collection("cuentas corrientes")
                                .document(userId)
                                .collection("cuenta")
                                .document(name);

                        // we will perform operations on the reference in a separate thread
                        // and latch the current thread while doing it to halt the iteration
                        // if we don't latch, the reference on the next iteration might be outdated
                        CountDownLatch latch = new CountDownLatch(1);

                        // if a product with the same name already exists, we'll increment its count
                        // if it doesn't, we will create a new document for the product; and populate the meta doc with the user data
                        App.executor.submit(new CuentaCreator(reference, latch, name, userName, userMesa, userId, currentDate,
                                price, cantidad, mesaId));

                        // latch the thread after starting the background task
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // the pedido item is transformed to a cuenta item, we can delete its document from the pedido collection
                        snapshot.getReference().delete();

                    }

                    // after the iteration is done, we can delete the pedido meta doc if all of its products are served
                    // if only one of the categories is served, we just mark the category as served
                    DocumentReference metaDocReference = documentSnapshot.getReference();
                    switch (mode) {
                        case Utils.TODO:
                            metaDocReference.delete();
                            break;
                        case Utils.BARRA:
                            metaDocReference.get().addOnSuccessListener(App.executor, snapshot -> {
                                if (snapshot.getBoolean(Utils.SERVIDO_COCINA)) {
                                    metaDocReference.delete();
                                } else {
                                    metaDocReference.update(Utils.SERVIDO_BARRA, true);
                                }
                            });
                            break;
                        case Utils.COCINA:
                            metaDocReference.get().addOnSuccessListener(App.executor, snapshot -> {
                                if (snapshot.getBoolean(Utils.SERVIDO_BARRA)) {
                                    metaDocReference.delete();
                                } else {
                                    metaDocReference.update(Utils.SERVIDO_COCINA, true);
                                }
                            });
                            break;

                    }

                });


    }
}
