package com.trevorbye.POJO;

import java.security.Principal;

public class PrincipalHalWrapper extends HALResource {
    private Principal principal;

    public PrincipalHalWrapper() {
    }

    public PrincipalHalWrapper(Principal principal) {
        this.principal = principal;
    }

    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public String toString() {
        return "PrincipalHalWrapper{" +
                "principal=" + principal +
                '}';
    }
}
