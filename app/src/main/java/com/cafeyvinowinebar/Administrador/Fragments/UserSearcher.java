package com.cafeyvinowinebar.Administrador.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterUsuariosIdEmail;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.HistoryActivity;
import com.cafeyvinowinebar.Administrador.POJOs.Usuario;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Displays a list of all the customers with the name passed as argument in the constructor
 * The parent view of the dialog fragment is the recyclerView itself
 */
public class UserSearcher extends DialogFragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private String nombre;
    private AdapterUsuariosIdEmail adapter;

    // we pass the name being searched as an argument in the arguments bundle
    public static UserSearcher newInstance(String nombre) {
        Bundle args = new Bundle();
        args.putString(Utils.KEY_NOMBRE, nombre);
        UserSearcher fragment = new UserSearcher();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        nombre = getArguments().getString(Utils.KEY_NOMBRE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        assert context != null;

        RecyclerView view = new RecyclerView(context);
        Query query = fStore.collection(Utils.USUARIOS)
                .whereEqualTo(Utils.KEY_NOMBRE, nombre);
        FirestoreRecyclerOptions<Usuario> options = new FirestoreRecyclerOptions.Builder<Usuario>()
                .setQuery(query, Usuario.class)
                .build();
        adapter = new AdapterUsuariosIdEmail(options, context);
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(context));
        adapter.setOnItemClickListener((snapshot, position, view1) -> {

            // we build an alert dialog with two buttons: one directs the admin to the HistoryActivity
            // the other sends a message to the customer
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View viewHistoryMensaje = getLayoutInflater().inflate(R.layout.dialog_history_mensaje, null);

            Button btnHistory = viewHistoryMensaje.findViewById(R.id.btnHistory);
            Button btnMensaje = viewHistoryMensaje.findViewById(R.id.btnMensaje);

            builder.setView(viewHistoryMensaje);
            AlertDialog alertDialog = builder.create();

            btnHistory.setOnClickListener(v -> {

                // before going to the HistoryActivity, destroy the dialog fragment
                dismiss();
                // and destroy the alert dialog as well
                alertDialog.dismiss();
                // go to the HistoryActivity with the clicked user's id in the intent
                startActivity(HistoryActivity.newIntent(context, snapshot.getId()));

            });
            btnMensaje.setOnClickListener(v -> {

                // display a nested alert dialog with a field to enter the message
                AlertDialog.Builder builderMensaje = new AlertDialog.Builder(context);
                View viewMensaje = getLayoutInflater().inflate(R.layout.dialog_reserva_rejection, null);
                EditText edtMensaje = viewMensaje.findViewById(R.id.rejection);
                edtMensaje.setHint(getString(R.string.mensaje));
                builderMensaje.setView(viewMensaje);

                builderMensaje.setPositiveButton(getString(R.string.enviar_mensaje), (dialog, which) -> {

                    // retrieve the message from the edit text
                    String msg = edtMensaje.getText().toString().trim();
                    if (msg.isEmpty()) {
                        Toast.makeText(context, getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                    } else {

                        // send it to the customer
                        fMessaging.send(new RemoteMessage.Builder(getString(R.string.fcm_id, App.SENDER_ID))
                                .setMessageId(Utils.getMessageId())
                                .addData(Utils.KEY_TOKEN, snapshot.getString(Utils.KEY_TOKEN))
                                .addData(Utils.KEY_ACTION, Utils.ACTION_MSG)
                                .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                                .addData(Utils.ACTION_MSG, msg)
                                .build());
                        dialog.dismiss();
                        dismiss();
                    }
                });
                builderMensaje.create().show();
            });
            alertDialog.show();

        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
