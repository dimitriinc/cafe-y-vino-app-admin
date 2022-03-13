package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.cafeyvinowinebar.Administrador.Fragments.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Only responsibility of the activity is to create a date value and pass it to the next activity
 * Displays an 'hoy' button so the date value is the current date
 * Also displays a date picker if the date is any other date but not the current one
 */
public class ReservasDatePickerActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservas_date_picker);

        ImageView imgCalendarConf = findViewById(R.id.imgCalendarConf);
        ImageView imgReservaHoy = findViewById(R.id.imgReservaHoy);
        String currentDate = Utils.getCurrentDate();

        imgCalendarConf.setOnClickListener(v -> new DatePicker().show(getSupportFragmentManager(), "DATE_PICKER"));

        imgReservaHoy.setOnClickListener(v -> {

            // we want to review the the reservations for the current date
            startActivity(ReservasDelDiaActivity.newIntent(getBaseContext(), currentDate, null));
        });
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

        // when the date is chosen via the date picker
        // we create a string according to the desired format
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat format = new SimpleDateFormat(Utils.DATE_FORMAT);
        String date = format.format(calendar.getTime());

        // and send the string with the intent
        startActivity(ReservasDelDiaActivity.newIntent(getBaseContext(), date, null));
    }
}