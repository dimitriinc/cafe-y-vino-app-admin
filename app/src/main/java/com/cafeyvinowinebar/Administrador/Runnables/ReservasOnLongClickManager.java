package com.cafeyvinowinebar.Administrador.Runnables;

import android.view.View;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterReservas;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * On long click we just delete the document stored with the table number as its id
 * From the Reservas collection on the said day in the said part
 */
public class ReservasOnLongClickManager implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final DocumentSnapshot snapshot;
    private final String date;
    private final String part;
    private final View view;
    private final AdapterReservas adapter;
    private final int position;


    public ReservasOnLongClickManager(DocumentSnapshot snapshot, String date, String part, View view,
                                      AdapterReservas adapter, int position) {
        this.snapshot = snapshot;
        this.date = date;
        this.part = part;
        this.view = view;
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public void run() {

        // the snapshot has only one value - its id, which is the number of the table clicked
        String mesa = snapshot.getId();

        // the reservations are stored with the table number on which it was requested
        DocumentReference reference = fStore.collection(Utils.RESERVAS)
                .document(date)
                .collection(part)
                .document(mesa);

        reference.get().addOnSuccessListener(App.executor, documentSnapshot -> {

            // here we need to check if the doc really exists, because admin can long click a table with no reservation
            // also we need a snapshot object to pass to the showSnackbar()
            if (documentSnapshot.exists()) {
                reference.delete()
                        .addOnSuccessListener(unused -> {

                        // once the doc is deleted, we notify the adapter and show a snackbar, in case admin wants to undo the deletion
                        adapter.notifyItemChanged(position);
                        showSnackbar(documentSnapshot, reference, position);

                });
            }
        });
    }

    public void showSnackbar(DocumentSnapshot snapshot, DocumentReference reference, int position) {
        Snackbar snackbar = Snackbar.make(view, "La reserva está eliminada", Snackbar.LENGTH_LONG)
                .setAction("DESHACER", v -> reference.set(Objects.requireNonNull(snapshot.getData()))
                        .addOnSuccessListener(unused -> adapter.notifyItemChanged(position)));
        snackbar.show();
    }
}
