package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseMessaging messaging = FirebaseMessaging.getInstance();

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

        messaging.getToken().addOnSuccessListener(s -> Log.i(TAG, "onCreate: the token:: " + s));
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


    }
}