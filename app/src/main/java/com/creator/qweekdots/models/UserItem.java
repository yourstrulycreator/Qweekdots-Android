package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserItem {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("fullname")
    @Expose
    private String fullname;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("cover")
    @Expose
    private String cover;
    @SerializedName("verified_status")
    @Expose
    private String verified_status;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("telephone")
    @Expose
    private String telephone;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("theme")
    @Expose
    private String theme;
    @SerializedName("followers_count")
    @Expose
    private String followers_count;
    @SerializedName("following_count")
    @Expose
    private String following_count;
    @SerializedName("drops_count")
    @Expose
    private String drops_count;
    @SerializedName("qweeksnap_count")
    @Expose
    private String qweeksnap_count;
    @SerializedName("liked_count")
    @Expose
    private String liked_count;
    @SerializedName("followed")
    @Expose
    private String followed;


    /**
     *
     * @return
     * The Initial Id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * The Initial Id
     */
    public void setId(String id) {
        this.id = id;
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
     * The ProfileModel Picture
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     *
     * The ProfileModel Picture
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     *
     * @return
     * The ProfileModel Cover
     */
    public String getProfileCover() {
        return cover;
    }

    /**
     *
     * The ProfileModel Cover
     */
    public void setProfileCover(String cover) {
        this.cover = cover;
    }

    public String getVerified_status() {
        return verified_status;
    }

    public void setVerified_status(String verified_status) {
        this.verified_status =verified_status;
    }

    /**
     *
     * @return
     * The Bio
     */
    public String getBio() {
        return bio;
    }

    /**
     *
     * The Bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     *
     * @return
     * The Email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * The Email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The Birthday
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     *
     * The Birthday
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     *
     * @return
     * The Telephone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     *
     * The Telephone
     */
    public void setTelephone(String bio) {
        this.telephone = telephone;
    }

    /**
     *
     * @return
     * The Location
     */
    public String getLocation() {
        return location;
    }

    /**
     *
     * The Location
     */
    public void setLocation(String location) {
        this.location= location;
    }

    /**
     *
     * @return
     * The Theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     *
     * The Theme
     */
    public void setTheme(String theme) {
        this.theme= theme;
    }

    /**
     *
     * @return
     * The Followers count
     */
    public String getFollowers_count() {
        return followers_count;
    }

    /**
     *
     * The Followers count
     */
    public void setFollowers_count(String followers_count) {
        this.followers_count= followers_count;
    }

    /**
     *
     * @return
     * The Following count
     */
    public String getFollowing_count() {
        return following_count;
    }

    /**
     *
     * The Drop count
     */
    public void setDrop_count(String drops_count) {
        this.drops_count = drops_count;
    }

    /**
     *
     * @return
     * The Drop count
     */
    public String getDrop_count() {
        return drops_count;
    }

    /**
     *
     * The QweekSnap count
     */
    public void setQweeksnap_count(String qweeksnap_count) {
        this.qweeksnap_count = qweeksnap_count;
    }

    /**
     *
     * @return
     * The QweekSnap count
     */
    public String getQweeksnap_count() {
        return qweeksnap_count;
    }

    /**
     *
     * The Liked count
     */
    public void setLiked_count(String liked_count) {
        this.liked_count = liked_count;
    }

    /**
     *
     * @return
     * The Followed
     */
    public String getFollowed() {
        return followed;
    }

    /**
     *
     * The Followed
     * @return
     */
    public String setFollowed(String followed) {
        this.followed = followed;
        return followed;
    }
}
