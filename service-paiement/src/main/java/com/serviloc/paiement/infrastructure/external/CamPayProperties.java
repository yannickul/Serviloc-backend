package com.serviloc.paiement.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campay")
public class CamPayProperties {

    private String username;
    private String password;
    private String env;
    private String baseUrl;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEnv()      { return env; }
    public void setEnv(String env) { this.env = env; }
    public String getBaseUrl()  { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}