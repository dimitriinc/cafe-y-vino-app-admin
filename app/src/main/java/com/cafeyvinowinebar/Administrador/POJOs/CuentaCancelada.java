package com.cafeyvinowinebar.Administrador.POJOs;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

public class CuentaCancelada {
    String name, total, userId, fecha, metaDocId;
    public boolean isExpanded;
    Timestamp timestamp;

    public CuentaCancelada() {
    }

    public CuentaCancelada(String name, boolean isExpanded, String total, String userId, String fecha, Timestamp timestamp) {
        this.name = name;
        this.isExpanded = isExpanded;
        this.total = total;
        this.userId = userId;
        this.fecha = fecha;
        this.timestamp = timestamp;
    }


    public String getName() {
        return name;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getTotal() {
        return total;
    }

    public String getUserId() {
        return userId;
    }

    public String getFecha() {
        return fecha;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getMetaDocId() {
        return metaDocId;
    }
}
