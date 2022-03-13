package com.cafeyvinowinebar.Administrador.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterReservas;
import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.POJOs.Reserva;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.ReservasOnClickManager;
import com.cafeyvinowinebar.Administrador.Runnables.ReservasOnLongClickManager;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays night reservations for the day
 * The on click functionality depends on the state of the reservation
 * So we assign two runnables to manage it: one for on click, another for on long click
 */
public class ReservasNight extends Fragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public AdapterReservas adapter;
    public String date, part;
    private RecyclerView recViewNight;
    public Context context;
    public Activity activity;
    public Handler mainHandler;
    public View view;

    public static ReservasNight newInstance(String date) {
        Bundle args = new Bundle();
        args.putString(Utils.KEY_DATE, date);
        ReservasNight fragment = new ReservasNight();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        date = getArguments().getString(Utils.KEY_DATE);
        part = Utils.NOCHE;
        context = getContext();
        activity = getActivity();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reservas_night, container, false);

        if (container != null) {
            container.removeAllViews();
        }

        recViewNight = view.findViewById(R.id.recViewNight);

        setupAdapter();

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

    public void setupAdapter() {

        Query query = fStore.collection(Utils.RESERVAS_MODEL);
        FirestoreRecyclerOptions<Reserva> options = new FirestoreRecyclerOptions.Builder<Reserva>()
                .setQuery(query, Reserva.class)
                .build();

        adapter = new AdapterReservas(options, date, part, context, mainHandler);
        recViewNight.setAdapter(adapter);
        recViewNight.setLayoutManager(new GridLayoutManager(context, 3));

        adapter.setOnItemClickListener((snapshot, position, view) -> App.executor.submit(
                new ReservasOnClickManager(snapshot, date, part, context, position, adapter, activity, mainHandler)));

        adapter.setOnItemLongClickListener((snapshot, position, v) -> App.executor.submit(
                new ReservasOnLongClickManager(snapshot, date, part, view, adapter, position)));
    }




}