package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchUserModel {

    @SerializedName("feed")
    @Expose
    private List<UserItem> feed = new ArrayList<UserItem>();
    @SerializedName("cursor")
    @Expose
    private List<Cursor> cursor;

    /**
     *
     * @return
     * The results
     */
    public List<UserItem> getUserItems() {
        return feed;
    }


    /**
     *
     * @param feed
     * The results
     */
    public void setUserItems(List<UserItem> feed) {
        this.feed = feed;
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
