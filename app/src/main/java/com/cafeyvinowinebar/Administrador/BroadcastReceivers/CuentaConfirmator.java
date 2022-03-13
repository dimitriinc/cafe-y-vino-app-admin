package com.cafeyvinowinebar.Administrador.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.CuentasActivity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

/**
 * Sends a cuenta confirmation message to the user
 */
public class CuentaConfirmator extends BroadcastReceiver {

    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {

        String token = intent.getStringExtra(Utils.KEY_TOKEN);
        int notiId = intent.getIntExtra(Utils.KEY_NOTI_ID, 0);

        fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                .setMessageId(Utils.getMessageId())
                .addData(Utils.KEY_TOKEN, token)
                .addData(Utils.KEY_ACTION, Utils.ACTION_CUENTA_ACK)
                .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                .build());

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(notiId);
    }
}
