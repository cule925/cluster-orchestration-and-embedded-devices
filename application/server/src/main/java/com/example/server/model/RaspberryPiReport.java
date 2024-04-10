package com.example.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class RaspberryPiReport {

    private UUID uuid;

    private String name;

    private List<ESP32Report> esp32ReportList;

    @JsonCreator
    public RaspberryPiReport(@JsonProperty("uuid") UUID uuid,
                             @JsonProperty("name") String name,
                             @JsonProperty("esp32ReportList") List<ESP32Report> esp32ReportList) {

        this.uuid = uuid;
        this.name = name;
        this.esp32ReportList = esp32ReportList;
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

    public List<ESP32Report> getEsp32ReportList() {
        return esp32ReportList;
    }

    public void setEsp32ReportList(List<ESP32Report> esp32ReportList) {
        this.esp32ReportList = esp32ReportList;
    }

    @Override
    public String toString() {
        return "RaspberryPiReport{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", esp32ReportList=" + esp32ReportList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaspberryPiReport that = (RaspberryPiReport) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
