package org.akvo.caddisfly.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Group {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("imageScale")
    @Expose
    private String imageScale;
    @SerializedName("instructions")
    @Expose
    private List<Object> instructions = null;
    @SerializedName("tests")
    @Expose
    private List<Test> tests = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getImageScale() {
        return imageScale;
    }

    public void setImageScale(String imageScale) {
        this.imageScale = imageScale;
    }

    public List<Object> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Object> instructions) {
        this.instructions = instructions;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

}
