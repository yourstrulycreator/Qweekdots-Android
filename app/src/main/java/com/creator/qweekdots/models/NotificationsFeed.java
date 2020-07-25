package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFeed {
    @SerializedName("notifications")
    @Expose
    private List<NotificationItem> notifications = new ArrayList<NotificationItem>();
    @SerializedName("cursor")
    @Expose
    private List<Cursor> cursor;

    /**
     *
     * @return
     * The notifications
     */
    public List<NotificationItem> getNotificationItem() {
        return notifications;
    }


    /**
     *
     * @param notifications
     * The results
     */
    public void setNotificationItems(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    /**
     *
     * @return
     * The cursors
     */
    public List<Cursor> getCursorLinks() {
        return cursor;
    }


    /**
     *
     * @param cursor
     * The results
     */
    public void setCursorlinks(List<Cursor> cursor) {
        this.cursor = cursor;
    }
}
