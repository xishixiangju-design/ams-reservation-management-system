package com.trae.ams.dto.auth;

import java.io.Serializable;
import java.util.List;

public class LoginResult implements Serializable {
    private Long id;
    private String username;
    private String nickname;
    private List<String> roles;
    private String token;

    public LoginResult() {
    }

    public LoginResult(Long id, String username, String nickname, List<String> roles, String token) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.roles = roles;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
