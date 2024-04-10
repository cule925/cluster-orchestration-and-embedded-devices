package com.example.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasuringComponent {

    public enum ValueType {
        VAL_BOOLEAN, VAL_INT, VAL_FLOAT
    }

    public enum ComponentType {
        COMPONENT_SENSOR, COMPONENT_ACTUATOR
    }

    private int id;
    private String name;

    private ComponentType componentType;

    private float value;

    private ValueType valueType;

    private float valueMin;

    private float valueMax;

    @JsonCreator
    public MeasuringComponent(@JsonProperty("id") int id,
                              @JsonProperty("name") String name,
                              @JsonProperty("componentType") ComponentType componentType,
                              @JsonProperty("value") float value,
                              @JsonProperty("valueType") ValueType valueType,
                              @JsonProperty("valueMin") float valueMin,
                              @JsonProperty("valueMax") float valueMax) {
        this.id = id;
        this.name = name;
        this.componentType = componentType;
        this.value = value;
        this.valueType = valueType;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public float getValueMin() {
        return valueMin;
    }

    public void setValueMin(float valueMin) {
        this.valueMin = valueMin;
    }

    public float getValueMax() {
        return valueMax;
    }

    public void setValueMax(float valueMax) {
        this.valueMax = valueMax;
    }

    @Override
    public String toString() {
        return "MeasuringComponent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", componentType=" + componentType +
                ", value=" + value +
                ", valueType=" + valueType +
                '}';
    }

}
