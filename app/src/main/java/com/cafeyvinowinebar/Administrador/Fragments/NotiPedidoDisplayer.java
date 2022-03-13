package com.cafeyvinowinebar.Administrador.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterNotiPedido;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.ItemShort;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class NotiPedidoDisplayer extends DialogFragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private RecyclerView recPedido;
    private AdapterNotiPedido adapter;
    private String token, nombre, mesa, metaDocId, fecha;
    private int notiId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = requireActivity().getIntent();
        token = intent.getStringExtra(Utils.KEY_TOKEN);
        metaDocId = intent.getStringExtra(Utils.KEY_META_ID);
        fecha = intent.getStringExtra(Utils.KEY_FECHA);
        nombre = intent.getStringExtra(Utils.KEY_NOMBRE);
        mesa = intent.getStringExtra(Utils.KEY_MESA);
        notiId = intent.getIntExtra(Utils.KEY_NOTI_ID, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_noti_pedido, container, false);

        TextView txtNoti = view.findViewById(R.id.txtNotiPedido);
        Button btnConfirmar = view.findViewById(R.id.btnNotiPedido);
        recPedido = view.findViewById(R.id.recNotiPedido);

        // tell the admin who sent the order
        txtNoti.setText(requireActivity().getString(R.string.pedido_item_title, nombre, mesa));

        NotificationManagerCompat manager = NotificationManagerCompat.from(requireContext());

        setupAdapter();



        if (!token.equals("cliente")) {

            // if the order was sent by a user of the app we can send a confirmation message to the user
            btnConfirmar.setOnClickListener(v -> {

                fMessaging.send(new RemoteMessage.Builder(getString(R.string.fcm_id, App.SENDER_ID))
                        .setMessageId(Utils.getMessageId())
                        .addData(Utils.KEY_TOKEN, token)
                        .addData(Utils.KEY_ACTION, Utils.ACTION_PEDIDO_ADMIN)
                        .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                        .build());

                // remove the noti from the tray, destroy the fragment and the activity
                manager.cancel(notiId);
                requireActivity().finish();

            });
        } else {

            // if the order was created by admin, we don't show the confirmar button
            // since there's no one to send a confirmation message to
            btnConfirmar.setVisibility(View.GONE);
        }

        Button btnOmitir = view.findViewById(R.id.btnOmitir);
        btnOmitir.setOnClickListener(v -> {
            // remove the noti from the tray, destroy the fragment and the activity
            manager.cancel(notiId);
            requireActivity().finish();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void setupAdapter() {
        Query query = fStore.collection("pedidos")
                .document(fecha)
                .collection("pedidos enviados")
                .document(metaDocId)
                .collection("pedido")
                .orderBy(Utils.KEY_NAME);
        FirestoreRecyclerOptions<ItemShort> options = new FirestoreRecyclerOptions.Builder<ItemShort>()
                .setQuery(query, ItemShort.class)
                .build();
        adapter = new AdapterNotiPedido(options);
        recPedido.setAdapter(adapter);
        recPedido.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
