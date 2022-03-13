package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cafeyvinowinebar.Administrador.Fragments.ReservasDay;
import com.cafeyvinowinebar.Administrador.Fragments.ReservasNight;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays fragments with the set of reservations for the chosen date
 * Has one container for two fragments
 * Chooses which fragment to show based on the 'parte' value received with the intent
 * If the intent doesn't have a 'parte' value in the intent, shows the day fragment by default
 * To switch the fragments in the container, admin can press two image views above the container
 * Which triggers the changeFragment()
 */
public class ReservasDelDiaActivity extends AppCompatActivity {

    private Fragment fragment;
    String date, parte;

    public static Intent newIntent(Context context, String date, String parte) {
        Intent i = new Intent(context, ReservasDelDiaActivity.class);
        i.putExtra(Utils.KEY_DATE, date);
        i.putExtra(Utils.KEY_PARTE, parte);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservas_del_dia);

        FloatingActionButton fabSolicitudesHome = findViewById(R.id.fabSolicitudesHome);
        fabSolicitudesHome.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        date = getIntent().getStringExtra(Utils.KEY_DATE);

        // when the activity is started from the messaging service
        // the intent arrives with the part of day value to show which of the fragments to display first
        parte = getIntent().getStringExtra(Utils.KEY_PARTE);
        if (parte != null) {
            if (parte.equals(Utils.NOCHE)) {
                setupNightFragment();
            } else {
                setupDayFragment();
            }
        } else {
            setupDayFragment();
        }
    }


    public void changeFragment(View view) {

        if (view == findViewById(R.id.imgDay)) {
            setupDayFragment();
        }
        if (view == findViewById(R.id.imgNight)) {
            setupNightFragment();
        }
    }

    private void setupDayFragment() {
        fragment = ReservasDay.newInstance(date);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setReorderingAllowed(true);
        ft.replace(R.id.fragment_container, fragment, null);
        ft.commit();
    }

    private void setupNightFragment() {
        fragment = ReservasNight.newInstance(date);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setReorderingAllowed(true);
        ft.replace(R.id.fragment_container, fragment, null);
        ft.commit();
    }
}