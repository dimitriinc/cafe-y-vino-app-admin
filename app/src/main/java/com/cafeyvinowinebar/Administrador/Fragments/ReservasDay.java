package com.cafeyvinowinebar.Administrador.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.cafeyvinowinebar.Administrador.ReservasDelDiaActivity;
import com.cafeyvinowinebar.Administrador.Runnables.ReservasOnClickManager;
import com.cafeyvinowinebar.Administrador.Runnables.ReservasOnLongClickManager;
import com.cafeyvinowinebar.Administrador.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Displays day reservations for the day
 * The on click functionality depends on the state of the reservation
 * So we assign two runnables to manage it: one for on click, another for on long click
 */
public class ReservasDay extends Fragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public AdapterReservas adapter;
    public String date, part;
    private RecyclerView recViewDay;
    public Context context;
    public Handler mainHandler;
    public Activity activity;
    public View view;

    public static ReservasDay newInstance(String date) {
        Bundle args = new Bundle();
        args.putString(Utils.KEY_DATE, date);
        ReservasDay fragment = new ReservasDay();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());
        activity = getActivity();
        context = getContext();
        part = Utils.DIA;
        date = requireActivity().getIntent().getStringExtra(Utils.KEY_DATE);
        assert getArguments() != null;
        date = getArguments().getString(Utils.KEY_DATE);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reservas_day, container, false);

        if (container != null) {
            container.removeAllViews();
        }

        recViewDay = view.findViewById(R.id.recViewDay);
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
        recViewDay.setAdapter(adapter);
        recViewDay.setLayoutManager(new GridLayoutManager(context, 3));

        adapter.setOnItemClickListener((snapshot, position, view) ->
                App.executor.submit(new ReservasOnClickManager(snapshot, date, part, context, position, adapter, activity, mainHandler)));
        adapter.setOnItemLongClickListener((snapshot, position, v) ->
                App.executor.submit(new ReservasOnLongClickManager(snapshot, date, part, view, adapter, position)));
    }
}