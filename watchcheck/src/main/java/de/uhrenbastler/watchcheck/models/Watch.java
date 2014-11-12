package de.uhrenbastler.watchcheck.models;

import com.orm.SugarRecord;

import java.sql.Timestamp;

/**
 * Created by clorenz on 12.11.14.
 */
public class Watch extends SugarRecord<Watch> {

    String name;
    String serial;
    Timestamp createdAt;
    String comment;

    public Watch() {}

    public Watch(String name, String serial, Timestamp createdAt, String comment) {
        this.name = name;
        this.serial = serial;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Watch{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", serial='" + serial + '\'' +
                ", createdAt=" + createdAt +
                ", comment='" + comment + '\'' +
                '}';
    }
}
