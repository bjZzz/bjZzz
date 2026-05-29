package com.nanda.ingestion.adapter;

import lombok.Data;

@Data
public class ConnectionTestResult {

    private boolean success;
    private String message;

    public static ConnectionTestResult ok(String message) {
        ConnectionTestResult r = new ConnectionTestResult();
        r.setSuccess(true);
        r.setMessage(message);
        return r;
    }

    public static ConnectionTestResult fail(String message) {
        ConnectionTestResult r = new ConnectionTestResult();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }
}
