package com.example.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class RaspberryPiReceive {

    private UUID uuid;

    private List<ESP32Receive> esp32ReceiveList;

    @JsonCreator
    public RaspberryPiReceive(@JsonProperty("uuid") UUID uuid, @JsonProperty("esp32ReceiveList") List<ESP32Receive> esp32ReceiveList) {
        this.uuid = uuid;
        this.esp32ReceiveList = esp32ReceiveList;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<ESP32Receive> getEsp32ReceiveList() {
        return esp32ReceiveList;
    }

    public void setEsp32ReceiveList(List<ESP32Receive> esp32ReceiveList) {
        this.esp32ReceiveList = esp32ReceiveList;
    }

    @Override
    public String toString() {
        return "RaspberryPiReceive{" +
                "uuid=" + uuid +
                ", esp32ReceiveList=" + esp32ReceiveList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaspberryPiReceive that = (RaspberryPiReceive) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
