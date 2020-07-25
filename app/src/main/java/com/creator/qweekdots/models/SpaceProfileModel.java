package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SpaceProfileModel {
    @SerializedName("space")
    @Expose
    private List<SpaceItem> space = new ArrayList<SpaceItem>();

    /**
     *
     * @return
     * The results
     */
    public List<SpaceItem> getSpaceItems() {
        return space;
    }


    /**
     *
     * @param user
     * The results
     */
    public void setSpaceItems(List<SpaceItem> user) {
        this.space = space;
    }
}
