package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCuentasCanceladas;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaCancelada;
import com.cafeyvinowinebar.Administrador.Runnables.IngresoTotalDisplayer;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Receives a date from a date picker
 * And displays all the canceled bills of the date
 * Contains a gifts FAB that directs to the display of all the served gifts of the received date
 */
public class CuentasCanceladasDelDiaActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private RecyclerView recCuentasCanceladas;
    public String date;
    public Query query;
    private AdapterCuentasCanceladas adapter;
    public FragmentManager manager;

    public static Intent newIntent(Context context, String date) {
        Intent i = new Intent(context, CuentasCanceladasDelDiaActivity.class);
        i.putExtra(Utils.KEY_DATE, date);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuentas_canceladas_del_dia);

        recCuentasCanceladas = findViewById(R.id.recCuentasCanceladas);
        FloatingActionButton fabCuentasHome = findViewById(R.id.fabCuentasHome);
        fabCuentasHome.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), MainActivity.class)));
        FloatingActionButton fabGift = findViewById(R.id.fabGift_cancel);
        FloatingActionButton fabMoney = findViewById(R.id.fabMoney);
        Handler mainHandler = new Handler();
        manager = getSupportFragmentManager();



        Intent intent = getIntent();
        if (intent != null) {
            date = intent.getStringExtra(Utils.KEY_DATE);
            if (!date.isEmpty()) {
                query = fStore.collection(Utils.CUENTAS)
                        .document(date)
                        .collection("cuentas_canceladas");

                setUpRecyclerView();

                // go to see the gift items served on that day
                fabGift.setOnClickListener(v -> startActivity(GiftsActivity.newIntent(getBaseContext(), true, date)));

                // displays how much money was gained during the day, and how it's divided between the payment types
                fabMoney.setOnClickListener(v -> App.executor.submit(new IngresoTotalDisplayer(query, mainHandler,
                        manager)));
            }
        }
    }

    public void setUpRecyclerView() {
        FirestoreRecyclerOptions<CuentaCancelada> options = new FirestoreRecyclerOptions.Builder<CuentaCancelada>()
                .setQuery(query, CuentaCancelada.class)
                .build();
        adapter = new AdapterCuentasCanceladas(options, CuentasCanceladasDelDiaActivity.this);
        recCuentasCanceladas.setAdapter(adapter);
        recCuentasCanceladas.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}