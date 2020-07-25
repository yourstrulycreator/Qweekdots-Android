package com.creator.qweekdots.models;

import java.io.Serializable;

public class ChatRoom implements Serializable {
    String id, name, type, private_to, private_from, lastMessage, timestamp, private_avatar;
    int unreadCount;

    public ChatRoom() {
    }

    public ChatRoom(String id, String name, String type, String private_from, String private_to, String lastMessage, String timestamp, int unreadCount, String private_avatar) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.private_from = private_from;
        this.private_to = private_to;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.private_avatar = private_avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrivate_to() {
        return private_to;
    }

    public void setPrivate_to(String private_to) {
        this.private_to = private_to;
    }

    public String getPrivate_from() {
        return private_from;
    }

    public void setPrivate_from(String private_from) {
        this.private_from = private_from;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPrivate_avatar() {
        return private_avatar;
    }

    public void setPrivate_avatar(String private_avatar) {
        this.private_avatar = private_avatar;
    }
}