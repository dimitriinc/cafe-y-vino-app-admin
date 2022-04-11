package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.MesasViewModel;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.CuentaCancelador;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Displays a set of edit text for each payment type
 * Admin can divide the bill by typing in the desired numbers to the edit texts
 */
public class CuentaDivider extends DialogFragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private TextView txtSumToDivide, txtEfectivoOk, txtVisaOk, txtYapeOk, txtCriptoOk;
    private EditText edtEfectivo, edtVisa, edtYape, edtCripto;
    private Button btnListo;
    public String currentDate;
    private FloatingActionButton fabReset;
    public DocumentSnapshot snapshot;
    public double total, montoEfectivo, montoVisa, montoYape, montoCripto;

    public CuentaDivider(String currentDate, DocumentSnapshot snapshot) {
        this.currentDate = currentDate;
        this.snapshot = snapshot;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuenta_divider, container);

        String userId = snapshot.getId();

        init(view);

        // at the top of the dialog we display the total sum of the bill
        fStore.collection(Utils.CUENTAS)
                .document(currentDate)
                .collection("cuentas corrientes")
                .document(userId)
                .collection(Utils.CUENTA)
                .get()
                .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        total += doc.getLong(Utils.TOTAL);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> txtSumToDivide.setText(String.valueOf(total)));
                });

        txtEfectivoOk.setOnClickListener(v -> acceptDivision(edtEfectivo, Utils.EFECTIVO));

        txtVisaOk.setOnClickListener(v -> acceptDivision(edtVisa, Utils.VISA));

        txtYapeOk.setOnClickListener(v -> acceptDivision(edtYape, Utils.YAPE));

        txtCriptoOk.setOnClickListener(v -> acceptDivision(edtCripto, Utils.CRIPTO));

        // clears all the values, make the ok text views visible, reset the listeners;
        // and admin starts again
        fabReset.setOnClickListener(v -> {
            txtSumToDivide.setText(String.valueOf(total));
            montoEfectivo = 0;
            montoVisa = 0;
            montoYape = 0;
            montoCripto = 0;
            edtEfectivo.getText().clear();
            edtVisa.getText().clear();
            edtYape.getText().clear();
            edtCripto.getText().clear();
            txtEfectivoOk.setVisibility(View.VISIBLE);
            txtVisaOk.setVisibility(View.VISIBLE);
            txtYapeOk.setVisibility(View.VISIBLE);
            txtCriptoOk.setVisibility(View.VISIBLE);
            txtEfectivoOk.setOnClickListener(vi -> acceptDivision(edtEfectivo, Utils.EFECTIVO));
            txtVisaOk.setOnClickListener(vi -> acceptDivision(edtVisa, Utils.VISA));
            txtYapeOk.setOnClickListener(vi -> acceptDivision(edtYape, Utils.YAPE));
            txtCriptoOk.setOnClickListener(vi -> acceptDivision(edtCripto, Utils.CRIPTO));
        });

        btnListo.setOnClickListener(v -> {

            // if the division is performed correctly the sum of all the monto doubles must be equal the 'total' value
            if (montoEfectivo + montoVisa + montoYape + montoCripto != total) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            } else {
                App.executor.submit(new CuentaCancelador(montoEfectivo, montoVisa, montoYape, montoCripto, snapshot, currentDate, Utils.DIVIDIDO));
                dismiss();
            }
        });

        return view;
    }

    private void init(View view) {
        txtSumToDivide = view.findViewById(R.id.txtSumToDivide);
        txtEfectivoOk = view.findViewById(R.id.txtEffectivoOk);
        txtVisaOk = view.findViewById(R.id.txtVisaOk);
        txtYapeOk = view.findViewById(R.id.txtYapeOk);
        txtCriptoOk = view.findViewById(R.id.txtCriptoOk);
        edtEfectivo = view.findViewById(R.id.edtEfectivo);
        edtVisa = view.findViewById(R.id.edtVisa);
        edtYape = view.findViewById(R.id.edtYape);
        edtCripto = view.findViewById(R.id.edtCripto);
        fabReset = view.findViewById(R.id.fabReset);
        btnListo = view.findViewById(R.id.btnListo);
    }

    /**
     * Get the value from the edit text, converts it to a double
     * Based on the montoType stores the double into the appropriate member var and disables the appropriate txt button
     * Does the subtraction from the total price and sets the result on the text view to show how much left
     */
    private void acceptDivision(EditText editText, String montoType) {

        String input = editText.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
        } else {

            double inputNum = Double.parseDouble(input);

            switch (montoType) {
                case Utils.EFECTIVO:
                    montoEfectivo = inputNum;
                    txtEfectivoOk.setVisibility(View.INVISIBLE);
                    txtEfectivoOk.setOnClickListener(null);
                    break;
                case Utils.VISA:
                    montoVisa = inputNum;
                    txtVisaOk.setVisibility(View.INVISIBLE);
                    txtVisaOk.setOnLongClickListener(null);
                    break;
                case Utils.YAPE:
                    montoYape = inputNum;
                    txtYapeOk.setVisibility(View.INVISIBLE);
                    txtVisaOk.setOnClickListener(null);
                    break;
                case Utils.CRIPTO:
                    montoCripto = inputNum;
                    txtCriptoOk.setVisibility(View.INVISIBLE);
                    txtCriptoOk.setOnClickListener(null);
                    break;
            }

            double resto = Double.parseDouble(txtSumToDivide.getText().toString()) - inputNum;
            txtSumToDivide.setText(String.valueOf(resto));

        }
    }
}
