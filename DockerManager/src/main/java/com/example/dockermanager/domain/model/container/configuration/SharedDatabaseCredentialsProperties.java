package com.example.dockermanager.domain.model.container.configuration;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SharedDatabaseCredentialsProperties {
    private String username;
    private String password;
    private String name;
}
