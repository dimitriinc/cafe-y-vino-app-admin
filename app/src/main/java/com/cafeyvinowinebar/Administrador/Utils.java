package com.cafeyvinowinebar.Administrador;

import android.annotation.SuppressLint;
import android.nfc.cardemulation.HostNfcFService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class Utils {

    // constants for the messaging action keys
    public static final String ACTION_PUERTA = "puerta";
    public static final String ACTION_RESERVA = "reserva";
    public static final String ACTION_PEDIDO = "pedido";
    public static final String ACTION_CUENTA = "cuenta";
    public static final String ACTION_REGALO = "regalo";
    public static final String ACTION_PUERTA_ADMIN = "puerta_admin";
    public static final String ACTION_RESERVA_ACK = "reserva_ack";
    public static final String ACTION_RESERVA_NACK = "reserva_nack";
    public static final String ACTION_PEDIDO_ADMIN = "pedido_admin";
    public static final String ACTION_CUENTA_ACK = "cuenta_ack";
    public static final String ACTION_CUENTA_ADMIN = "cuenta_admin";
    public static final String ACTION_MSG = "msg";

    // keys for data transferring
    public static final String KEY_TOKEN = "token";
    public static final String KEY_COUNT = "count";
    public static final String KEY_FECHA = "fecha";
    public static final String KEY_NOTI_ID = "notiId";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_MESA = "mesa";
    public static final String KEY_NOMBRE = "nombre";
    public static final String KEY_REGALO = "regalo";
    public static final String KEY_ACTION = "action";
    public static final String KEY_MODO = "modo";
    public static final String KEY_HORA = "hora";
    public static final String KEY_PAX = "pax";
    public static final String KEY_COMENTARIO = "comentario";
    public static final String KEY_PARTE = "parte";
    public static final String KEY_CATEGORY = "category";
    public static final String TAG = "tag";
    public static final String KEY_DATE = "date";
    public static final String KEY_MODE = "mode";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_BONO = "bono";
    public static final String KEY_LLEGADO = "llegado";
    public static final String KEY_PRECIO = "precio";
    public static final String KEY_PAY_TYPE = "pay_type";
    public static final String KEY_META_ID = "metaDocId";
    public static final String MESA_ID = "mesaID";

    // values
    public static final String TO_CLIENT = "toClient";
    public static final String TO_ADMIN = "toAdmin";
    public static final String TODO = "todo";
    public static final String BARRA = "barra";
    public static final String COCINA = "cocina";
    public static final String IS_PRESENT = "isPresent";
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String GMT = "GMT-5";
    public static final String DIA = "dia";
    public static final String NOCHE = "noche";
    public static final String EFECTIVO = "efectivo";
    public static final String VISA = "visa";
    public static final String YAPE = "yape";
    public static final String CRIPTO = "cripto";
    public static final String DIVIDIDO = "dividido";

    // firestore root collections
    public static final String USUARIOS = "usuarios";
    public static final String RESERVAS_MODEL = "reservas model";
    public static final String ADMINS = "administradores";
    public static final String CUENTAS = "cuentas";
    public static final String PEDIDOS = "pedidos";
    public static final String RESERVAS = "reservas";

    // firestore keys
    public static final String TOTAL = "total";
    public static final String CUENTA = "cuenta";
    public static final String SERVIDO = "servido";
    public static final String SERVIDO_BARRA = "servidoBarra";
    public static final String SERVIDO_COCINA = "servidoCocina";
    public static final String KEY_IS_EXPANDED = "isExpanded";
    public static final String KEY_BONOS = "bonos";
    public static final String PRICE = "price";
    public static final String TELEFONO = "telefono";
    public static final String KEY_USER = "user";
    public static final String TIMESTAMP = "timestamp";
    public static final String KEY_CONFIRMADO = "confirmado";
    public static final String EDICION = "Edición";
    public static final String ELIMINACION = "Eliminación";

    public static final String[] FIXED_MESAS = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};



    public static String getMessageId() {
        return "m-" + new Random().nextLong();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(Utils.GMT));
        return sdf.format(new Date());
    }

    public static String getCurrentHour() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        String amPm;
        if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
            amPm = "AM";
        } else {
            amPm = "PM";
        }
        return calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + " " + amPm;
    }

}
