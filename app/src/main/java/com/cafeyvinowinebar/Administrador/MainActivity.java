package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseUser user = fAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, BienvenidoActivity.class));
            finish();
            return;
        }

        TextView txtMenu = findViewById(R.id.txtMenu);
        TextView txtPedidos = findViewById(R.id.txtPedidos);
        TextView txtReservas = findViewById(R.id.txtReservas);
        TextView txtCuentas = findViewById(R.id.txtCuentas);
        TextView txtUsarios = findViewById(R.id.txtUsuarios);


        txtMenu.setOnClickListener(v -> startActivity(new Intent(this, MainMenuActivity.class)));
        txtReservas.setOnClickListener(v -> startActivity(new Intent(this, ReservasDatePickerActivity.class)));
        txtPedidos.setOnClickListener(v -> startActivity(new Intent(this, PedidosActivity.class)));
        txtCuentas.setOnClickListener(v -> startActivity(new Intent(this, CuentasActivity.class)));
        txtUsarios.setOnClickListener(v -> startActivity(new Intent(this, UsuariosActivity.class)));

//        for (String name : Utils.FIXED_MESAS) {
//            fStore.collection("mesas").add(new Mesa(false, true, false, name));
//        }


    }
}