package com.cytomine.registry.client.http.auth;

public enum Scope {
    NONE(""),
    PULL("pull"),
    PULL_PUSH("pull,push"),
    DELETE("delete"),;

    private String scope;

    Scope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}
