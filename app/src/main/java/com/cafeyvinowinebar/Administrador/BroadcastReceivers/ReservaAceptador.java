package com.cafeyvinowinebar.Administrador.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class ReservaAceptador extends BroadcastReceiver {

    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {

        String fecha = intent.getStringExtra(Utils.KEY_FECHA);
        String parte = intent.getStringExtra(Utils.KEY_PARTE);
        String mesa = intent.getStringExtra(Utils.KEY_MESA);
        String token = intent.getStringExtra(Utils.KEY_TOKEN);
        String hora = intent.getStringExtra(Utils.KEY_HORA);
        String pax = intent.getStringExtra(Utils.KEY_PAX);
        int notiId = intent.getIntExtra(Utils.KEY_NOTI_ID, 0);

        // update the confirmado field of the reservation doc
        fStore.collection(Utils.RESERVAS)
                .document(fecha)
                .collection(parte)
                .document(mesa)
                .update(Utils.KEY_CONFIRMADO, true);

        // let the user know that their reservation is accepted
        fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                .setMessageId(Utils.getMessageId())
                .addData(Utils.KEY_TOKEN, token)
                .addData(Utils.KEY_FECHA, fecha)
                .addData(Utils.KEY_HORA, hora)
                .addData(Utils.KEY_PAX, pax)
                .addData(Utils.KEY_ACTION, Utils.ACTION_RESERVA_ACK)
                .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                .build());

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(notiId);

    }
}
