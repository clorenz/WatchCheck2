package de.uhrenbastler.watchcheck.models;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by clorenz on 10.12.14.
 */
public class Log extends SugarRecord<Log> implements Serializable {

    private int watchId;
    private int period;
    private Timestamp referenceTime;
    private Timestamp watchTime;
    private String position;
    private int temperature;
    private String comment;

    public Log() {}

    public Log(int watchId, int period, Timestamp referenceTime, Timestamp watchTime, String position, int temperature, String comment) {
        this.watchId = watchId;
        this.period = period;
        this.referenceTime = referenceTime;
        this.watchTime = watchTime;
        this.position = position;
        this.temperature = temperature;
        this.comment = comment;
    }

    public int getWatchId() {
        return watchId;
    }

    public void setWatchId(int watchId) {
        this.watchId = watchId;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Timestamp getReferenceTime() {
        return referenceTime;
    }

    public void setReferenceTime(Timestamp referenceTime) {
        this.referenceTime = referenceTime;
    }

    public Timestamp getWatchTime() {
        return watchTime;
    }

    public void setWatchTime(Timestamp watchTime) {
        this.watchTime = watchTime;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Log{" +
                "watchId=" + watchId +
                ", period=" + period +
                ", referenceTime=" + referenceTime +
                ", watchTime=" + watchTime +
                ", position='" + position + '\'' +
                ", temperature=" + temperature +
                ", comment='" + comment + '\'' +
                '}';
    }
}
