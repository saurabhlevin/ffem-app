package org.akvo.caddisfly.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QualityGuide {

    @SerializedName("standards")
    @Expose
    private List<Standard> standards = null;

    public List<Standard> getStandards() {
        return standards;
    }
}