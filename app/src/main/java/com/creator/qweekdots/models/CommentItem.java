package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommentItem {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("commented_drop_id")
    @Expose
    private String commented_drop_id;
    @SerializedName("drop_id")
    @Expose
    private String drop_id;
    @SerializedName("parent_id")
    @Expose
    private int parent_id;
    @SerializedName("user_id")
    @Expose
    private String user_id;
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
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("audio")
    @Expose
    private String audio;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("timeStamp")
    @Expose
    private String timeStamp;
    @SerializedName("replied")
    @Expose
    private String replied;
    @SerializedName("liked")
    @Expose
    private String liked;
    @SerializedName("upvoted")
    @Expose
    private String upvoted;
    @SerializedName("downvoted")
    @Expose
    private String downvoted;
    @SerializedName("commentNum")
    @Expose
    private String commentNum;
    @SerializedName("upvoteNum")
    @Expose
    private String upvoteNum;
    @SerializedName("downvoteNum")
    @Expose
    private String downvoteNum;
    @SerializedName("depth")
    @Expose
    private int depth;
    @SerializedName("childCount")
    @Expose
    private int childCount;

    /**
     *
     * @return
     * The Initial Id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * The Initial Id
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     *
     * @return
     * The Parent Id
     */
    public int getParent_Id() {
        return parent_id;
    }

    /**
     *
     * The Parent Id
     */
    public void setParent_Id(int parent_id) {
        this.parent_id = parent_id;
    }

    /**
     *
     * @return
     * The Commented Drop Id
     */
    public String getCommented_drop_id() {
        return commented_drop_id;
    }

    /**
     *
     * The Commented Drop Id
     */
    public void setCommented_drop_id(String commented_drop_id) {
        this.commented_drop_id = commented_drop_id;
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
     * The User Id
     */
    public String getUserID() {
        return user_id;
    }

    /**
     *
     * The User Id
     */
    public void setUserID(String user_id) {
        this.user_id = user_id;
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
    public String getAvatar() {
        return avatar;
    }

    /**
     *
     * @param avatar
     * The ProfileModel Picture
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
     * The Replied
     */
    public String getReplied() {
        return replied;
    }

    /**
     *
     * The Replied
     */
    public void setReplied(String replied) {
        this.replied = replied;
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
    public void setLiked(String liked) {
        this.liked = liked;
    }

    /**
     *
     * @return
     * The Upvoted
     */
    public String getUpvoted() {
        return upvoted;
    }

    /**
     *
     * The Upvoted
     */
    public void setUpvoted(String upvoted) {
        this.upvoted = upvoted;
    }

    /**
     *
     * @return
     * The Downvoted
     */
    public String getDownvoted() {
        return downvoted;
    }

    /**
     *
     * The Downvoted
     */
    public void setDownvoted(String downvoted) {
        this.downvoted = downvoted;
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

    /**
     *
     * @return
     * Comment Depth
     */
    public int getCommentDepth() {
        return depth;
    }

    /**
     *
     * Set Comment Depth
     */
    public void setCommentDepth(int depth) {
        this.depth = depth;
    }

    /**
     *
     * @return
     * Comment Child Counts
     */
    public int getChildCount() {
        return childCount;
    }

    /**
     *
     * Set Child Count if any
     */
    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }
}
