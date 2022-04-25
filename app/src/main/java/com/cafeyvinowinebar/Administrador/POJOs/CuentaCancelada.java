package com.cafeyvinowinebar.Administrador.POJOs;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

public class CuentaCancelada {
    String name, total, userId, hora, mesa, metaDocId, fecha;
    public boolean isExpanded;
    Timestamp timestamp;

    public CuentaCancelada() {
    }

    public CuentaCancelada(String name, String total, String userId, String hora, String mesa, String metaDocId, String fecha, boolean isExpanded, Timestamp timestamp) {
        this.name = name;
        this.total = total;
        this.userId = userId;
        this.hora = hora;
        this.mesa = mesa;
        this.metaDocId = metaDocId;
        this.fecha = fecha;
        this.isExpanded = isExpanded;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getMetaDocId() {
        return metaDocId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public void setMetaDocId(String metaDocId) {
        this.metaDocId = metaDocId;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
