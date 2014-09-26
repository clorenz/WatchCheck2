package de.uhrenbastler.watchcheck.data;

import android.net.Uri;
import android.provider.BaseColumns;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by clorenz on 14.02.14.
 */
public class Log implements Serializable {

    private static final long serialVersionUID = -6533940264730427036L;
    long id;
    long watchId;
    String modus;
    Calendar localTimestamp;
    double ntpDiff;
    double deviation;
    boolean flagReset;
    String position;
    int temperature;
    String comment;
    double dailyDeviation;
    double ntpCorrectionFactor=0d;

    public Log() {}



    /**
     * @return the id
     */
    public long getId() {
        return id;
    }



    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }



    /**
     * @return the watchId
     */
    public long getWatchId() {
        return watchId;
    }



    /**
     * @param watchId the watchId to set
     */
    public void setWatchId(long watchId) {
        this.watchId = watchId;
    }



    /**
     * @return the modus
     */
    public String getModus() {
        return modus;
    }



    /**
     * @param modus the modus to set
     */
    public void setModus(String modus) {
        this.modus = modus;
    }



    /**
     * @return the localTimestamp
     */
    public Calendar getLocalTimestamp() {
        return localTimestamp;
    }



    /**
     * @param localTimestamp the localTimestamp to set
     */
    public void setLocalTimestamp(Calendar localTimestamp) {
        this.localTimestamp = localTimestamp;
    }



    /**
     * @return the ntpDiff
     */
    public double getNtpDiff() {
        return ntpDiff;
    }



    /**
     * @param ntpDiff the ntpDiff to set
     */
    public void setNtpDiff(double ntpDiff) {
        this.ntpDiff = ntpDiff;
    }



    /**
     * @return the deviation
     */
    public double getDeviation() {
        return deviation;
    }



    /**
     * @param deviation the deviation to set
     */
    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }



    /**
     * @return the flagReset
     */
    public boolean isFlagReset() {
        return flagReset;
    }



    /**
     * @param flagReset the flagReset to set
     */
    public void setFlagReset(boolean flagReset) {
        this.flagReset = flagReset;
    }



    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }



    /**
     * @param position the position to set
     */
    public void setPosition(String position) {
        this.position = position;
    }



    /**
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }



    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }



    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }



    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }




    /**
     * @return the dailyDeviation
     */
    public double getDailyDeviation() {
        return dailyDeviation;
    }



    /**
     * @param dailyDeviation the dailyDeviation to set
     */
    public void setDailyDeviation(double dailyDeviation) {
        this.dailyDeviation = dailyDeviation;
    }


    public boolean isNtpMode() {
        return "1".equals(modus);
    }



    public void setNtpCorrectionFactor(double factor) {
        this.ntpCorrectionFactor = factor;
    }


    public double getNtpCorrectionFactor() {
        return ntpCorrectionFactor;
    }







    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Log [id=" + id + "\n\twatchId=" + watchId + "\n\tmodus=" + modus
                + "\n\tisNtpMode="+isNtpMode()
                + "\n\tlocalTimestamp=" + (localTimestamp!=null?localTimestamp.getTime():"null")
                + "\n\tntpDiff=" + ntpDiff
                + "\n\tntpCorrectionFactor="+ntpCorrectionFactor
                + "\n\tdeviation=" + deviation + "\n\tflagReset=" + flagReset
                + "\n\tposition=" + position + "\n\ttemperature=" + temperature
                + "\n\tcomment=" + comment + "]";
    }




    public static final class Logs implements BaseColumns {

        private Logs() {}

        public static final String TABLE_NAME="logs";

        public static final Uri CONTENT_URI = Uri.parse("content://"+WatchCheckLogContentProvider.AUTHORITY+"/logs");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.watchcheck.logs";

        public static final String LOG_ID = "_id";

        public static final String WATCH_ID = "watch_id";

        public static final String MODUS = "modus";

        public static final String LOCAL_TIMESTAMP = "local_timestamp";             // local timestamp

        public static final String NTP_DIFF = "ntpDiff";                // diff local to ntp

        public static final String DEVIATION = "deviation";

        /**
         * This flag, when set, indicates the begin of a new measure period
         */
        public static final String FLAG_RESET = "reset";

        public static final String POSITION = "position";

        public static final String TEMPERATURE = "temperature";

        public static final String COMMENT = "comment";

        public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME+" (" +
                LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                WATCH_ID + " INTEGER, "+
                MODUS + " VARCHAR(5), "+
                LOCAL_TIMESTAMP + " TIMESTAMP, "+
                NTP_DIFF + " DECIMAL(6,2), "+
                DEVIATION + " DECIMAL(6,2), "+
                FLAG_RESET + " BOOLEAN, "+
                POSITION + " VARCHAR(2), "+
                TEMPERATURE + " INTEGER, "+
                COMMENT + " TEXT);";
    }
}
