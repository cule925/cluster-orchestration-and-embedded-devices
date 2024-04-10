package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasuringComponentNew {

    private int id;

    private float value;

    @JsonCreator
    public MeasuringComponentNew(@JsonProperty("id") int id,
                                 @JsonProperty("value") float value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MeasuringComponentNew{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }

}
