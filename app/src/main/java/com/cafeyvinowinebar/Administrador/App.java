package com.cafeyvinowinebar.Administrador;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    public static final String PUERTA = "channelPuerta";
    public static final String RESERVA = "channelReserva";
    public static final String PEDIDO = "channelPedido";
    public static final String CUENTA = "channelCuenta";
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_CORES * 2);
    public static final String SENDER_ID = "check the firebase account";

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel channelPuerta = new NotificationChannel(
                PUERTA,
                "Puerta",
                NotificationManager.IMPORTANCE_HIGH
        );
        channelPuerta.setDescription("Canal para recibir las peticiones de entrada");
        NotificationChannel channelReserva = new NotificationChannel(
                RESERVA,
                "Reservas",
                NotificationManager.IMPORTANCE_HIGH
        );
        channelReserva.setDescription("Canal para recibir las solicitudes de reservas");
        NotificationChannel channelPedido = new NotificationChannel(
                PEDIDO,
                "Pedidos",
                NotificationManager.IMPORTANCE_HIGH
        );
        channelPedido.setDescription("Canal para recibir los pedidos");
        NotificationChannel channelCuenta = new NotificationChannel(
                CUENTA,
                "Cuentas",
                NotificationManager.IMPORTANCE_HIGH
        );
        channelCuenta.setDescription("Canal para recibir las peticiones de cuentas / avisos de cancelación");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channelPuerta);
        manager.createNotificationChannel(channelReserva);
        manager.createNotificationChannel(channelPedido);
        manager.createNotificationChannel(channelCuenta);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        executor.shutdown();
    }
}
