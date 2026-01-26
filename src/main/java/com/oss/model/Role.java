package com.oss.model;
public enum Role {
    ADMIN,
    MANAGER,
    STAFF,
    CUSTOMER,
    // Legacy support - map SUPERADMIN to ADMIN
    @Deprecated
    SUPERADMIN
}