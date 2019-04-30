package com.shubhamk.datacollectionwear;

public class SensorData {
    Float ax;
    Float ay;
    Float az;
    String sensor;
    Long timestamp;


    public SensorData() {
    }

    public Float getAx() {
        return ax;
    }

    public void setAx(Float ax) {
        this.ax = ax;
    }

    public Float getAy() {
        return ay;
    }

    public void setAy(Float ay) {
        this.ay = ay;
    }

    public Float getAz() {
        return az;
    }

    public void setAz(Float az) {
        this.az = az;
    }

    public SensorData(Float ax, Float ay, Float az, String sensor, Long timestamp) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.sensor = sensor;
        this.timestamp = timestamp;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
