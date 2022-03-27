package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Checks if the cuenta meta doc is already populated
 * If it's not, or if the doc doesn't exist; populates/creates it
 */
public class CuentaMetaDocSetter implements Runnable {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    private final String userName, userMesa, userId, currentDate;
    private final Long mesaId;

    public CuentaMetaDocSetter(String userName, String userMesa, String userId, String currentDate, Long mesaId) {
        this.userName = userName;
        this.userMesa = userMesa;
        this.userId = userId;
        this.currentDate = currentDate;
        this.mesaId = mesaId;
    }

    @Override
    public void run() {

        // get the cuenta meta doc reference
        DocumentReference metaDoc = fStore.collection("cuentas")
                .document(currentDate)
                .collection("cuentas corrientes")
                .document(userId);

        metaDoc.get().addOnSuccessListener(App.executor, snapshot -> {

            if ((snapshot.exists() && !snapshot.contains(Utils.KEY_NAME)) || (!snapshot.exists())) {

                // populates the meta doc with meta data
                Map<String, Object> cuentaData = new HashMap<>();

                // mesaId will be null, if the pedido was created by a user of the client app
                if (mesaId != null) {
                    cuentaData.put(Utils.MESA_ID, mesaId);
                }
                cuentaData.put(Utils.KEY_NAME, userName);
                cuentaData.put(Utils.KEY_MESA, userMesa);
                cuentaData.put(Utils.KEY_IS_EXPANDED, false);
                metaDoc.set(cuentaData);
            }
        });
    }
}
