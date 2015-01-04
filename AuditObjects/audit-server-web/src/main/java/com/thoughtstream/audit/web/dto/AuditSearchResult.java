package com.thoughtstream.audit.web.dto;

import com.thoughtstream.audit.service.AuditMetaData;

import java.util.Date;

/**
 * @author Sateesh
 * @since 01/01/2015
 */
public class AuditSearchResult {
    private String id;
    private String who;
    private Date when ;
    private String operationType;
    private String jsonString;

    public AuditSearchResult(String id, AuditMetaData auditMetaData, String jsonString) {
        this.id = id;
        this.who = auditMetaData.who();
        this.when = auditMetaData.when();
        this.operationType = auditMetaData.operationType();
        this.jsonString = jsonString;
    }

    public String getId() {
        return id;
    }

    public String getWho() {
        return who;
    }

    public Date getWhen() {
        return when;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getOperationType() {
        return operationType;
    }
}
