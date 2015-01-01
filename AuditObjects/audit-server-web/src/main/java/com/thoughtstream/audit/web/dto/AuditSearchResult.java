package com.thoughtstream.audit.web.dto;

import java.util.Date;

/**
 * @author Sateesh
 * @since 01/01/2015
 */
public class AuditSearchResult {
    private String id;
    private String who;
    private Date when ;
    private String jsonString;

    public AuditSearchResult(String id, String who, Date when, String jsonString) {
        this.id = id;
        this.who = who;
        this.when = when;
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
}
