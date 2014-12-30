package de.uhrenbastler.greendaogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

/**
 * Created by clorenz on 30.12.14.
 *
 * With much help from a great blog post at
 * http://blog.surecase.eu/using-greendao-with-android-studio-ide/
 */
public class GreenDaoGenerator {

    public static void main(String args[]) throws Exception {

        Schema watchCheck = new Schema(5, "watchcheck.db");

        Entity watch = watchCheck.addEntity("Watch");
        watch.implementsSerializable();
        watch.addIdProperty().autoincrement();
        watch.addStringProperty("name").notNull();
        watch.addStringProperty("serial");
        watch.addDateProperty("createdAt");
        watch.addStringProperty("comment");

        Entity log = watchCheck.addEntity("Log");
        log.implementsSerializable();
        log.addIdProperty().autoincrement();
        Property watchId = log.addLongProperty("watchId").notNull().getProperty();
        log.addIntProperty("period").notNull();
        Property referenceTime = log.addDateProperty("referenceTime").notNull().getProperty();
        log.addDateProperty("watchTime").notNull();
        log.addStringProperty("position");
        log.addIntProperty("temperature");
        log.addStringProperty("comment");

        ToMany watchToLogs = watch.addToMany(log, watchId);
        watchToLogs.setName("logs"); // Optional
        watchToLogs.orderAsc(referenceTime); // Optional

        new DaoGenerator().generateAll(watchCheck, args[0]);
    }
}
