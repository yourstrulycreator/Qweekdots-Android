package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ProfileModel {

    @SerializedName("user")
    @Expose
    private List<UserItem> user = new ArrayList<UserItem>();

    /**
     *
     * @return
     * The results
     */
    public List<UserItem> getUserItems() {
        return user;
    }


    /**
     *
     * @param user
     * The results
     */
    public void setUserItems(List<UserItem> user) {
        this.user = user;
    }

}
