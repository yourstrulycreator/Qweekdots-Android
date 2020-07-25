package com.creator.qweekdots.models;

import java.io.Serializable;

public class User implements Serializable {
    String id, username, fullname, email, avatar;

    public User() {
    }

    public User(String id, String username, String fullname, String email, String avatar) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullname;
    }

    public void setFullName(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
