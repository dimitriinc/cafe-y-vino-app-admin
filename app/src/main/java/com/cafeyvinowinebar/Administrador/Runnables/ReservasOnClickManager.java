package com.cafeyvinowinebar.Administrador.Runnables;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterReservas;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.Reserva;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;


public class ReservasOnClickManager implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private final DocumentSnapshot snapshot;
    private final String date, part;
    private final Context context;
    private final int position;
    private final AdapterReservas adapter;
    private final Activity activity;
    private final Handler mainHandler;

    public ReservasOnClickManager(DocumentSnapshot snapshot, String date, String part, Context context, int position, AdapterReservas adapter,
                                  Activity activity, Handler mainHandler) {
        this.snapshot = snapshot;
        this.date = date;
        this.part = part;
        this.context = context;
        this.position = position;
        this.adapter = adapter;
        this.activity = activity;
        this.mainHandler = mainHandler;
    }

    @Override
    public void run() {

        boolean isDarkThemeOn = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        // first we store the table number
        String mesa = snapshot.getId();

        // get the reference to the doc in the reservas collection with the id of the table
        // on the said day in the said part
        DocumentReference reference = fStore.collection(Utils.RESERVAS)
                .document(date)
                .collection(part)
                .document(mesa);

        // get the reference
        reference.get().addOnSuccessListener(App.executor, reservaSnapshot -> {

            // if the snapshot exists, there is a reservation on this table
            if (reservaSnapshot.exists()) {

                // before checking if the reservation is confirmed or not, we store the data about the reservation, that we'll need later
                String nombre = reservaSnapshot.getString(Utils.KEY_NOMBRE);
                String hora = reservaSnapshot.getString(Utils.KEY_HORA);
                String pax = reservaSnapshot.getString(Utils.KEY_PAX);
                String telefono = reservaSnapshot.getString(Utils.TELEFONO);
                String comentario = reservaSnapshot.getString(Utils.KEY_COMENTARIO);
                String userId = reservaSnapshot.getString(Utils.KEY_USER_ID);

                assert userId != null;
                if (!reservaSnapshot.getBoolean(Utils.KEY_CONFIRMADO)) {

                    // the reservation isn't confirmed yet (request)
                    // we get the token of the user who made the reservation to send him a message later
                    fStore.collection(Utils.USUARIOS)
                            .document(userId)
                            .get()
                            .addOnSuccessListener(App.executor, userSnap -> {
                                String token = userSnap.getString(Utils.KEY_TOKEN);

                                // we build an alert dialog to show admin the details of the request
                                // in the dialog admin can accept or reject the request with the buttons
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                View view = activity.getLayoutInflater().inflate(R.layout.dialog_reserva_displayer, null);
                                builder.setView(view);
                                builder.setTitle("Solicitud");
                                TextView txtNombre = view.findViewById(R.id.txtNombre);
                                TextView txtHora = view.findViewById(R.id.txtHora);
                                TextView txtPax = view.findViewById(R.id.txtPax);
                                TextView txtTelefono = view.findViewById(R.id.txtTelefono);
                                TextView txtComentario = view.findViewById(R.id.txtComentario);
                                txtNombre.setText(nombre);
                                txtHora.setText(hora);
                                txtPax.setText(pax);
                                txtTelefono.setText(telefono);
                                txtComentario.setText(comentario);

                                builder.setPositiveButton(context.getString(R.string.confirmar), (dialog, which) -> {

                                    // on accepting the request we mark the document as confirmed
                                    reference.update(Utils.KEY_CONFIRMADO, true)
                                            .addOnSuccessListener(App.executor, unused -> {

                                                mainHandler.post(() -> adapter.notifyItemChanged(position));

                                                // and send a message to the customer
                                                fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                                                        .setMessageId(Utils.getMessageId())
                                                        .addData(Utils.KEY_TOKEN, token)
                                                        .addData(Utils.KEY_FECHA, date)
                                                        .addData(Utils.KEY_HORA, hora)
                                                        .addData(Utils.KEY_PAX, pax)
                                                        .addData(Utils.KEY_ACTION, Utils.ACTION_RESERVA_ACK)
                                                        .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                                                        .build());
                                            });
                                });

                                builder.setNegativeButton(context.getString(R.string.rechazar), (dialog, which) -> {

                                    // on rejecting the request we build a nested alert dialog
                                    // for admin to write an explication of why the request is rejected
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                    View view1 = activity.getLayoutInflater().inflate(R.layout.dialog_reserva_rejection, null);
                                    builder1.setView(view1);
                                    EditText et = view1.findViewById(R.id.rejection);
                                    builder1.setCancelable(true);
                                    builder1.setPositiveButton("OK", (dialog1, which1) -> {

                                        // store the explication, which is not obligatory
                                        // if we send a packet with an empty string as comment, the client's app will know how to react
                                        String comment = et.getText().toString().trim();

                                        // we delete the document from the reservas collection
                                        reference.delete().addOnSuccessListener(App.executor, unused -> {

                                            mainHandler.post(() -> adapter.notifyItemChanged(position));

                                            // and send a message to the customer who made the request
                                            fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                                                    .setMessageId(Utils.getMessageId())
                                                    .addData(Utils.KEY_TOKEN, token)
                                                    .addData(Utils.KEY_FECHA, date)
                                                    .addData(Utils.KEY_HORA, hora)
                                                    .addData(Utils.KEY_COMENTARIO, comment)
                                                    .addData(Utils.KEY_ACTION, Utils.ACTION_RESERVA_NACK)
                                                    .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                                                    .build());
                                        });
                                    });
                                    mainHandler.post(() -> builder1.create().show());
                                });



                                mainHandler.post(() -> {

                                    AlertDialog dialogRequest = builder.create();
                                    dialogRequest.show();

                                    if (isDarkThemeOn) {
                                        Button btnPositive = dialogRequest.getButton(DialogInterface.BUTTON_POSITIVE);
                                        btnPositive.setTextColor(context.getColor(R.color.white));
                                        Button btnNegative = dialogRequest.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        btnNegative.setTextColor(context.getColor(R.color.white));
                                    }
                                });
                            });
                } else {

                    // the reservation is confirmed, we build an alert dialog to show admin its details
                    // in the dialog admin can mark the reservation as 'arrived' or 'left'
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View view = activity.getLayoutInflater().inflate(R.layout.dialog_reserva_displayer, null);
                    builder.setView(view);
                    builder.setTitle("Reserva confirmada");

                    builder.setPositiveButton(context.getString(R.string.llegado), (dialog, which) -> {

                        // admin can create a custom reservation if request arrives not via the client's app
                        // or if they created an empty reservation to block reservations for the table
                        // in this case the value of 'userId' will be 'none'
                        if (userId.equals("none")) {
                            mainHandler.post(() -> Toast.makeText(context, context.getString(R.string.entrada_a_medida), Toast.LENGTH_SHORT).show());
                        } else {

                            // marking the reservation 'arrived', we change the client's status to 'present' and assign him the table
                            // on which the reservation was made
                            DocumentReference userReference = fStore.collection("usarios").document(userId);
                            userReference.update(Utils.IS_PRESENT, true);
                            userReference.update(Utils.KEY_MESA, mesa);
                            reference.update(Utils.KEY_LLEGADO, true);
                            mainHandler.post(() -> adapter.notifyItemChanged(position));

                            // we get the client's token and his name
                            userReference.get().addOnSuccessListener(App.executor, userSnapshot ->

                                    // and send him a welcoming message
                                    fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                                            .setMessageId(Utils.getMessageId())
                                            .addData(Utils.KEY_TOKEN, userSnapshot.getString(Utils.KEY_TOKEN))
                                            .addData(Utils.KEY_NOMBRE, userSnapshot.getString(Utils.KEY_NOMBRE))
                                            .addData(Utils.KEY_ACTION, Utils.ACTION_PUERTA_ADMIN)
                                            .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                                            .build()));
                        }
                    });

                    builder.setNegativeButton(context.getString(R.string.salido), (dialog, which) -> {
                        if (userId.equals("none")) {
                            mainHandler.post(() -> Toast.makeText(context, context.getString(R.string.entrada_a_medida), Toast.LENGTH_SHORT).show());
                        } else {

                            // this button basically undoes the positive button in case it was pressed by mistake
                            DocumentReference userReference = fStore.collection("usarios").document(userId);
                            userReference.update(Utils.IS_PRESENT, false);
                            userReference.update(Utils.KEY_MESA, "00");
                            reference.update(Utils.KEY_LLEGADO, false);
                            mainHandler.post(() -> adapter.notifyItemChanged(position));
                        }
                    });

                    TextView txtNombre = view.findViewById(R.id.txtNombre);
                    TextView txtHora = view.findViewById(R.id.txtHora);
                    TextView txtPax = view.findViewById(R.id.txtPax);
                    TextView txtTelefono = view.findViewById(R.id.txtTelefono);
                    TextView txtComentario = view.findViewById(R.id.txtComentario);
                    txtNombre.setText(nombre);
                    txtHora.setText(hora);
                    txtPax.setText(pax);
                    txtTelefono.setText(telefono);
                    txtComentario.setText(comentario);

                    mainHandler.post(() -> {

                        AlertDialog dialogConfirm = builder.create();
                        dialogConfirm.show();

                        if (isDarkThemeOn) {
                            Button btnPositive = dialogConfirm.getButton(DialogInterface.BUTTON_POSITIVE);
                            btnPositive.setTextColor(context.getColor(R.color.white));
                            Button btnNegative = dialogConfirm.getButton(DialogInterface.BUTTON_NEGATIVE);
                            btnNegative.setTextColor(context.getColor(R.color.white));
                        }
                    });
                }
            } else {

                // the table has no reservations on it, we build an alert dialog where admin can create a custom reservation
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = activity.getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_new_reserva, null);
                builder.setView(view);
                EditText etNombre = view.findViewById(R.id.etNombre);
                EditText etHora = view.findViewById(R.id.etHora);
                EditText etPax = view.findViewById(R.id.etPax);
                EditText etTelefono = view.findViewById(R.id.etTelefono);
                EditText etComentario = view.findViewById(R.id.etComentario);
                builder.setTitle("Agregar una reserva");
                builder.setPositiveButton("OK", (dialog, which) -> {

                    // a reservation is created with custom data
                    // note that 'userId' is 'none'; and that a custom one is confirmed on creation
                    String nombre = etNombre.getText().toString();
                    String hora = etHora.getText().toString();
                    String pax = etPax.getText().toString();
                    String telefono = etTelefono.getText().toString();
                    String comentario = etComentario.getText().toString();
                    reference.set(new Reserva(nombre, telefono, pax, hora, comentario, false, "none", true))
                            .addOnSuccessListener(unused -> adapter.notifyItemChanged(position));
                });

                mainHandler.post(() -> {

                    AlertDialog dialogEmpty = builder.create();
                    dialogEmpty.show();

                    if (isDarkThemeOn) {
                        Button btnPositive = dialogEmpty.getButton(DialogInterface.BUTTON_POSITIVE);
                        btnPositive.setTextColor(context.getColor(R.color.white));
                        Button btnNegative = dialogEmpty.getButton(DialogInterface.BUTTON_NEGATIVE);
                        btnNegative.setTextColor(context.getColor(R.color.white));
                    }
                });
            }
        });
    }
}
