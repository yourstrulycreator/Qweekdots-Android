package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationItem {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("notification_id")
    @Expose
    private String notification_id;
    @SerializedName("drop_id")
    @Expose
    private String drop_id;
    @SerializedName("note_type")
    @Expose
    private String note_type;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("string")
    @Expose
    private String string;
    @SerializedName("drop_type")
    @Expose
    private String drop_type;
    @SerializedName("drop")
    @Expose
    private String drop;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("hasMedia")
    @Expose
    private Integer hasMedia;
    @SerializedName("hasLink")
    @Expose
    private Integer hasLink;
    @SerializedName("qweeksnap")
    @Expose
    private String qweeksnap;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("audio")
    @Expose
    private String audio;

    /**
     *
     * @return
     * The Initial Id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * The Initial Id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The Notification ID
     */
    public String getNotificationID() {
        return notification_id;
    }

    /**
     *
     * The Initial Id
     */
    public void setNotificationID(String notification_id) {
        this.notification_id = notification_id;
    }

    /**
     *
     * @return
     * The Drop Id
     */
    public String getDropID() {
        return drop_id;
    }

    /**
     *
     * The Drop Id
     */
    public void setDropID(String drop_id) {
        this.drop_id = drop_id;
    }

    /**
     *
     * @return
     * The Notification Type
     */
    public String getNotificationType() {
        return note_type;
    }

    /**
     *
     * The Initial Id
     */
    public void setNotificationType(String note_type) {
        this.note_type = note_type;
    }

    /**
     *
     * @return
     * The Username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * The Initial Id
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The Status/What they did
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * The Status/What they did
     */
    public void setStatus(String status) {
        this.status = status;
    }


    /**
     *
     * @return
     * The String/Actual comment, message(if any)
     */
    public String getString() {
        return string;
    }

    /**
     *
     * The String/Actual comment, message(if any)
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     *
     * @return
     * The Drop Type(when involved)
     */
    public String getDropType() {
        return drop_type;
    }

    /**
     *
     * The Drop Type
     */
    public void setDropType(String drop_type) {
        this.drop_type = drop_type;
    }

    /**
     *
     * @return
     * The Drop(when involved)
     */
    public String getDrop() {
        return drop;
    }

    /**
     *
     * The Drop(when involved)
     */
    public void setDrop(String drop) {
        this.drop = drop;
    }

    /**
     *
     * @return
     * The ProfileModel Picture
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     *
     * The Initial Id
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     *
     * @return
     * The hasMedia
     */
    public Integer getHasMedia() {
        return hasMedia;
    }

    /**
     *
     * The hasMedia
     */
    public void setHasMedia(Integer hasMedia) {
        this.hasMedia = hasMedia;
    }

    /**
     *
     * @return
     * The hasLink
     */
    public Integer getHasLink() {
        return hasLink;
    }

    /**
     *
     * The hasLink
     */
    public void setHasLink(Integer hasLink) {
        this.hasLink = hasLink;
    }

    /**
     *
     * @return
     * The QweekSnap
     */
    public String getQweekSnap() {
        return qweeksnap;
    }

    /**
     *
     * @param qweeksnap
     * The QweekSnap
     */
    public void setQweekSnap(String qweeksnap) {
        this.qweeksnap = qweeksnap;
    }

    /**
     *
     * @return
     * The Url/Link
     */
    public String getLink() {
        return url;
    }

    /**
     *
     * The Url/Link
     */
    public void setLink(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The Audio
     */
    public String getAudio() {
        return audio;
    }

    /**
     *
     * @param audio
     *Audio
     */
    public void setAudio(String audio) {
        this.audio = audio;
    }
}
