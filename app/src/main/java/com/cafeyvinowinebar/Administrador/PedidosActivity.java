package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.cafeyvinowinebar.Administrador.Fragments.MesaPicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Here admin can choose how he wants to see the sent orders: only kitchen products, only bar products, or all together
 * For that, there are three text views to choose from
 *
 * There are also two FABs
 * One to see the list of requested via the fidelity program gifts
 * Another to create a custom order
 */
public class PedidosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        String currentDate = Utils.getCurrentDate();

        TextView txtBarra = findViewById(R.id.txtBarra);
        TextView txtCocina = findViewById(R.id.txtCocina);
        TextView txtTodo = findViewById(R.id.txtTodo);

        FloatingActionButton fabAddPedido = findViewById(R.id.fabAddPedido);
        FloatingActionButton fabGift = findViewById(R.id.fabGift);

        fabGift.setOnClickListener(v -> startActivity(GiftsActivity.newIntent(getBaseContext(), false, currentDate)));

        fabAddPedido.setOnClickListener(v -> {
            new MesaPicker().show(getSupportFragmentManager(), "MESA_PICKER");
        });

        txtTodo.setOnClickListener(v -> startActivity(PedidosTodoActivity.newIntent(getBaseContext(), Utils.TODO)));

        txtCocina.setOnClickListener(v -> startActivity(PedidosTodoActivity.newIntent(getBaseContext(), Utils.COCINA)));

        txtBarra.setOnClickListener(v -> startActivity(PedidosTodoActivity.newIntent(getBaseContext(), Utils.BARRA)));
    }

}