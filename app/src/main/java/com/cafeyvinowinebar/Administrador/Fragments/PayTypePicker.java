package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.MesasViewModel;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.CuentaCancelador;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

/**
 * A dialog fragment where admin chooses the payment type
 * The type value gets passed to the CuentaCancelador runnable for the actual cancelation
 * A special use case: the bill must be divided in different payment types
 * In this case we open a new dialog fragment CuentaDivider where admin can assign values to the division
 */
public class PayTypePicker extends DialogFragment {

    public DocumentSnapshot snapshot;
    public String currentDate;
    private final FragmentManager manager;
    public MesasViewModel mesasViewModel;

    public PayTypePicker(DocumentSnapshot snapshot, String currentDate, FragmentManager manager) {
        this.snapshot = snapshot;
        this.currentDate = currentDate;
        this.manager = manager;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_mode_picker, container);

        mesasViewModel = new ViewModelProvider(requireActivity()).get(MesasViewModel.class);

        TextView txtCash = view.findViewById(R.id.txtPayEfectivo);
        TextView txtVisa = view.findViewById(R.id.txtPayVisa);
        TextView txtYape = view.findViewById(R.id.txtPayYape);
        TextView txtCripto = view.findViewById(R.id.txtPayCripto);
        Button btnDivide = view.findViewById(R.id.btnDivide);

        txtCash.setOnClickListener(v -> {
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.EFECTIVO, mesasViewModel));
            dismiss();
        });
        txtVisa.setOnClickListener(v -> {
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.VISA, mesasViewModel));
            dismiss();
        });
        txtYape.setOnClickListener(v -> {
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.YAPE, mesasViewModel));
            dismiss();
        });
        txtCripto.setOnClickListener(v -> {
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.CRIPTO, mesasViewModel));
            dismiss();
        });
        btnDivide.setOnClickListener(v -> {
            CuentaDivider fragment = new CuentaDivider(currentDate, snapshot);
            fragment.show(manager, Utils.TAG);
            dismiss();
        });

        return view;
    }
}
