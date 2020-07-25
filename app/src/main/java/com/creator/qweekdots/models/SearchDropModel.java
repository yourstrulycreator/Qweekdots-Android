package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchDropModel {

    @SerializedName("feed")
    @Expose
    private List<FeedItem> feed = new ArrayList<FeedItem>();
    @SerializedName("cursor")
    @Expose
    private List<Cursor> cursor;

    /**
     *
     * @return
     * The results
     */
    public List<FeedItem> getFeedItems() {
        return feed;
    }


    /**
     *
     * @param feed
     * The results
     */
    public void setFeedItems(List<FeedItem> feed) {
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
