package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class VinosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vinos);

        TextView txtTintos = findViewById(R.id.txtTintos);
        TextView txtBlancos = findViewById(R.id.txtBlancos);
        TextView txtPostre = findViewById(R.id.txtPostre);
        TextView txtCopa = findViewById(R.id.txtCopa);
        TextView txtRose = findViewById(R.id.txtRose);

        txtTintos.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/03.vinos/vinos/Vinos tintos/vinos")));

        txtBlancos.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/03.vinos/vinos/Vinos blancos/vinos")));

        txtPostre.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/03.vinos/vinos/Vinos de postre/vinos")));

        txtCopa.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/03.vinos/vinos/Vinos por copa/vinos")));

        txtRose.setOnClickListener(v -> startActivity(MenuCategoryActivity.newIntent(getBaseContext(), "menu/03.vinos/vinos/Vinos rose/vinos")));
    }
}