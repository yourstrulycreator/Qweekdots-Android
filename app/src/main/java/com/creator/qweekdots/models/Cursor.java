package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Cursor {
    @SerializedName("pages")
    @Expose
    private int pages;
    @SerializedName("next_link")
    @Expose
    private String next_link;
    @SerializedName("prev_link")
    @Expose
    private String prev_link;
    @SerializedName("max_id")
    @Expose
    private String max_id;
    @SerializedName("since_id")
    @Expose
    private String since_id;


    /**
     *
     * @return
     * Pages Num
     */
    public int getPagesNum() {
        return pages;
    }

    /**
     *
     * @param pages
     * Pages Num
     */
    public void setPagesNum(int pages) {
        this.pages = pages;
    }

    /**
     *
     * @return
     * The Next Link
     */
    public String getNextLink() {
        return next_link;
    }

    /**
     *
     * @param next_link
     * The Next Link
     */
    public void setNext_link(String next_link) {
        this.next_link = next_link;
    }

    /**
     *
     * @return
     * The Previous Link
     */
    public String getPrevLink() {
        return prev_link;
    }

    /**
     *
     * @param prev_link
     * The Previous Link
     */
    public void setPrevLink(String prev_link) {
        this.prev_link = prev_link;
    }

    /**
     *
     * @return
     * The Max Id
     */
    public String getMaxID() {
        return max_id;
    }

    /**
     *
     * @param max_id
     * The Max Id
     */
    public void setMaxID(String max_id) {
        this.max_id = max_id;
    }

    /**
     *
     * @return
     * The Since Id
     */
    public String getSinceID() {
        return since_id;
    }

    /**
     *
     * @param since_id
     * The Since Id
     */
    public void setSinceID(String since_id) {
        this.since_id = since_id;
    }
}
