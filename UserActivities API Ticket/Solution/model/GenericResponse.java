package com.adobe.aem.capstone.core.models;

public class GenericResponse {
    private int statusCode;
    private String ressponse;
    private Boolean retry;

    public GenericResponse(int statusCode, String ressponse, Boolean retry) {
        this.statusCode = statusCode;
        this.ressponse = ressponse;
        this.retry = retry;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getRessponse() {
        return ressponse;
    }

    public void setRessponse(String ressponse) {
        this.ressponse = ressponse;
    }

    public Boolean getRetry() {
        return retry;
    }

    public void setRetry(Boolean retry) {
        this.retry = retry;
    }
}
