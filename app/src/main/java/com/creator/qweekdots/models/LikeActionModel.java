package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LikeActionModel {
    @SerializedName("likeNum")
    @Expose
    private String likeNum;

    /**
     *
     * @return
     * The Like Number
     */
    public String getLikeNum() {
        return likeNum;
    }

    /**
     *
     * The Upvote Number
     */
    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }
}
