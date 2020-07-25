package com.creator.qweekdots.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RingItem {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("ring_id")
    @Expose
    private String ring_id;
    @SerializedName("ring_name")
    @Expose
    private String ring_name;
    @SerializedName("ring_desc")
    @Expose
    private String ring_desc;
    @SerializedName("ring_cover")
    @Expose
    private String ring_cover;

    /**
     *
     * @return
     * The Initial Id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * The Initial Id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The RIng Id
     */
    public String getRingID() {
        return ring_id;
    }

    /**
     *
     * The Ring Id
     */
    public void setRingID(String ring_id) {
        this.ring_id = ring_id;
    }

    /**
     *
     * @return
     * The Ring Name
     */
    public String getRingname() {
        return ring_name;
    }

    /**
     *
     * The Ring Name
     */
    public void setRingname(String ring_name) {
        this.ring_name = ring_name;
    }

    /**
     *
     * @return
     * The Ring Bio
     */
    public String getRingBio() {
        return ring_desc;
    }

    /**
     *
     * The Ring Bio
     */
    public void setRingBio(String ring_desc) {
        this.ring_desc = ring_desc;
    }


    /**
     *
     * @return
     * The Ring Cover
     */
    public String getRingCover() {
        return ring_cover;
    }

    /**
     *
     * The Ring Cover
     */
    public void setRingCover(String ring_cover) {
        this.ring_cover = ring_cover;
    }

}
