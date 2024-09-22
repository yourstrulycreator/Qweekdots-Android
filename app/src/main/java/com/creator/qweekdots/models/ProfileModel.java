package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ProfileModel {

    @SerializedName("user")
    @Expose
    private List<UserItem> user = new ArrayList<>();
    @SerializedName("cursor")
    @Expose
    private List<Cursor> cursor;

    /**
     *
     * @return
     * The results
     */
    public List<UserItem> getUserItems() {
        return user;
    }


    /**
     *
     * @param user
     * The results
     */
    public void setUserItems(List<UserItem> user) {
        this.user = user;
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
