package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.cafeyvinowinebar.Administrador.Fragments.Consumo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Shows the list of categories to choose from, to manage their products
 * Also a FAB to open a fragment dialog Consumo to review the statistics of all the products sold
 */
public class MainMenuActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        FragmentManager manager = getSupportFragmentManager();
        Query query = fStore.collection("consumo_total")
                .orderBy(Utils.KEY_COUNT, Query.Direction.DESCENDING);

        TextView txtOfertas = findViewById(R.id.txtOfertas);
        TextView txtPlatos = findViewById(R.id.txtPlatos);
        TextView txtVinos = findViewById(R.id.txtVinos);
        TextView txtCocteles = findViewById(R.id.txtCocteles);
        TextView txtCervezas = findViewById(R.id.txtCervezas);
        TextView txtBebidasCalientes = findViewById(R.id.txtBebidasCalientes);
        TextView txtPiqueos = findViewById(R.id.txtPiqueos);
        TextView txtRegalos = findViewById(R.id.txtRegalos);
        FloatingActionButton fabConsumo = findViewById(R.id.fabConsumoTotal);

        txtCocteles.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/04.cocteles/cocteles")));

        txtPlatos.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/01.platos/platos")));

        txtPiqueos.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/02.piqueos/piqueos")));

        // the wines category has an intermediary activity to choose the type of wines
        txtVinos.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), VinosActivity.class)));

        txtCervezas.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/05.cervezas/cervezas")));

        txtBebidasCalientes.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/06.bebidas calientes/bebidas calientes")));

        txtOfertas.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/07.ofertas/ofertas")));

        txtRegalos.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "regalos")));

        fabConsumo.setOnClickListener(v -> new Consumo(query).show(manager, "TOTAL"));
    }

}