package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Takes all the items in the pending bill collection and moves them to a cuenta cancelada collection
 * Deletes the pending bill
 * Updates the user data (if the bill belongs to a user of the app): presence status, bonus points, and the 'mesa' field
 * Sends a goodbye message
 * Updates two consumption collections: total and personal
 * If the bill was assigned to custom table for a custom user, deletes the table from the SQLiteDB
 */
public class CuentaCancelador implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private double total, montoEfectivo, montoVisa, montoYape, montoCripto;
    private final DocumentSnapshot snapshot;
    private final String currentDate;
    private String userId;
    private String userMesa;
    private String userName;
    private final String payType;
    private Long propinaVisa, propinaYape, propinaCripto;

    public CuentaCancelador(DocumentSnapshot snapshot, String currentDate, String payType,
                            Long propinaVisa, Long propinaYape, Long propinaCripto) {
        this.snapshot = snapshot;
        this.currentDate = currentDate;
        this.payType = payType;
        this.propinaVisa = propinaVisa;
        this.propinaYape = propinaYape;
        this.propinaCripto = propinaCripto;
    }

    public CuentaCancelador(double montoEfectivo, double montoVisa, double montoYape,
                            double montoCripto, DocumentSnapshot snapshot, String currentDate, String payType,
                            Long propinaVisa, Long propinaYape, Long propinaCripto) {
        this.montoEfectivo = montoEfectivo;
        this.montoVisa = montoVisa;
        this.montoYape = montoYape;
        this.montoCripto = montoCripto;
        this.snapshot = snapshot;
        this.currentDate = currentDate;
        this.payType = payType;
        this.propinaVisa = propinaVisa;
        this.propinaYape = propinaYape;
        this.propinaCripto = propinaCripto;
    }

    @Override
    public void run() {

        // we get a snapshot of a cuenta meta doc with the constructor
        // store the meta data about the bill
        userId = snapshot.getId();
        userMesa = snapshot.getString(Utils.KEY_MESA);
        userName = snapshot.getString(Utils.KEY_NAME);
        total = 0;

        // to calculate the total sum of the bill we get the cuenta collection and iterate through its documents
        fStore.collection(Utils.CUENTAS)
                .document(currentDate)
                .collection("cuentas corrientes")
                .document(userId)
                .collection(Utils.CUENTA)
                .get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                    synchronized (this) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            long itemTotal = doc.getLong(Utils.TOTAL);
                            total += itemTotal;
                        }
                    }

                    // next, we check if the cuenta belongs to a customer or not
                    // if it does, we need to update their personal doc, move data, and send a message
                    // if the cuenta belongs to a custom user, we just move data and delete the cuenta meta doc
                    if (userName.equals("Cliente")) {

                        moveData(userId, userName, total);
                        snapshot.getReference().delete();

                    } else {

                        // calculate how much bonus points the user will receive
                        long bono = (long) total / 10;

                        // then we deal with the customer's personal data
                        // we need to increment his bonus points field
                        // change his status to 'not present'
                        // and update his mesa value to '00'
                        DocumentReference reference = fStore.collection(Utils.USUARIOS)
                                .document(userId);
                        reference.get()
                                .addOnSuccessListener(App.executor, userDocumentSnapshot -> {

                                    reference.update(Utils.KEY_BONOS, FieldValue.increment(bono));
                                    reference.update(Utils.IS_PRESENT, false);
                                    reference.update(Utils.KEY_MESA, "00");

                                    // actually moves all the products from the pending cuenta collection
                                    // to the canceled cuenta collection
                                    moveData(userId, userName, total);

                                    // after we moved the data, we delete the meta doc of the pending bill
                                    snapshot.getReference().delete();

                                    // we also should update the 'blocked' status of the table if it's one of the fixed ones
                                    // if the table assigned to the client was not one of the fixed, the mesaId will be null
                                    // and we shouldn't worry about this step
                                    fStore.collection("mesas").whereEqualTo(Utils.KEY_NAME, userMesa).get()
                                            .addOnSuccessListener(App.executor, queryDocumentSnapshots1 -> {
                                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots1) {
                                                    doc.getReference().update("blocked", false);
                                                }
                                            });

                                    // get some personal data about the user
                                    String token = userDocumentSnapshot.getString(Utils.KEY_TOKEN);
                                    String nombre = userDocumentSnapshot.getString(Utils.KEY_NOMBRE);

                                    // send a goodbye message to the client
                                    fMessaging.send(new RemoteMessage.Builder(App.SENDER_ID + "@fcm.googleapis.com")
                                            .setMessageId(Utils.getMessageId())
                                            .addData(Utils.KEY_TOKEN, token)
                                            .addData(Utils.KEY_NOMBRE, nombre)
                                            .addData(Utils.KEY_BONO, String.valueOf(bono))
                                            .addData(Utils.KEY_ACTION, Utils.ACTION_CUENTA_ADMIN)
                                            .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                                            .build());
                                });
                    }
                });
    }

    public void moveData(String userId, String userName, double total) {

        // store a string of a random number to use it as a meta doc id
        String metaDocId = String.valueOf(new Random().nextLong());

        CollectionReference fromPath = fStore.collection(Utils.CUENTAS)
                .document(currentDate)
                .collection("cuentas corrientes")
                .document(userId)
                .collection(Utils.CUENTA);
        CollectionReference toPath = fStore.collection(Utils.CUENTAS)
                .document(currentDate)
                .collection("cuentas_canceladas")
                .document(metaDocId)
                .collection(Utils.CUENTA);
        DocumentReference toDoc = fStore.collection(Utils.CUENTAS)
                .document(currentDate)
                .collection("cuentas_canceladas")
                .document(metaDocId);

        CountDownLatch latch = new CountDownLatch(1);

        fromPath.get().addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

            // get the pending cuenta collection and iterate through its items
            for (QueryDocumentSnapshot cuentaProductSnapshot : queryDocumentSnapshots) {


                // get the item's name and count
                String itemName = cuentaProductSnapshot.getId();
                Long count = cuentaProductSnapshot.getLong(Utils.KEY_COUNT);
                assert count != null;

                // first we want to add the item to a total consumo collection
                // which is a statistic of all the products consumed
                // we get a reference of a document with the item's name in this collection
                DocumentReference consumoTotalDoc = fStore
                        .collection("consumo_total")
                        .document(itemName);
                // then check if such product is already in the collection
                consumoTotalDoc.get().addOnSuccessListener(App.executor, snapshot -> {
                    if (snapshot.exists()) {
                        // if it's already there, we increment its count
                        consumoTotalDoc.update(Utils.KEY_COUNT, FieldValue.increment(count));
                    } else {
                        // if not we create a new document
                        Map<String, Object> item = new HashMap<>();
                        item.put(Utils.KEY_COUNT, count);
                        item.put(Utils.KEY_NAME, itemName);
                        consumoTotalDoc.set(item);
                    }
                });

                // next operation is only for the user of the client app, it adds the product to the personal consumo collection
                // reminder: IDs of the bills of custom users correspond to the table number
                if (!userName.equals("Cliente")) {
                    // means the cuenta belongs to a user of the app
                    // in this case we store the product in the personal consumo collection of the user
                    // as we did with the total consumo collection, get a reference of a document with the item's name in the collection
                    DocumentReference consumoDoc = fStore.collection(Utils.USUARIOS)
                            .document(userId)
                            .collection("consumo")
                            .document(itemName);
                    // check if it exists
                    consumoDoc.get().addOnSuccessListener(App.executor, snapshot -> {
                        if (snapshot.exists()) {
                            // if it does, update its count
                            consumoDoc.update(Utils.KEY_COUNT, FieldValue.increment(count));
                        } else {
                            // if it doesn't create a new doc
                            Map<String, Object> item = new HashMap<>();
                            item.put(Utils.KEY_COUNT, count);
                            item.put(Utils.KEY_NAME, itemName);
                            consumoDoc.set(item);
                        }
                    });

                }

                // copy the item to the cuenta cancelada collection, and then delete it
                toPath.add(cuentaProductSnapshot.getData());


                cuentaProductSnapshot.getReference().delete().addOnSuccessListener(App.executor, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        // if the product is the last one in the collection, release the latch
                        if (cuentaProductSnapshot.equals(queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1))) {
                            latch.countDown();
                        }
                    }
                });
            }
        });

        // to prevent meta doc being deleted before all the products are deleted we will latch the thread
        // untill the iteration is over and all the items are deleted for good
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // at this point the iteration is over, so we can handle the meta doc of the canceled bill
        // create an object
        Map<String, Object> data = new HashMap<>();
        data.put(Utils.KEY_NAME, userName);
        data.put(Utils.KEY_IS_EXPANDED, false);
        data.put(Utils.KEY_HORA, Utils.getCurrentHour());
        data.put(Utils.TOTAL, String.valueOf(total));
        data.put(Utils.KEY_MESA, userMesa);
        data.put(Utils.KEY_USER_ID, userId);
        data.put(Utils.KEY_FECHA, currentDate);
        data.put(Utils.KEY_META_ID, metaDocId);
        data.put(Utils.TIMESTAMP, new Timestamp(new Date()));
        data.put(Utils.KEY_PAY_TYPE, payType);

        if (propinaVisa != null) {
            data.put(Utils.PROPINA_VISA, propinaVisa);
        }
        if (propinaYape != null) {
            data.put(Utils.PROPINA_YAPE, propinaYape);
        }
        if (propinaCripto != null) {
            data.put(Utils.PROPINA_CRIPTO, propinaCripto);
        }

        // if the bill was divided between different payment types
        // we also add the data of how exactly it was devided, and what were the payment types
        // we'll use this data when calculating a report for total earnings for the day, and for the spending stats of the user
        if (payType.equals(Utils.DIVIDIDO)) {
            if (montoEfectivo != 0) {
                data.put(Utils.EFECTIVO, montoEfectivo);
            }
            if (montoVisa != 0) {
                data.put(Utils.VISA, montoVisa);
            }
            if (montoYape != 0) {
                data.put(Utils.YAPE, montoYape);
            }
            if (montoCripto != 0) {
                data.put(Utils.CRIPTO, montoCripto);
            }
        }

        // set the prepared object as a meta doc of the canceled bill
        toDoc.set(data);

        // finally, we handle custom tables
        // if the table in question is a fixed one, we update its presence status to false
        // if it's not a fixed one, we delete it
        if (userName.equals("Cliente")) {

            fStore.collection("mesas").whereEqualTo(Utils.KEY_NAME, userMesa).get()
                    .addOnSuccessListener(App.executor, queryDocumentSnapshots1 -> {

                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots1) {
                            if (snapshot.getBoolean("fixed")) {
                                snapshot.getReference().update("present", false);
                            } else {
                                snapshot.getReference().delete();
                            }
                        }
                    });
        }

    }
}
