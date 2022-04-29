package com.cafeyvinowinebar.Administrador.Runnables;

import android.os.Handler;

import com.cafeyvinowinebar.Administrador.Fragments.IngresoTotal;

import androidx.fragment.app.FragmentManager;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

/**
 * Computes the total amount of money spent by the customer
 * Also computes how much was spent by each payment mode
 * Stores all the computed values in separate member vars and passes those to a dialog fragment to display
 */
public class IngresoTotalDisplayer implements Runnable {

    private final Query query;
    private final Handler mainHandler;
    public double total, totalEfectivo, totalVisa, totalYape, totalCripto;
    private final FragmentManager manager;

    public IngresoTotalDisplayer(Query query, Handler mainHandler,
                                 FragmentManager manager) {
        this.query = query;
        this.mainHandler = mainHandler;
        this.manager = manager;
    }

    @Override
    public void run() {

        // the query we get is a collection group of all the bills of the user
        query.get().addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                // for each bill we retrieve the total cost
                final double monto = Double.parseDouble(Objects.requireNonNull(snapshot.getString(Utils.TOTAL)));
                // and increment the 'total' var by this value
                total += monto;

                // get the propina value
                Long propina = snapshot.getLong(Utils.PROPINA);

                // then we check how the customer paid for the bill
                // and depending on the payment mode, we increment the corresponding var
                // if there were tips left by other means than cash, we add the amount to the corresponding pay type
                // and subtract the tip from cash
                switch (Objects.requireNonNull(snapshot.getString(Utils.KEY_PAY_TYPE))) {

                    case Utils.EFECTIVO:
                        totalEfectivo += monto;
                        break;
                    case Utils.VISA:
                        totalVisa += monto;
                        if (propina != null) {
                            totalVisa += propina;
                            totalEfectivo -= propina;
                        }
                        break;
                    case Utils.YAPE:
                        totalYape += monto;
                        if (propina != null) {
                            totalYape += propina;
                            totalEfectivo -= propina;
                        }
                        break;
                    case Utils.CRIPTO:
                        totalCripto += monto;
                        if (propina != null) {
                            totalCripto += propina;
                            totalEfectivo -= propina;
                        }
                        break;

                    // if the bill was divided by different payment modes, we iterate through the possible modes
                    // and when we encounter a value, which is not null, we increment the corresponding var
                    // if there is propina, we add it to the visa amount
                    case Utils.DIVIDIDO:
                        Double montoEfectivo = snapshot.getDouble(Utils.EFECTIVO);
                        if (montoEfectivo != null) {
                            totalEfectivo += montoEfectivo;
                        }
                        Double montoVisa = snapshot.getDouble(Utils.VISA);
                        if (montoVisa != null) {
                            totalVisa += montoVisa;
                        }
                        Double montoYape = snapshot.getDouble(Utils.YAPE);
                        if (montoYape != null) {
                            totalYape += montoYape;
                        }
                        Double montoCripto = snapshot.getDouble(Utils.CRIPTO);
                        if (montoCripto != null) {
                            totalCripto += montoCripto;
                        }
                        if (propina != null) {
                            totalVisa += propina;
                            totalEfectivo -= propina;
                        }

                    default:
                        break;
                }
            }

            // once all the member vars are computed, we pass them to display
            mainHandler.post(() -> {
                IngresoTotal fragment = IngresoTotal.newInstance(total, totalEfectivo, totalVisa, totalYape, totalCripto);
                fragment.show(manager, Utils.TAG);
            });
        });
    }
}
