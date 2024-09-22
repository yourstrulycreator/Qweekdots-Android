package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SpaceItem {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("space_name")
    @Expose
    private String space_name;
    @SerializedName("space_galaxy")
    @Expose
    private String space_galaxy;
    @SerializedName("space_art")
    @Expose
    private String space_art;
    @SerializedName("space_members")
    @Expose
    private Integer space_members;
    @SerializedName("followed_count")
    @Expose
    private String followed_count;
    @SerializedName("pinned")
    @Expose
    private String pinned;

    /**
     *
     * @return
     * The Id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * The Id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The Space Name
     */
    public String getSpacename() {
        return space_name;
    }

    /**
     *
     * The Spacename
     */
    public void setSpacename(String space_name) {
        this.space_name = space_name;
    }

    /**
     *
     * @return
     * The Space Galaxy
     */
    public String getSpaceGalaxy() {
        return space_galaxy;
    }

    /**
     *
     * The Space Galaxy
     */
    public void setSpaceGalaxy(String space_galaxy) {
        this.space_galaxy = space_galaxy;
    }

    /**
     *
     * @return
     * The Space Art
     */
    public String getSpaceArt() {
        return space_art;
    }

    /**
     *
     * The Space Art
     */
    public void setSpaceArt(String space_art) {
        this.space_art = space_art;
    }

    /**
     *
     * @return
     * The Followed count
     */
    public String getFollowed_count() {
        return followed_count;
    }

    /**
     *
     * The Followed count
     */
    public void setFollowed_count(String followed_count) {
        this.followed_count = followed_count;
    }

    /**
     *
     * @return
     * The Pinned
     */
    public String getPinned() {
        return pinned;
    }

    /**
     *
     * The Pinned
     */
    public String setPinned(String pinned) {
        this.pinned = pinned;
        return pinned;
    }

    /**
     *
     * @return
     * The space_members
     */
    public Integer getSpaceMembers() {
        return space_members;
    }

    /**
     *
     * The space_members
     */
    public void setSpaceMembers(Integer space_members) {
        this.space_members = space_members;
    }

}
