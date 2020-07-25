package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class DropModel {

    @SerializedName("drop")
    @Expose
    private List<FeedItem> drop = new ArrayList<FeedItem>();

    /**
     *
     * @return
     * The results
     */
    public List<FeedItem> getDropItems() {
        return drop;
    }


    /**
     *
     * @param drop
     * The results
     */
    public void setDropItems(List<FeedItem> drop) {
        this.drop = drop;
    }

}
