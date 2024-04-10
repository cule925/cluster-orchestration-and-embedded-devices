package com.example.clientserver.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ESP32Receive {

    private UUID uuid;

    private List<MeasuringComponentNew> measuringComponentNewList;

    @JsonCreator
    public ESP32Receive(@JsonProperty("uuid") UUID uuid, @JsonProperty("measuringComponentNewList") List<MeasuringComponentNew> measuringComponentNewList) {
        this.uuid = uuid;
        this.measuringComponentNewList = measuringComponentNewList;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<MeasuringComponentNew> getMeasuringComponentNewList() {
        return measuringComponentNewList;
    }

    public void setMeasuringComponentNewList(List<MeasuringComponentNew> measuringComponentNewList) {
        this.measuringComponentNewList = measuringComponentNewList;
    }

    @Override
    public String toString() {
        return "ESP32Receive{" +
                "uuid=" + uuid +
                ", measuringComponentNewList=" + measuringComponentNewList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ESP32Receive that = (ESP32Receive) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
