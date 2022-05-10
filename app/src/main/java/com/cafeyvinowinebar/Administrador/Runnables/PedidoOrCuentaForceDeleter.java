package com.cafeyvinowinebar.Administrador.Runnables;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.CountDownLatch;

public class PedidoOrCuentaForceDeleter implements Runnable {

    DocumentSnapshot metaDoc;
    String comment, mode;

    public PedidoOrCuentaForceDeleter(DocumentSnapshot metaDoc, String comment, String mode) {
        this.metaDoc = metaDoc;
        this.comment = comment;
        this.mode = mode;
    }

    @Override
    public void run() {

        CountDownLatch latch = new CountDownLatch(1);
        if (mode.equals(Utils.CUENTA)) {
            App.executor.submit(new EliminationApplier(
                    metaDoc.getString(Utils.KEY_NAME),
                    metaDoc.getString(Utils.KEY_MESA),
                    comment,
                    metaDoc.getReference().getPath() + "/cuenta",
                    latch));
        } else if (mode.equals(Utils.PEDIDO)) {
            App.executor.submit(new EliminationApplier(
                    metaDoc.getString(Utils.KEY_USER),
                    metaDoc.getString(Utils.KEY_MESA),
                    comment,
                    metaDoc.getReference().getPath() + "/pedido",
                    latch));
        }

        // before deleting the meta doc, we must delete the products in the cuenta, which happens in the EliminationApplier
        // so until it's done with its operations, we latch the current thread
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        metaDoc.getReference().delete().addOnSuccessListener(App.executor, unused -> {

            // if the client doesn't have any pedidos assigned to them, we want to close the session and unblock the table
            // we do it after the deletion is done, so the runnable doesn't think the client still has a cuenta
            App.executor.submit(new UniquePedidoDeleter(metaDoc));
        });
    }
}
