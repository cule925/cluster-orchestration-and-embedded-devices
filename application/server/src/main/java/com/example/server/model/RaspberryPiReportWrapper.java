package com.example.server.model;

import javax.sound.midi.Receiver;
import java.time.LocalDateTime;
import java.util.*;

public class RaspberryPiReportWrapper {

    private RaspberryPiReport raspberryPiReport;

    private RaspberryPiReceive raspberryPiReceive;

    private LocalDateTime lastActivity;

    public RaspberryPiReportWrapper(RaspberryPiReport raspberryPiReport, LocalDateTime lastActivity) {

        this.raspberryPiReport = raspberryPiReport;
        this.lastActivity = lastActivity;

        List<ESP32Receive> esp32ReceiveList = constructNewRaspberryPiReceive(raspberryPiReport.getEsp32ReportList());
        this.raspberryPiReceive = new RaspberryPiReceive(raspberryPiReport.getUuid(), esp32ReceiveList);

    }

    private List<ESP32Receive> constructNewRaspberryPiReceive(List<ESP32Report> newESP32ReportList) {

        List<ESP32Receive> newESP32ReceiveList = new LinkedList<>();

        // Initial values of the actuator states that will be returned to ESP32
        for(ESP32Report esp32Report : newESP32ReportList) {

            List<MeasuringComponentNew> measuringComponentNewList = new LinkedList<>();
            ESP32Receive esp32Receive = new ESP32Receive(esp32Report.getUuid(), measuringComponentNewList);

            for(MeasuringComponent measuringComponent : esp32Report.getMeasuringComponentList()) {

                if (measuringComponent.getComponentType() == MeasuringComponent.ComponentType.COMPONENT_ACTUATOR) {

                    MeasuringComponentNew measuringComponentNew = new MeasuringComponentNew(measuringComponent.getId(), measuringComponent.getValue());
                    esp32Receive.getMeasuringComponentNewList().add(measuringComponentNew);

                }

            }

            newESP32ReceiveList.add(esp32Receive);

        }

        return newESP32ReceiveList;

    }

    public void updateESP32List(List<ESP32Report> newESP32ReportList) {

        List<ESP32Receive> newESP32ReceiveList = constructNewRaspberryPiReceive(newESP32ReportList);

        // Fill the new reports
        raspberryPiReport.setEsp32ReportList(newESP32ReportList);

        // First remove the non-existent receives
        Iterator<ESP32Receive> esp32ReceiveIterator = raspberryPiReceive.getEsp32ReceiveList().iterator();

        while(esp32ReceiveIterator.hasNext()) {
            if(!newESP32ReceiveList.contains(esp32ReceiveIterator.next())) esp32ReceiveIterator.remove();
        }

        // Now add new receives
        List<ESP32Receive> esp32ReceiveList = raspberryPiReceive.getEsp32ReceiveList();

        for(ESP32Receive esp32Receive : newESP32ReceiveList) {
            if(!esp32ReceiveList.contains(esp32Receive)) esp32ReceiveList.add(esp32Receive);
        }

    }

    public RaspberryPiReport getRaspberryPiReport() {
        return raspberryPiReport;
    }

    public void setRaspberryPiReport(RaspberryPiReport raspberryPiReport) {
        this.raspberryPiReport = raspberryPiReport;
    }

    public RaspberryPiReceive getRaspberryPiReceive() {
        return raspberryPiReceive;
    }

    public void setRaspberryPiReceive(RaspberryPiReceive raspberryPiReceive) {
        this.raspberryPiReceive = raspberryPiReceive;
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
        RaspberryPiReportWrapper that = (RaspberryPiReportWrapper) o;
        return Objects.equals(raspberryPiReport, that.raspberryPiReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raspberryPiReport);
    }

    @Override
    public String toString() {
        return "RaspberryPiReportWrapper{" +
                "raspberryPiReport=" + raspberryPiReport +
                ", raspberryPiReceive=" + raspberryPiReceive +
                ", lastActivity=" + lastActivity +
                '}';
    }

}
