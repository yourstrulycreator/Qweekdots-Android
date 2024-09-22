package com.creator.qweekdots.models;

import java.io.Serializable;

public class StreamUser implements Serializable {
    String id;

    public StreamUser() {
    }

    public StreamUser(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
