package com.betfair.aping.containers;

/**
 * @author Sateesh
 * @since 02/12/2014
 */
public class SSOTokenContainer {

    public String token;
    public String status;
    public String error;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}