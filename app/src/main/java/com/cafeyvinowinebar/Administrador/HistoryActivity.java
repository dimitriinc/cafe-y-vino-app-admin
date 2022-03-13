package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterHistory;
import com.cafeyvinowinebar.Administrador.Fragments.Consumo;
import com.cafeyvinowinebar.Administrador.POJOs.CuentaCancelada;
import com.cafeyvinowinebar.Administrador.Runnables.IngresoTotalDisplayer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays all the canceled bills of the customer
 * Gets the customer's id with the intent to know what customer it checks
 * With FABs we can view the consumption statists of the customer
 * And the spending statists as well
 */
public class HistoryActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private RecyclerView recHistory;
    private FloatingActionButton fabHome, fabMoney, fabCanasta;
    public Handler mainHandler;
    private String userId;
    public FragmentManager manager;
    Query query, queryConsumo;

    public static Intent newIntent(Context context, String userId) {
        Intent i = new Intent(context, HistoryActivity.class);
        i.putExtra(Utils.KEY_USER_ID, userId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        init();

        fabHome.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        // displays how much the user has spent
        fabMoney.setOnClickListener(v -> App.executor.submit(new IngresoTotalDisplayer(query, mainHandler, manager)));

        // displays all the products the user has consumed
        fabCanasta.setOnClickListener(v -> new Consumo(queryConsumo).show(manager, "CONSUMO"));
    }

    private void setupAdapter() {

        // the FirestoreAdapter can't display the collection group correctly for some reason
        // so we create a list of CuentaCancelada objects, and populate it with the docs we extract from the querySnapshot
        // and pass the list to a standard adapter, that extends the ListAdapter
        query = fStore.collectionGroup("cuentas_canceladas")
                .whereEqualTo(Utils.KEY_USER_ID, userId)
                .orderBy(Utils.TIMESTAMP, Query.Direction.ASCENDING);

        query.get().addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

            List<CuentaCancelada> cuentasCanceladas = new ArrayList<>();

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                cuentasCanceladas.add(snapshot.toObject(CuentaCancelada.class));
            }
            mainHandler.post(() -> {
                AdapterHistory adapter = new AdapterHistory(HistoryActivity.this);
                recHistory.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
                recHistory.setHasFixedSize(true);
                recHistory.setAdapter(adapter);
                adapter.submitList(cuentasCanceladas);
            });

        });
    }

    private void init() {
        mainHandler = new Handler();
        userId = getIntent().getStringExtra(Utils.KEY_USER_ID);
        fabHome = findViewById(R.id.fabHistoryHome);
        fabCanasta = findViewById(R.id.fabCanastaHistory);
        fabMoney = findViewById(R.id.fabPersonalConsumption);
        recHistory = findViewById(R.id.recHistory);
        manager = getSupportFragmentManager();
        queryConsumo = fStore.collection(Utils.USUARIOS)
                .document(userId)
                .collection("consumo")
                .orderBy(Utils.KEY_COUNT, Query.Direction.DESCENDING);
        setupAdapter();
    }
}