package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.ProductEntity;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Creates a new pedido document with a nested collection of products
 */
public class PedidoCreator implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private final String currentDate, mesa;
    private final List<ProductEntity> canasta;
    private String mesaId;

    public PedidoCreator(String currentDate, String mesa, List<ProductEntity> canasta, String mesaId) {
        this.currentDate = currentDate;
        this.mesa = mesa;
        this.mesaId = mesaId;
        this.canasta = canasta;
    }

    @Override
    public void run() {

        String metaDocId = String.valueOf(new Random().nextLong());

        CollectionReference pedido = fStore.collection("pedidos")
                .document(currentDate)
                .collection("pedidos enviados")
                .document(metaDocId)
                .collection("pedido");

        DocumentReference metaDoc = fStore.collection("pedidos")
                .document(currentDate)
                .collection("pedidos enviados")
                .document(metaDocId);

        boolean servidoBarra = true;
        boolean servidoCocina = true;

        // take a list of products from canasta
        // iterating, add each product to the pedido collection
        // updating servido fields, if necessary
        for (ProductEntity product : canasta) {
            pedido.add(product);
            if (product.getCategory().equals(Utils.BARRA)) {
                servidoBarra = false;
            } else {
                servidoCocina = false;
            }
        }

        // create a meta document and send the packet on success
        Map<String, Object> doc = new HashMap<>();
        doc.put(Utils.KEY_IS_EXPANDED, false);
        doc.put(Utils.KEY_MESA, mesa);
        doc.put(Utils.SERVIDO, false);
        doc.put(Utils.SERVIDO_BARRA, servidoBarra);
        doc.put(Utils.SERVIDO_COCINA, servidoCocina);
        doc.put(Utils.KEY_USER, "Cliente");
        doc.put(Utils.KEY_USER_ID, mesa);
        doc.put(Utils.MESA_ID, mesaId);
        doc.put(Utils.TIMESTAMP, new Timestamp(new Date()));

        metaDoc.set(doc);

        fStore.collection("administradores")
                .get()
                .addOnSuccessListener(App.executor, admins -> {

                    for (QueryDocumentSnapshot admin : admins) {
                        String adminToken = admin.getString(Utils.KEY_TOKEN);
                        fMessaging.send(new RemoteMessage.Builder(App.SENDER_ID + "@fcm.googleapis.com")
                                .setMessageId(Utils.getMessageId())
                                .addData(Utils.KEY_MESA, mesa)
                                .addData(Utils.KEY_TOKEN, "cliente")
                                .addData(Utils.KEY_NOMBRE, "Cliente")
                                .addData(Utils.KEY_FECHA, currentDate)
                                .addData(Utils.ADMIN_TOKEN, adminToken)
                                .addData(Utils.KEY_META_ID, metaDocId)
                                .addData(Utils.KEY_ACTION, Utils.ACTION_PEDIDO)
                                .addData(Utils.KEY_TYPE, Utils.TO_ADMIN_NEW)
                                .build());
                    }
                });


    }
}
