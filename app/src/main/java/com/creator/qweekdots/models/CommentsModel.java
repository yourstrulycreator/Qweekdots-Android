package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class CommentsModel {

    @SerializedName("feed")
    @Expose
    private List<CommentItem> feed = new ArrayList<>();
    @SerializedName("cursor")
    @Expose
    private List<Cursor> cursor;

    /**
     *
     * @return
     * The results
     */
    public List<CommentItem> getFeedItems() {
        return ThreadedComments.toThreadedComments(feed);
    }


    /**
     *
     * @param feed
     * The results
     */
    public void setFeedItems(List<CommentItem> feed) {
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
