package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.CuentaCancelador;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * A dialog fragment where admin chooses the payment type
 * The type value gets passed to the CuentaCancelador runnable for the actual cancelation
 * A special use case: the bill must be divided in different payment types
 * In this case we open a new dialog fragment CuentaDivider where admin can assign values to the division
 * Also, client can leave a tip with visa, yape, or cripto; in this case the staff will take out this tip in cash from the cashier
 * In this case we store the tip ammount to subtract it later from the cash earning of the day, and add it to whatever the pay type that was
 */
public class PayTypePicker extends DialogFragment {

    public DocumentSnapshot snapshot;
    public String currentDate;
    private final FragmentManager manager;
    private EditText edtPropina;
    Long propinaVisa, propinaYape, propinaCripto;

    public PayTypePicker(DocumentSnapshot snapshot, String currentDate, FragmentManager manager) {
        this.snapshot = snapshot;
        this.currentDate = currentDate;
        this.manager = manager;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_mode_picker, container);

        TextView txtCash = view.findViewById(R.id.txtPayEfectivo);
        TextView txtVisa = view.findViewById(R.id.txtPayVisa);
        TextView txtYape = view.findViewById(R.id.txtPayYape);
        TextView txtCripto = view.findViewById(R.id.txtPayCripto);
        Button btnDivide = view.findViewById(R.id.btnDivide);
        edtPropina = view.findViewById(R.id.edtPropina);

        txtCash.setOnClickListener(v -> {
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.EFECTIVO, propinaVisa, propinaYape, propinaCripto));
            dismiss();
        });
        txtVisa.setOnClickListener(v -> {
            getPropina(Utils.VISA);
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.VISA, propinaVisa, propinaYape, propinaCripto));
            dismiss();
        });
        txtYape.setOnClickListener(v -> {
            getPropina(Utils.YAPE);
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.YAPE, propinaVisa, propinaYape, propinaCripto));
            dismiss();
        });
        txtCripto.setOnClickListener(v -> {
            getPropina(Utils.CRIPTO);
            App.executor.submit(new CuentaCancelador(snapshot, currentDate, Utils.CRIPTO, propinaVisa, propinaYape, propinaCripto));
            dismiss();
        });
        btnDivide.setOnClickListener(v -> {
            CuentaDivider fragment = new CuentaDivider(currentDate, snapshot);
            fragment.show(manager, Utils.TAG);
            dismiss();
        });

        return view;
    }

    private void getPropina(String payType) {
        switch (payType) {
            case Utils.VISA:
                String propinaVisaTxt = edtPropina.getText().toString().trim();
                if (!propinaVisaTxt.isEmpty()) {
                    propinaVisa = Long.parseLong(propinaVisaTxt);
                }
                break;
            case Utils.YAPE:
                String propinaYapeTxt = edtPropina.getText().toString().trim();
                if (!propinaYapeTxt.isEmpty()) {
                    propinaYape = Long.parseLong(propinaYapeTxt);
                }
                break;
            case Utils.CRIPTO:
                String propinaCriptoTxt = edtPropina.getText().toString().trim();
                if (!propinaCriptoTxt.isEmpty()) {
                    propinaCripto = Long.parseLong(propinaCriptoTxt);
                }
                break;
            default:
                break;
        }
    }
}
