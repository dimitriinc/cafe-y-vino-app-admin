package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cafeyvinowinebar.Administrador.Fragments.NotiPedidoDisplayer;

/**
 * Is transparent, shows a dialog fragment
 */
public class PedidoDisplayerActivity extends AppCompatActivity {

    public static Intent newIntent(Context context, String metaDocId, String fecha, String token, int notiId, String nombre, String mesa) {
        Intent i = new Intent(context, PedidoDisplayerActivity.class);
        i.putExtra(Utils.KEY_META_ID, metaDocId);
        i.putExtra(Utils.KEY_FECHA, fecha);
        i.putExtra(Utils.KEY_TOKEN, token);
        i.putExtra(Utils.KEY_NOTI_ID, notiId);
        i.putExtra(Utils.KEY_NOMBRE, nombre);
        i.putExtra(Utils.KEY_MESA, mesa);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_displayer);

        new NotiPedidoDisplayer().show(getSupportFragmentManager(), Utils.TAG);

    }
}