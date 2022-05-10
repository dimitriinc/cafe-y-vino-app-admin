package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.Redaction;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * creates a redaction object out of pedido or cuenta before deleting it
 * stores the redaction into the cambios collection in the Firestore
 * deletes the products inside the pedido or cuenta collection
 */
public class EliminationApplier implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final String userName, mesa, comment, collectionPath;
    private final CountDownLatch latch;

    public EliminationApplier(String userName, String mesa, String comment, String collectionPath, CountDownLatch latch) {

        this.userName = userName;
        this.mesa = mesa;
        this.comment = comment;
        this.collectionPath = collectionPath;
        this.latch = latch;
    }

    @Override
    public void run() {

        // we need a map to store the content of the collection being deleted
        Map<String, String> content = new HashMap<>();

        // get the collection from the Firestore, iterate through it to populate the content map
        fStore.collection(collectionPath).get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        content.put(snapshot.getString(Utils.KEY_NAME), String.valueOf(snapshot.getLong(Utils.KEY_COUNT)));

                        // delete the produce from db
                        snapshot.getReference().delete().addOnSuccessListener(App.executor, unused -> {
                            if (snapshot.equals(queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1))) {
                                latch.countDown();
                            }
                        });
                    }

                    // construct the time string
                    Calendar calendar = GregorianCalendar.getInstance();
                    calendar.setTime(new Date());
                    String amPm;
                    if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                        amPm = "AM";
                    } else {
                        amPm = "PM";
                    }
                    String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + " " + amPm;

                    // store a redaction object into the Firestore
                    fStore.collection("cambios").document(Utils.getCurrentDate())
                            .collection("cambios")
                            .add(new Redaction(content, comment, userName, mesa, new Timestamp(new Date()), time, Utils.ELIMINACION));
                });

    }
}
