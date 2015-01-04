package com.thoughtstream.audit.web.dto;

/**
 * @author Sateesh
 * @since 04/01/2015
 */
public class AuditSaveResponse {

    private boolean success;
    private String error;

    public AuditSaveResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}
