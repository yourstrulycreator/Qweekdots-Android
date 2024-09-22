package com.creator.qweekdots.models;

import java.io.Serializable;

public class ChatRoom implements Serializable {
    private String id, name, type, private_to, private_from, lastMessage, lastMessageTo, lastMessageFrom, timestamp, private_avatar, space_art;
    private int unreadCount;

    public ChatRoom() {
    }

    public ChatRoom(String id, String name, String type, String private_from, String private_to, String lastMessage, String lastMessageTo, String lastMessageFrom, String timestamp, int unreadCount, String private_avatar, String space_art) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.private_from = private_from;
        this.private_to = private_to;
        this.lastMessage = lastMessage;
        this.lastMessageTo = lastMessageTo;
        this.lastMessageFrom = lastMessageFrom;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.private_avatar = private_avatar;
        this.space_art = space_art;
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

    public String getLastMessageTo() {
        return lastMessageTo;
    }

    public void setLastMessageTo(String lastMessageTo) {
        this.lastMessageTo = lastMessageTo;
    }

    public String getLastMessageFrom() {
        return lastMessageFrom;
    }

    public void setLastMessageFrom(String lastMessageFrom) {
        this.lastMessageFrom = lastMessageFrom;
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

    public void setSpace_art(String space_art) {
        this.space_art = space_art;
    }

    public String getSpace_art() {
        return space_art;
    }
}