package com.creator.qweekdots.models;

import java.io.Serializable;

public class Space implements Serializable {
    String id, space_name, space_galaxy, space_art, followed_count, pinned;

    public Space() {
    }

    public Space(String id, String space_name, String space_galaxy, String space_art, String followed_count, String pinned) {
        this.id = id;
        this.space_name = space_name;
        this.space_art = space_art;
        this.space_galaxy = space_galaxy;
        this.followed_count = followed_count;
        this.pinned = pinned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpace_name() {
        return space_name;
    }

    public void setSpace_name(String space_name) {
        this.space_name = space_name;
    }

    public String getSpace_galaxy() {
        return space_galaxy;
    }

    public void setSpace_galaxy(String space_galaxy) {
        this.space_galaxy = space_galaxy;
    }

    public String getSpace_art() {
        return space_art;
    }

    public void setSpace_art(String space_art) {
        this.space_art = space_art;
    }

    public String getFollowed_count() {
        return followed_count;
    }

    public void setFollowed_count(String followed_count) {
        this.followed_count = followed_count;
    }

    public String getPinned() {
        return pinned;
    }

    public void setPinned(String pinned) {
        this.pinned = pinned;
    }
}
