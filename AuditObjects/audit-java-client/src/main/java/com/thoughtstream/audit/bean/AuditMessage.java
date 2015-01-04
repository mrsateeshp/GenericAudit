package com.thoughtstream.audit.bean;

import java.util.Date;

/**
 * @author Sateesh
 * @since 04/01/2015
 */
public class AuditMessage {
    private String oldDataSnapshot;
    private String newDataSnapshot;
    private String who;
    private Date when;
    private String operationType;

    public AuditMessage(String newDataSnapshot, String oldDataSnapshot, String who, Date when, String operationType) {
        if(oldDataSnapshot == null && newDataSnapshot == null){
            throw new IllegalArgumentException("At least one data snapshot should not be null.");
        }

        this.oldDataSnapshot = oldDataSnapshot;
        this.newDataSnapshot = newDataSnapshot;
        this.who = who;
        this.when = when;
        this.operationType = operationType;
    }

    public AuditMessage(String newDataSnapshot, String oldDataSnapshot) {
       this(newDataSnapshot, oldDataSnapshot, null, null, null);
    }

    public AuditMessage(String newDataSnapshot) {
       this(newDataSnapshot, null);
    }

    public String getOldDataSnapshot() {
        return oldDataSnapshot;
    }

    public String getNewDataSnapshot() {
        return newDataSnapshot;
    }

    public String getWho() {
        return who;
    }

    public Date getWhen() {
        return when;
    }

    public String getOperationType() {
        return operationType;
    }
}
