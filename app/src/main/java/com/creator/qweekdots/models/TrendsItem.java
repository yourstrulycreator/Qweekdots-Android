package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrendsItem {
    @SerializedName("title")
    @Expose
    private String title;

    /**
     *
     * @return
     * The Title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * The Username
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
