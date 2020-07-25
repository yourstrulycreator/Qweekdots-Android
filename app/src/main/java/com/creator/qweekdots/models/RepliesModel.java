package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class RepliesModel {

    @SerializedName("feed")
    @Expose
    private ArrayList<CommentItem> feed = new ArrayList<>();
    @SerializedName("cursor")
    @Expose
    private List<Cursor> cursor;

    /**
     *
     * @return
     * The results
     */
    public ArrayList<CommentItem> getFeedItems() {
        return feed;
    }


    /**
     *
     * @param feed
     * The results
     */
    public void setFeedItems(ArrayList<CommentItem> feed) {
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
