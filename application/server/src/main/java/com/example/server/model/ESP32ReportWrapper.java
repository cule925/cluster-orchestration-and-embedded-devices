package com.example.server.model;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ESP32ReportWrapper {

    // Last Report
    private ESP32Report esp32Report;

    // New actuator values
    private ESP32Receive esp32Receive;

    // Last activity timestamp
    private LocalDateTime lastActivity;

    public ESP32ReportWrapper(ESP32Report esp32Report, LocalDateTime lastActivity) {

        this.esp32Report = esp32Report;
        this.lastActivity = lastActivity;

        List<MeasuringComponentNew> measuringComponentNewList = new LinkedList<>();
        this.esp32Receive = new ESP32Receive(esp32Report.getUuid(), measuringComponentNewList);

        constructESP32Receive();

    }

    private void constructESP32Receive() {

        // Initial values of the actuator states that will be returned to ESP32
        for(MeasuringComponent measuringComponent : esp32Report.getMeasuringComponentList()) {

            if(measuringComponent.getComponentType() == MeasuringComponent.ComponentType.COMPONENT_ACTUATOR) {

                MeasuringComponentNew measuringComponentNew = new MeasuringComponentNew(measuringComponent.getId(), measuringComponent.getValue());
                esp32Receive.getMeasuringComponentNewList().add(measuringComponentNew);

            }

        }

    }

    public ESP32Report getEsp32Report() {
        return esp32Report;
    }

    public void setEsp32Report(ESP32Report esp32Report) {
        this.esp32Report = esp32Report;
    }

    public ESP32Receive getEsp32Receive() {
        return esp32Receive;
    }

    public void setEsp32Receive(ESP32Receive esp32Receive) {
        this.esp32Receive = esp32Receive;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ESP32ReportWrapper that = (ESP32ReportWrapper) o;
        return Objects.equals(esp32Report, that.esp32Report);
    }

    @Override
    public int hashCode() {
        return Objects.hash(esp32Report);
    }

}
