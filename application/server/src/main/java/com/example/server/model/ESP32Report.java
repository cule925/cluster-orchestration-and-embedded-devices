package com.example.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ESP32Report {

    private UUID uuid;

    private String name;

    private List<MeasuringComponent> measuringComponentList;

    @JsonCreator
    public ESP32Report(@JsonProperty("uuid") UUID uuid,
                       @JsonProperty("name") String name,
                       @JsonProperty("measuringComponentList") List<MeasuringComponent> measuringComponentList) {
        this.uuid = uuid;
        this.name = name;
        this.measuringComponentList = measuringComponentList;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MeasuringComponent> getMeasuringComponentList() {
        return measuringComponentList;
    }

    public void setComponentList(List<MeasuringComponent> measuringComponentList) {
        this.measuringComponentList = measuringComponentList;
    }

    @Override
    public String toString() {
        return "ESP32Report{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", measuringComponentList=" + measuringComponentList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ESP32Report that = (ESP32Report) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
