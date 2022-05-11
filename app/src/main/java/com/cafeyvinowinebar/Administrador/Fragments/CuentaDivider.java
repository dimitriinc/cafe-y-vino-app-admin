package com.cafeyvinowinebar.Administrador.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cafeyvinowinebar.Administrador.App;
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

    private TextView txtSumToDivide;
    private FloatingActionButton fabEfectivoOk, fabVisaOk, fabYapeOk, fabCriptoOk;
    private Button btnTipVisa, btnTipYape, btnTipCripto;
    private EditText edtEfectivo, edtVisa, edtYape, edtCripto;
    private Button btnListo;
    public String currentDate;
    private FloatingActionButton fabReset;
    public DocumentSnapshot snapshot;
    public double total, montoEfectivo, montoVisa, montoYape, montoCripto;
    Long propinaVisa, propinaYape, propinaCripto;

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

        fabEfectivoOk.setOnClickListener(v -> acceptDivision(edtEfectivo, Utils.EFECTIVO));

        fabVisaOk.setOnClickListener(v -> acceptDivision(edtVisa, Utils.VISA));

        fabYapeOk.setOnClickListener(v -> acceptDivision(edtYape, Utils.YAPE));

        fabCriptoOk.setOnClickListener(v -> acceptDivision(edtCripto, Utils.CRIPTO));

        btnTipVisa.setOnClickListener(v -> {
            if (propinaVisa == null) {
                showTipDialog(Utils.VISA);
            } else {
                showStoredTipDialog(propinaVisa);
            }
        });

        btnTipYape.setOnClickListener(v -> {
            if (propinaYape == null) {
                showTipDialog(Utils.YAPE);
            } else {
                showStoredTipDialog(propinaYape);
            }
        });

        btnTipCripto.setOnClickListener(v -> {
            if (propinaCripto == null) {
                showTipDialog(Utils.CRIPTO);
            } else {
                showStoredTipDialog(propinaCripto);
            }
        });

        // clears all the values, make the ok text views visible, tip buttons gone, reset the listeners;
        // and admin starts again
        fabReset.setOnClickListener(v -> {
            txtSumToDivide.setText(String.valueOf(total));
            montoEfectivo = 0;
            montoVisa = 0;
            montoYape = 0;
            montoCripto = 0;
            propinaVisa = null;
            propinaCripto = null;
            propinaYape = null;
            edtEfectivo.getText().clear();
            edtVisa.getText().clear();
            edtYape.getText().clear();
            edtCripto.getText().clear();
            fabEfectivoOk.setVisibility(View.VISIBLE);
            fabVisaOk.setVisibility(View.VISIBLE);
            fabYapeOk.setVisibility(View.VISIBLE);
            fabCriptoOk.setVisibility(View.VISIBLE);
            btnTipVisa.setVisibility(View.GONE);
            btnTipYape.setVisibility(View.GONE);
            btnTipCripto.setVisibility(View.GONE);
            fabEfectivoOk.setOnClickListener(vi -> acceptDivision(edtEfectivo, Utils.EFECTIVO));
            fabVisaOk.setOnClickListener(vi -> acceptDivision(edtVisa, Utils.VISA));
            fabYapeOk.setOnClickListener(vi -> acceptDivision(edtYape, Utils.YAPE));
            fabCriptoOk.setOnClickListener(vi -> acceptDivision(edtCripto, Utils.CRIPTO));
        });

        btnListo.setOnClickListener(v -> {

            // if the division is performed correctly the sum of all the monto doubles must be equal the 'total' value
            if (montoEfectivo + montoVisa + montoYape + montoCripto != total) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            } else {
                App.executor.submit(new CuentaCancelador(montoEfectivo, montoVisa, montoYape, montoCripto, snapshot, currentDate, Utils.DIVIDIDO,
                        propinaVisa, propinaYape, propinaCripto));
                dismiss();
            }
        });

        return view;
    }

    private void init(View view) {
        txtSumToDivide = view.findViewById(R.id.txtSumToDivide);
        fabEfectivoOk = view.findViewById(R.id.fabEffectivoOk);
        fabVisaOk = view.findViewById(R.id.fabVisaOk);
        fabYapeOk = view.findViewById(R.id.fabYapeOk);
        fabCriptoOk = view.findViewById(R.id.fabCriptoOk);
        edtEfectivo = view.findViewById(R.id.edtEfectivo);
        edtVisa = view.findViewById(R.id.edtVisa);
        edtYape = view.findViewById(R.id.edtYape);
        edtCripto = view.findViewById(R.id.edtCripto);
        fabReset = view.findViewById(R.id.fabReset);
        btnListo = view.findViewById(R.id.btnListo);
        btnTipCripto = view.findViewById(R.id.btnTipCripto);
        btnTipVisa = view.findViewById(R.id.btnTipVisa);
        btnTipYape = view.findViewById(R.id.btnTipYape);
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
                    fabEfectivoOk.setVisibility(View.INVISIBLE);
                    fabEfectivoOk.setOnClickListener(null);
                    break;
                case Utils.VISA:
                    montoVisa = inputNum;
                    fabVisaOk.setVisibility(View.GONE);
                    fabVisaOk.setOnLongClickListener(null);
                    btnTipVisa.setVisibility(View.VISIBLE);
                    break;
                case Utils.YAPE:
                    montoYape = inputNum;
                    fabYapeOk.setVisibility(View.GONE);
                    fabYapeOk.setOnClickListener(null);
                    btnTipYape.setVisibility(View.VISIBLE);
                    break;
                case Utils.CRIPTO:
                    montoCripto = inputNum;
                    fabCriptoOk.setVisibility(View.GONE);
                    fabCriptoOk.setOnClickListener(null);
                    btnTipCripto.setVisibility(View.VISIBLE);
                    break;
            }

            double resto = Double.parseDouble(txtSumToDivide.getText().toString()) - inputNum;
            txtSumToDivide.setText(String.valueOf(resto));

        }
    }

    /**
     * Displays a dialog to initialize a tip value depending on the payment type
     */
    private void showTipDialog(String tipVersion) {

        // not to create a new layout, we'll use the one for custom mesa, and change some of its specs
        View tipView = getLayoutInflater().inflate(R.layout.dialog_custom_mesa, null);
        EditText edtTip = tipView.findViewById(R.id.edtNewMesa);
        FloatingActionButton fabTipOk = tipView.findViewById(R.id.fabOkNewMesa);
        edtTip.setHint(R.string.tip);
        edtTip.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(tipView)
                .create();

        fabTipOk.setOnClickListener(tipV -> {
            String tip = edtTip.getText().toString().trim();
            if (!tip.isEmpty()) {
                switch (tipVersion) {
                    case Utils.VISA:
                        propinaVisa = Long.parseLong(tip);
                        break;
                    case Utils.YAPE:
                        propinaYape = Long.parseLong(tip);
                        break;
                    case Utils.CRIPTO:
                        propinaCripto = Long.parseLong(tip);
                        break;
                    default:
                        break;
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * In case the tip has already been stored, we show how much it is
     */
    private void showStoredTipDialog(long propinaMonto) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.propina_title)
                .setMessage(String.valueOf(propinaMonto))
                .create()
                .show();
    }
}
