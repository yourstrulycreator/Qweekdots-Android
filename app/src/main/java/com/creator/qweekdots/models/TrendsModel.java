package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TrendsModel {
    @SerializedName("trends")
    @Expose
    private List<TrendsItem> trends = new ArrayList<TrendsItem>();

    /**
     *
     * @return
     * The results
     */
    public List<TrendsItem> getTrendItems() {
        return trends;
    }


    /**
     *
     * @param trends
     * The results
     */
    public void setTrendItems(List<TrendsItem> trends) {
        this.trends = trends;
    }
}
