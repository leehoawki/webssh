package com.kedacom.ctsp.webssh.model;


import org.springframework.web.multipart.MultipartFile;

public class HostLoginInfo {
    private String hostname;

    private Integer port;

    private String username;

    private String password;

    private MultipartFile privatekey;

    public String getHostname() {
        return hostname;
    }

    public HostLoginInfo setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public HostLoginInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public HostLoginInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public HostLoginInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public MultipartFile getPrivatekey() {
        return privatekey;
    }

    public HostLoginInfo setPrivatekey(MultipartFile privatekey) {
        this.privatekey = privatekey;
        return this;
    }

    @Override
    public String toString() {
        return "HostLoginInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", privatekey=" + privatekey +
                '}';
    }
}
