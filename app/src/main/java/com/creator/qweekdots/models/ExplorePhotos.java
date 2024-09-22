package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ExplorePhotos {
    @SerializedName("feed")
    @Expose
    private List<FeedItem> feed = new ArrayList<FeedItem>();
    @SerializedName("cursor")
    @Expose
    private List<Pager> cursor;

    /**
     *
     * @return
     * The results
     */
    public List<FeedItem> getPhotoItems() {
        return feed;
    }


    /**
     *
     * @param feed
     * The results
     */
    public void setPhotoItems(List<FeedItem> feed) {
        this.feed = feed;
    }

    /**
     *
     * @return
     * The cursors
     */
    public List<Pager> getPageLinks() {
        return cursor;
    }


    /**
     *
     * @param cursor
     * The results
     */
    public void setPagelinks(List<Pager> cursor) {
        this.cursor = cursor;
    }

}
