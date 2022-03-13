package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;

/**
 * Displays precomputed values of the spending activity
 */
public class IngresoTotal extends DialogFragment {

    private double total, totalEfectivo, totalVisa, totalYape, totalCripto;

    public static IngresoTotal newInstance(double total, double totalEfectivo, double totalVisa, double totalYape, double totalCripto) {
        Bundle args = new Bundle();
        args.putDouble(Utils.TOTAL, total);
        args.putDouble(Utils.EFECTIVO, totalEfectivo);
        args.putDouble(Utils.VISA, totalVisa);
        args.putDouble(Utils.YAPE, totalYape);
        args.putDouble(Utils.CRIPTO, totalCripto);

        IngresoTotal fragment = new IngresoTotal();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // we retrieve the values passed with the arguments bundle
        assert getArguments() != null;
        total = getArguments().getDouble(Utils.TOTAL);
        totalEfectivo = getArguments().getDouble(Utils.EFECTIVO);
        totalVisa = getArguments().getDouble(Utils.VISA);
        totalYape = getArguments().getDouble(Utils.YAPE);
        totalCripto = getArguments().getDouble(Utils.CRIPTO);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // and populate the views with those values
        View view = inflater.inflate(R.layout.fragment_ingreso_total, container);
        TextView txtIngreso = view.findViewById(R.id.txtIngresoNum);
        TextView txtEfectivo = view.findViewById(R.id.txtEfectivoNum);
        TextView txtVisa = view.findViewById(R.id.txtVisaNum);
        TextView txtYape = view.findViewById(R.id.txtYapeNum);
        TextView txtCripto = view.findViewById(R.id.txtCriptoNum);
        txtIngreso.setText(String.valueOf(total));
        txtEfectivo.setText(String.valueOf(totalEfectivo));
        txtVisa.setText(String.valueOf(totalVisa));
        txtYape.setText(String.valueOf(totalYape));
        txtCripto.setText(String.valueOf(totalCripto));
        return view;

    }
}
