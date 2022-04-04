package com.cafeyvinowinebar.Administrador.POJOs;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Redaction {

    Map<String, String> changes;
    String comment;
    String userName, mesa;
    Timestamp timestamp;

    public Redaction(Map<String, String> changes, String comment, String userName, String mesa, Timestamp timestamp) {
        this.changes = changes;
        this.comment = comment;
        this.userName = userName;
        this.mesa = mesa;
        this.timestamp = timestamp;
    }

    public Redaction() {}

    public Map<String, String> getChanges() {
        return changes;
    }

    public void setChanges(Map<String, String> changes) {
        this.changes = changes;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
