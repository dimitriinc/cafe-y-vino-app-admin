package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.POJOs.RedactEntity;
import com.cafeyvinowinebar.Administrador.POJOs.Redaction;
import com.cafeyvinowinebar.Administrador.RedactViewModel;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Applies changes made to the pedido or cuenta collections
 * Stores the changes into the Firestore
 */
public class RedactionApplier implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final String comment, userName, mesa, mode;
    private final RedactViewModel viewModel;
    private final List<DocumentSnapshot> inputCollection;

    public RedactionApplier(String comment, String userName, String mesa, RedactViewModel viewModel, List<DocumentSnapshot> inputCollection,
                            String mode) {
        this.comment = comment;
        this.userName = userName;
        this.mesa = mesa;
        this.viewModel = viewModel;
        this.inputCollection = inputCollection;
        this.mode = mode;
    }

    @Override
    public void run() {

        // first lets get the current time
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        String amPm;
        if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
            amPm = "AM";
        } else {
            amPm = "PM";
        }
        String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + " " + amPm;


        // we prepare a map, were we will store all the changes
        // the map connects the name of the product with the numerical value of the change in its count
        Map<String, String> changes = new HashMap<>();

        // retrieve the list of entities from the table, this is our output collection
        List<RedactEntity> outputCollection = viewModel.getAllProducts().getValue();
        assert outputCollection != null;

        // as soon as we retrieve the output collection we can empty the SQLite table
        viewModel.deleteAll();

        // iterate through the output collection
        for (RedactEntity product : outputCollection) {

            // for each output product we shall find the input product of the same name
            // to do it we iterate through the input collection
            for (DocumentSnapshot snapshot : inputCollection) {

                if (Objects.equals(snapshot.getString(Utils.KEY_NAME), product.getName())) {

                    // we've found the matching product
                    // now we check if any changes were made to it's count value
                    long difference = snapshot.getLong(Utils.KEY_COUNT) - product.getCount();

                    // if the difference is 0, no changes were made, so we can leave the product alone
                    if (difference != 0) {

                        // if the difference is not 0, we can start to work with the change
                        // first, if the output count is 0, we delete the product from the pedido / cuenta
                        if (product.getCount() == 0) {
                            snapshot.getReference().delete();
                        } else {

                            // at this point we need to differentiate between pedidos and cuentas
                            // if the input collection is a cuenta, we update not only the count value, but also the total value

                            if (mode.equals(Utils.PEDIDOS)) {
                                // update the count value
                                snapshot.getReference().update(Utils.KEY_COUNT, product.getCount());
                            } else {
                                // update the count value and the total value in one batch (just for the sake of it)
                                WriteBatch batch = fStore.batch();
                                batch.update(snapshot.getReference(), Utils.KEY_COUNT, product.getCount());
                                batch.update(snapshot.getReference(), Utils.TOTAL, snapshot.getLong(Utils.PRICE) * product.getCount());
                                batch.commit();
                            }
                        }


                        // to display the redaction correctly, we change the sign of the difference
                        difference = difference * -1;

                        // also if the difference is positive, we want to display it with a plus sign
                        String differenceToStore;
                        if (difference > 0) {
                            differenceToStore = "+" + difference;
                        } else {
                            differenceToStore = String.valueOf(difference);
                        }
                        // put the change inside the map
                        changes.put(product.getName(), differenceToStore);
                    }
                }
            }
        }

        // after the iteration, the map of changes is populated (if there are any changes)
        // we can store the redaction object, empty the SQLite table, and leave the fragment
        if (!changes.isEmpty()) {
            fStore.collection("cambios").document(Utils.getCurrentDate()).collection("cambios")
                    .add(new Redaction(changes, comment, userName, mesa, new Timestamp(new Date()), time, Utils.EDICION));
        }
    }
}
