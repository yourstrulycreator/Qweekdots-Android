package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pager {
    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("total_results")
    @Expose
    private Integer totalResults;
    @SerializedName("total_pages")
    @Expose
    private int totalPages;

    /**
     *
     * @return
     * The page
     */
    public Integer getPage() {
        return page;
    }

    /**
     *
     * @param page
     * The page
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     *
     * @param totalResults
     * The total_results
     */
    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    /**
     *
     * @return
     * The totalPages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     *
     * @param totalPages
     * The total_pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
