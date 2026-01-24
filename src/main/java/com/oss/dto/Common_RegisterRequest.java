package com.oss.dto;

import lombok.Data;

@Data
public class Common_RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
}

