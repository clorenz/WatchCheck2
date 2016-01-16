package de.uhrenbastler.watchcheck;

/**
 * Created by clorenz on 14.03.15.
 */
public enum Deviations {

    ALL("ALL"),
    WORN("WORN"),
    OTHER("OTHER"),
    WINDER("WINDER"),
    DD("DD"),
    DU("DU"),
    O3("3U"),
    O6("6U"),
    O9("9U"),
    O12("12U");

    String nameForLog;


    Deviations(String nameForLog) {
        this.nameForLog = nameForLog;
    }
}
