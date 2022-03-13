package com.cafeyvinowinebar.Administrador.BroadcastReceivers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import androidx.core.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Runnables.DoorOpener;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class AssignMesaReceiver extends BroadcastReceiver {

    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    @SuppressLint({"DefaultLocale", "UnspecifiedImmutableFlag"})
    @Override
    public void onReceive(Context context, Intent intent) {
        String userId = intent.getStringExtra(Utils.KEY_USER_ID);
        String token = intent.getStringExtra(Utils.KEY_TOKEN);
        String nombre = intent.getStringExtra(Utils.KEY_NOMBRE);
        int notiId = intent.getIntExtra(Utils.KEY_NOTI_ID, 0);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (remoteInput != null) {

            // get the value from the remote input
            String mesa = remoteInput.getCharSequence(Utils.KEY_MESA).toString();
            try {

                // we want to format the received string
                long mesaLong = Long.parseLong(mesa);
                // if admin entered something rather than numbers, the conversion to long will through an exception

                String mesaFormat = String.format("%02d", mesaLong);

                // update the presence status and the 'mesa' field of the user's doc on a background thread
                App.executor.submit(new DoorOpener(userId, mesaFormat));

                // send a welcome message
                fMessaging.send(new RemoteMessage.Builder(context.getString(R.string.fcm_id, App.SENDER_ID))
                        .setMessageId(Utils.getMessageId())
                        .addData(Utils.KEY_TOKEN, token)
                        .addData(Utils.KEY_NOMBRE, nombre)
                        .addData(Utils.KEY_ACTION, Utils.ACTION_PUERTA_ADMIN)
                        .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                        .build());

                // i couldn't figure out how to dismiss the noti right away if it contains a Remote Input
                // so i change it to a noti that will last 1 second and then destroys itself
                Notification replyNotification = new Notification.Builder(context, App.PUERTA)
                        .setSmallIcon(R.drawable.img_mini_logo)
                        .setColor(context.getColor(R.color.notification))
                        .setContentText(context.getString(R.string.puerta_update))
                        .setTimeoutAfter(1000)
                        .build();

                NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                manager.notify(notiId, replyNotification);

            } catch (Exception e){

                // admin entered a non-numerical value to the remote input
                // so we rebuild the same noti
                RemoteInput mesaInput = new RemoteInput.Builder(Utils.KEY_MESA)
                        .setLabel(context.getString(R.string.puerta_input))
                        .setAllowFreeFormInput(true)
                        .build();
                Intent assignMesaIntent = new Intent(context, AssignMesaReceiver.class);
                assignMesaIntent.putExtra(Utils.KEY_USER_ID, userId);
                assignMesaIntent.putExtra(Utils.KEY_TOKEN, token);
                assignMesaIntent.putExtra(Utils.KEY_NOTI_ID, notiId);
                assignMesaIntent.putExtra(Utils.KEY_NOMBRE, nombre);
                PendingIntent assignMesaPendingIntent = PendingIntent.getBroadcast(context, 1, assignMesaIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action assignMesaAction = new NotificationCompat.Action.Builder(R.drawable.img_mini_logo,
                        context.getString(R.string.aceptar), assignMesaPendingIntent)
                        .addRemoteInput(mesaInput)
                        .build();


                // but with different text
                Notification notiPuerta = new NotificationCompat.Builder(context, App.PUERTA)
                        .setContentTitle(context.getString(R.string.puerta_update_title, nombre))
                        .setContentText(context.getString(R.string.puerta_update_text))
                        .setColor(context.getColor(R.color.notification))
                        .setSmallIcon(R.drawable.img_mini_logo)
                        .addAction(assignMesaAction)
                        .build();

                NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                manager.notify(notiId, notiPuerta);
            }
        }
    }
}
