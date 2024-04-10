package com.example.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class WebPostJSON {

    private UUID raspberryPiUUID;

    private UUID esp32UUID;

    private int id;

    private float value;

    @JsonCreator
    public WebPostJSON(@JsonProperty("raspberryPiUUID") UUID raspberryPiUUID, @JsonProperty("esp32UUID") UUID esp32UUID, @JsonProperty("id") int id, @JsonProperty("value") float value) {
        this.raspberryPiUUID = raspberryPiUUID;
        this.esp32UUID = esp32UUID;
        this.id = id;
        this.value = value;
    }

    public UUID getRaspberryPiUUID() {
        return raspberryPiUUID;
    }

    public void setRaspberryPiUUID(UUID raspberryPiUUID) {
        this.raspberryPiUUID = raspberryPiUUID;
    }

    public UUID getEsp32UUID() {
        return esp32UUID;
    }

    public void setEsp32UUID(UUID esp32UUID) {
        this.esp32UUID = esp32UUID;
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
        return "WebPostJSON{" +
                "raspberryPiUUID=" + raspberryPiUUID +
                ", esp32UUID=" + esp32UUID +
                ", id=" + id +
                ", value=" + value +
                '}';
    }

}
