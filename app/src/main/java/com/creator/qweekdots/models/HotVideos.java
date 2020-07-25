package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class HotVideos {
    @SerializedName("feed")
    @Expose
    private List<VideoItem> feed = new ArrayList<VideoItem>();

    /**
     *
     * @return
     * The results
     */
    public List<VideoItem> getVideoItems() {
        return feed;
    }


    /**
     *
     * @param feed
     * The results
     */
    public void setVideoItems(List<VideoItem> feed) {
        this.feed = feed;
    }
}
