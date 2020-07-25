package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideoItem {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("drop_id")
    @Expose
    private String drop_id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("fullname")
    @Expose
    private String fullname;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("drop")
    @Expose
    private String drop;
    @SerializedName("hasMedia")
    @Expose
    private Integer hasMedia;
    @SerializedName("hasLink")
    @Expose
    private Integer hasLink;
    @SerializedName("qweeksnap")
    @Expose
    private String qweeksnap;
    @SerializedName("qweeksnap_thumbnail")
    @Expose
    private String qweeksnap_thumbnail;
    @SerializedName("qweeksnap_id")
    @Expose
    private String qweeksnap_id;
    @SerializedName("qweeksnap_views")
    @Expose
    private String qweeksnap_views;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("audio")
    @Expose
    private String audio;
    @SerializedName("avatar")
    @Expose
    private String profile_pic;
    @SerializedName("timeStamp")
    @Expose
    private String timeStamp;
    @SerializedName("liked")
    @Expose
    private String liked;
    @SerializedName("commentNum")
    @Expose
    private String commentNum;
    @SerializedName("upvoteNum")
    @Expose
    private String upvoteNum;
    @SerializedName("downvoteNum")
    @Expose
    private String downvoteNum;
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
     * The Drop Id
     */
    public String getDrop_Id() {
        return drop_id;
    }

    /**
     *
     * The Drop Id
     */
    public void setDrop_Id(String drop_id) {
        this.drop_id = drop_id;
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
     * The Username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The Fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     *
     * The Fullname
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     *
     * @return
     * The Drop Type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * The Drop Type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The Drop Text
     */
    public String getDrop() {
        return drop;
    }

    /**
     *
     * The Drop Text
     */
    public void setDrop(String drop) {
        this.drop = drop;
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
     * The QweekSnap Thumbnail
     */
    public String getQweekSnapThumb() {
        return qweeksnap_thumbnail;
    }

    /**
     *
     * @param qweeksnap_thumbnail
     * The QweekSnap Thumbnail
     */
    public void setQweekSnapThumb(String qweeksnap_thumbnail) {
        this.qweeksnap_thumbnail = qweeksnap_thumbnail;
    }

    /**
     *
     * @return
     * The QweekSnap ID
     */
    public String getQweekSnapID() {
        return qweeksnap_id;
    }

    /**
     *
     * @param qweeksnap_id
     * The QweekSnap ID
     */
    public void setQweekSnapID(String qweeksnap_id) {
        this.qweeksnap_id = qweeksnap_id;
    }

    /**
     *
     * @return
     * The QweekSnap Views
     */
    public String getQweekSnapViews() {
        return qweeksnap_views;
    }

    /**
     *
     * @param qweeksnap_views
     * The QweekSnap Views
     */
    public void setQweekSnapViews(String qweeksnap_views) {
        this.qweeksnap_views = qweeksnap_views;
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

    /**
     *
     * @return
     * The ProfileModel Picture
     */
    public String getProfilePic() {
        return profile_pic;
    }

    /**
     *
     * @param profile_pic
     * The ProfileModel Picture
     */
    public void setProfilePic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    /**
     *
     * @return
     * The TimeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     *
     * The TimeStamp
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     *
     * @return
     * The Liked
     */
    public String getLiked() {
        return liked;
    }

    /**
     *
     * The Liked
     */
    public void seLiked(String liked) {
        this.liked = liked;
    }

    /**
     *
     * @return
     * The Comment Number
     */
    public String getCommentNum() {
        return commentNum;
    }

    /**
     *
     * The Comment Number
     */
    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    /**
     *
     * @return
     * The Upvote Number
     */
    public String getUpvoteNum() {
        return upvoteNum;
    }

    /**
     *
     * The Upvote Number
     */
    public void setUpvoteNum(String upvoteNum) {
        this.upvoteNum = upvoteNum;
    }

    /**
     *
     * @return
     * The Downvote Number
     */
    public String getDownvoteNum() {
        return downvoteNum;
    }

    /**
     *
     * The Downvote Number
     */
    public void setDownvoteNum(String downvoteNum) {
        this.downvoteNum = downvoteNum;
    }
}
