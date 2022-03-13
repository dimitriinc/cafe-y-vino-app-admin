package com.cafeyvinowinebar.Administrador;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.cafeyvinowinebar.Administrador.BroadcastReceivers.AssignMesaReceiver;
import com.cafeyvinowinebar.Administrador.BroadcastReceivers.CuentaConfirmator;
import com.cafeyvinowinebar.Administrador.BroadcastReceivers.PedidoConfirmator;
import com.cafeyvinowinebar.Administrador.BroadcastReceivers.ReservaAceptador;
import com.cafeyvinowinebar.Administrador.BroadcastReceivers.ReservaRechazador;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

@SuppressLint("UnspecifiedImmutableFlag")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManagerCompat manager;
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String currentDate;

    @Override
    public void onCreate() {
        super.onCreate();
        manager = NotificationManagerCompat.from(this);
        currentDate = Utils.getCurrentDate();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String adminId = fAuth.getUid();
        if (adminId != null) {
            fStore.collection(Utils.ADMINS).document(adminId).update(Utils.KEY_TOKEN, s);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String action = remoteMessage.getData().get(Utils.KEY_ACTION);

        switch (Objects.requireNonNull(action)) {
            case Utils.ACTION_PUERTA:
                processPuertaMessage(remoteMessage);
                break;
            case Utils.ACTION_RESERVA:
                processReservaMessage(remoteMessage);
                break;
            case Utils.ACTION_PEDIDO:
                processPedidoMessage(remoteMessage);
                break;
            case Utils.ACTION_CUENTA:
                processCuentaMessage(remoteMessage);
                break;
            case Utils.ACTION_REGALO:
                processRegaloMessage(remoteMessage);
                break;
            default:
                break;
        }
    }

    /**
     * Tells the admin who on what table want to cancel the bill
     * On a 'confirm' button admin can send a confirmation message to the user
     */
    private void processCuentaMessage(RemoteMessage message) {

        int notiId = new Random().nextInt();

        String token = message.getData().get(Utils.KEY_TOKEN);
        String nombre = message.getData().get(Utils.KEY_NOMBRE);
        String mesa = message.getData().get(Utils.KEY_MESA);
        String modo = message.getData().get(Utils.KEY_MODO);

        // define intents for the action button and for the tap
        Intent tapIntent = new Intent(this, CuentasActivity.class);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(this, 3,
                tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // on pressing the 'confirmar' action button, we go to a broadcast receiver to send a confirmation message
        Intent confirmarIntent = new Intent(this, CuentaConfirmator.class);
        confirmarIntent.putExtra(Utils.KEY_TOKEN, token);
        confirmarIntent.putExtra(Utils.KEY_NOTI_ID, notiId);
        PendingIntent confirmarPendingIntent = PendingIntent.getBroadcast(this, 5,
                confirmarIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CUENTA)
                .setContentTitle(getString(R.string.cuenta_title, modo, mesa))
                .setSmallIcon(R.drawable.img_mini_logo)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.cuenta_big_text, nombre, mesa, modo)))
                .setColor(getColor(R.color.notification))
                .setContentIntent(tapPendingIntent)
                .addAction(R.drawable.img_mini_logo, getString(R.string.confirmar), confirmarPendingIntent);

        manager.notify(notiId, builder.build());
    }

    /**
     * Tells admin who on what table has ordered the specified gift
     */
    private void processRegaloMessage(RemoteMessage message) {

        String nombre = message.getData().get(Utils.KEY_NOMBRE);
        String mesa = message.getData().get(Utils.KEY_MESA);
        String regalo = message.getData().get(Utils.KEY_REGALO);

        Intent tapIntent = GiftsActivity.newIntent(this, false, currentDate);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(this, 18,
                tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.PEDIDO)
                .setContentTitle(getString(R.string.regalo_title, mesa))
                .setSmallIcon(R.drawable.img_mini_logo)
                .setContentIntent(tapPendingIntent)
                .setColor(getColor(R.color.notification))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.regalo_message, regalo, nombre, mesa)));

        manager.notify(new Random().nextInt(), builder.build());

    }

    /**
     * Shows who wants to enter
     * When confirming, admin must assign a 'mesa' value to the user via a RemoteInput
     * For rejection admin just ignores the message
     */
    private void processPuertaMessage(RemoteMessage message) {

        int notiId = new Random().nextInt();

        String nombre = message.getData().get(Utils.KEY_NOMBRE);
        String userId = message.getData().get(Utils.KEY_USER_ID);
        String token = message.getData().get(Utils.KEY_TOKEN);

        // build a RemoteInput object to add it later to an action
        RemoteInput mesaInput = new RemoteInput.Builder(Utils.KEY_MESA)
                .setLabel(getString(R.string.asignar_mesa))
                .setAllowFreeFormInput(true)
                .build();

        Intent assignMesaIntent = new Intent(this, AssignMesaReceiver.class);
        assignMesaIntent.putExtra(Utils.KEY_USER_ID, userId);
        assignMesaIntent.putExtra(Utils.KEY_TOKEN, token);
        assignMesaIntent.putExtra(Utils.KEY_NOMBRE, nombre);
        assignMesaIntent.putExtra(Utils.KEY_NOTI_ID, notiId);
        PendingIntent assignMesaPendingIntent = PendingIntent.getBroadcast(this, 89, assignMesaIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // build an action with the pending intent and the remote input
        NotificationCompat.Action assignMesaAction = new NotificationCompat.Action.Builder(R.drawable.img_mini_logo, getString(R.string.aceptar), assignMesaPendingIntent)
                .addRemoteInput(mesaInput)
                .build();


        // build the notification
        Notification notiPuerta = new NotificationCompat.Builder(this, App.PUERTA)
                .setContentTitle(getString(R.string.puerta_title))
                .setContentText(getString(R.string.puerta_message, nombre))
                .setColor(getColor(R.color.notification))
                .setSmallIcon(R.drawable.img_mini_logo)
                .addAction(assignMesaAction)
                .build();

        manager.notify(notiId, notiPuerta);
    }

    /**
     * Uses a custom layout to display the data of the reservation in the expanded view
     * Two possible actions: accept and reject the request
     * When rejecting admin must enter a comment via a RemoteInput
     */
    private void processReservaMessage(RemoteMessage message) {

        int notiId = new Random().nextInt();

        // define remote views to pass as custom layouts
        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.noti_view_reserva_collapsed);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.noti_view_reserva_expanded);

        // get the values from the message and set them to the textViews of the expandedView
        // plus the sender's token to send them a message back, and the partOfDay value for database manipulations
        Map<String, String> reserva = message.getData();
        String fecha = reserva.get(Utils.KEY_FECHA);
        String nombre = reserva.get(Utils.KEY_NOMBRE);
        String hora = reserva.get(Utils.KEY_HORA);
        String pax = reserva.get(Utils.KEY_PAX);
        String mesa = reserva.get(Utils.KEY_MESA);
        String comentario = reserva.get(Utils.KEY_COMENTARIO);
        String parte = reserva.get(Utils.KEY_PARTE);
        String token = reserva.get(Utils.KEY_TOKEN);

        expandedView.setTextViewText(R.id.txtNotiFecha, fecha);
        expandedView.setTextViewText(R.id.txtNotiNombre, nombre);
        expandedView.setTextViewText(R.id.txtNotiHora, hora);
        expandedView.setTextViewText(R.id.txtNotiPax, pax);
        expandedView.setTextViewText(R.id.txtNotiMesa, mesa);
        if (TextUtils.equals(comentario, "")) {
            expandedView.setTextViewText(R.id.txtNotiComentario, getString(R.string.no_comment));
        } else {
            expandedView.setTextViewText(R.id.txtNotiComentario, comentario);
        }

        // on tap sends admin to see the set of reservations for the day and the part of day
        Intent tapIntent = ReservasDelDiaActivity.newIntent(this, fecha, parte);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(this, 1, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent acceptIntent = new Intent(this, ReservaAceptador.class);
        acceptIntent.putExtra(Utils.KEY_NOTI_ID, notiId);
        acceptIntent.putExtra(Utils.KEY_FECHA, fecha);
        acceptIntent.putExtra(Utils.KEY_PARTE, parte);
        acceptIntent.putExtra(Utils.KEY_MESA, mesa);
        acceptIntent.putExtra(Utils.KEY_TOKEN, token);
        acceptIntent.putExtra(Utils.KEY_HORA, hora);
        acceptIntent.putExtra(Utils.KEY_PAX, pax);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, 2, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rejectIntent = new Intent(this, ReservaRechazador.class);
        rejectIntent.putExtra(Utils.KEY_NOTI_ID, notiId);
        rejectIntent.putExtra(Utils.KEY_FECHA, fecha);
        rejectIntent.putExtra(Utils.KEY_HORA, hora);
        rejectIntent.putExtra(Utils.KEY_TOKEN, token);
        rejectIntent.putExtra(Utils.KEY_MESA, mesa);
        rejectIntent.putExtra(Utils.KEY_PARTE, parte);
        PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(this, 3, rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // create a RemoteInput and an Action for the rejection
        androidx.core.app.RemoteInput rejectionInput = new RemoteInput.Builder(Utils.KEY_COMENTARIO)
                .setLabel(Utils.KEY_COMENTARIO)
                .build();
        NotificationCompat.Action rejectionAction = new NotificationCompat.Action.Builder(R.drawable.img_mini_logo,
                getString(R.string.rechazar), rejectPendingIntent)
                .addRemoteInput(rejectionInput)
                .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.RESERVA)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(tapPendingIntent)
                .setAutoCancel(true)
                .setColor(getColor(R.color.notification))
                .setSmallIcon(R.drawable.img_mini_logo)
                .addAction(R.drawable.img_mini_logo, getString(R.string.aceptar), acceptPendingIntent)
                .addAction(rejectionAction);

        manager.notify(notiId, builder.build());
    }

    /**
     * Tells admin that a new order has been submitted, and who on what table did it
     * On 'Ver' action button admin can review the contents of the order in a separate activity
     */
    private void processPedidoMessage(RemoteMessage message) {

        int notiId = new Random().nextInt();

        String token = message.getData().get(Utils.KEY_TOKEN);
        String nombre = message.getData().get(Utils.KEY_NOMBRE);
        String mesa = message.getData().get(Utils.KEY_MESA);
        String fecha = message.getData().get(Utils.KEY_FECHA);
        String metaDocId = message.getData().get(Utils.KEY_META_ID);

        // on tap admin goes to the list of unserved orders
        Intent tapIntent = PedidosTodoActivity.newIntent(this, Utils.TODO);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(this, 22,
                tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // send a confirmation message
        Intent confirmarIntent = new Intent(this, PedidoConfirmator.class);
        confirmarIntent.putExtra(Utils.KEY_TOKEN, token);
        confirmarIntent.putExtra(Utils.KEY_NOTI_ID, notiId);
        PendingIntent confirmarPendingIntent = PendingIntent.getBroadcast(this, 4, confirmarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // check the content of the order
        Intent verIntent = PedidoDisplayerActivity.newIntent(this, metaDocId, fecha, token, notiId, nombre, mesa);
        PendingIntent verPendingIntent = PendingIntent.getActivity(this, 11, verIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.PEDIDO);
        builder.setContentTitle(getString(R.string.nuevo_pedido, mesa))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.pedido_message, nombre, mesa)))
                .setSmallIcon(R.drawable.img_mini_logo)
                .setColor(getColor(R.color.notification))
                .setContentIntent(tapPendingIntent)
                .addAction(R.drawable.img_mini_logo, getString(R.string.ver), verPendingIntent);
        assert token != null;

        // we set the 'confirmar' action button, only if the order was sent by a user of the app
        // if this is an order created by admin, the token value will be 'cliente'
        if (!token.equals("cliente")) {
            builder.addAction(R.drawable.img_mini_logo, getString(R.string.confirmar), confirmarPendingIntent);
        }

        manager.notify(notiId, builder.build());
    }
}
