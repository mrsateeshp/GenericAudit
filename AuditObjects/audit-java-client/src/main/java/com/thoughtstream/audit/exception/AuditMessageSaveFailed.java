package com.thoughtstream.audit.exception;

/**
 * @author Sateesh
 * @since 04/01/2015
 */
public class AuditMessageSaveFailed extends Exception {
    public AuditMessageSaveFailed(String message) {
        super(message);
    }

    public AuditMessageSaveFailed(Throwable cause) {
        super(cause);
    }
}
