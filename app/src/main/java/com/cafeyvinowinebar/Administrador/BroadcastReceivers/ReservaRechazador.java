package com.cafeyvinowinebar.Administrador.BroadcastReceivers;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class ReservaRechazador extends BroadcastReceiver {

    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {

        String fecha = intent.getStringExtra(Utils.KEY_FECHA);
        String hora = intent.getStringExtra(Utils.KEY_HORA);
        String mesa = intent.getStringExtra(Utils.KEY_MESA);
        String parte = intent.getStringExtra(Utils.KEY_PARTE);
        String token = intent.getStringExtra(Utils.KEY_TOKEN);
        int notiId = intent.getIntExtra(Utils.KEY_NOTI_ID, 0);

        // delete the reservation from the db
        fStore.collection(Utils.RESERVAS)
                .document(fecha)
                .collection(parte)
                .document(mesa)
                .delete();

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {

            // get the comment from the remote input
            String comentario = remoteInput.getCharSequence(Utils.KEY_COMENTARIO).toString();
            // the string can't be empty, because the RemoteInput doesn't activate if nothing is typed

            fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                    .setMessageId(Utils.getMessageId())
                    .addData(Utils.KEY_TOKEN, token)
                    .addData(Utils.KEY_FECHA, fecha)
                    .addData(Utils.KEY_HORA, hora)
                    .addData(Utils.KEY_ACTION, Utils.ACTION_RESERVA_NACK)
                    .addData(Utils.KEY_COMENTARIO, comentario)
                    .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                    .build());

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);

            // to dismiss the noti, we replace it with a kamikadze one
            Notification replyNotification = new Notification.Builder(context, App.RESERVA)
                    .setSmallIcon(R.drawable.img_mini_logo)
                    .setColor(context.getColor(R.color.notification))
                    .setContentText(context.getString(R.string.reserva_rechazo))
                    .setTimeoutAfter(1000)
                    .build();

            manager.notify(notiId, replyNotification);
        }
    }
}
