package com.creator.qweekdots.models;

import java.io.Serializable;

public class Message implements Serializable {
    private String id, message, liked, qweeksnap, mediaType, link, createdAt;
    private Integer hasMedia, hasLink;
    private User user;

    public Message() {
    }

    public Message(String id, String message, String liked, String qweeksnap, String mediaType, Integer hasMedia, Integer hasLink, String link, String createdAt, User user) {
        this.id = id;
        this.message = message;
        this.liked = liked;
        this.qweeksnap = qweeksnap;
        this.mediaType = mediaType;
        this.hasMedia = hasMedia;
        this.hasLink = hasLink;
        this.link = link;
        this.createdAt = createdAt;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }

    public String getQweeksnap() { return qweeksnap; }

    public void setQweeksnap(String qweeksnap) { this.qweeksnap = qweeksnap; }

    public String getMediaType() { return mediaType; }

    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getLink() { return link; }

    public void setLink(String link) { this.link =link; }

    public Integer getHasMedia() { return hasMedia; }

    public void setHasMedia(Integer hasMedia) { this.hasMedia = hasMedia; }

    public Integer getHasLink() { return hasLink; }

    public void setHasLink(Integer hasLink) { this.hasLink = hasLink; }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
