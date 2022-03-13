package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.cafeyvinowinebar.Administrador.Fragments.Login;
import com.cafeyvinowinebar.Administrador.Fragments.Registration;

public class BienvenidoActivity extends AppCompatActivity {

    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenido);

        handler = new Handler();

        FragmentManager manager = getSupportFragmentManager();

        TextView txtRegister = findViewById(R.id.txtRegister);
        TextView txtLogin = findViewById(R.id.txtLogin);

        txtRegister.setOnClickListener(v -> {
            Registration fragmentRegistration = new Registration(handler);
            fragmentRegistration.show(manager, Utils.TAG);
        });

        txtLogin.setOnClickListener(v -> {
            Login fragmentLogin = new Login(handler);
            fragmentLogin.show(manager, Utils.TAG);
        });
    }
}