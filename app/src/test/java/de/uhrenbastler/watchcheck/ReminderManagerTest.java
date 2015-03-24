package de.uhrenbastler.watchcheck;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.uhrenbastler.watchcheck.reminder.ReminderManager;

import static org.junit.Assert.*;

public class ReminderManagerTest {

    @Test
    public void setFirstAlarmMillisForDateInThePastAndHourMinuteInThePastSetsNextAlarmInLessThan24Hours() {
        Calendar dateInThePast = new GregorianCalendar();
        dateInThePast.set(Calendar.HOUR_OF_DAY,12);
        dateInThePast.set(Calendar.MINUTE,00);
        dateInThePast.add(Calendar.YEAR,-1);

        long nextAlarmMillis = ReminderManager.getFirstAlarmMillis(dateInThePast.getTimeInMillis());

        Calendar today = new GregorianCalendar();
        Calendar nextAlarm = new GregorianCalendar();
        nextAlarm.setTimeInMillis(nextAlarmMillis);

        long millisToNextAlarm = nextAlarmMillis - today.getTimeInMillis();
        assertTrue(millisToNextAlarm >= 0);
        assertTrue(millisToNextAlarm <= 86400l * 1000);
        assertEquals(nextAlarm.get(Calendar.HOUR_OF_DAY), dateInThePast.get(Calendar.HOUR_OF_DAY));
        assertEquals(nextAlarm.get(Calendar.MINUTE), dateInThePast.get(Calendar.MINUTE));
    }


    @Test
    public void setFirstAlarmMillisForDateInThePastAndHourMinuteInTheFutureSetsNextAlarmToday() {
        Calendar dateInThePast = new GregorianCalendar();
        dateInThePast.add(Calendar.HOUR_OF_DAY, 1);
        dateInThePast.add(Calendar.YEAR,-1);

        long nextAlarmMillis = ReminderManager.getFirstAlarmMillis(dateInThePast.getTimeInMillis());

        Calendar today = new GregorianCalendar();
        Calendar nextAlarm = new GregorianCalendar();
        nextAlarm.setTimeInMillis(nextAlarmMillis);

        long millisToNextAlarm = nextAlarmMillis - today.getTimeInMillis();
        assertTrue(millisToNextAlarm >= 0);
        assertTrue(millisToNextAlarm <= 86400l * 1000);
        assertEquals(nextAlarm.get(Calendar.HOUR_OF_DAY), dateInThePast.get(Calendar.HOUR_OF_DAY));
        assertEquals(nextAlarm.get(Calendar.MINUTE), dateInThePast.get(Calendar.MINUTE));
        assertEquals(nextAlarm.get(Calendar.DAY_OF_YEAR), today.get(Calendar.DAY_OF_YEAR));
    }


    @Test
    public void setFirstAlarmMillisForDateInTheFutureAndHourMinuteInThePastSetsNextAlarmInLessThan24Hours() {
        Calendar dateInFuture = new GregorianCalendar();
        dateInFuture.set(Calendar.HOUR_OF_DAY, 12);
        dateInFuture.set(Calendar.MINUTE, 00);
        dateInFuture.add(Calendar.DAY_OF_YEAR, 1);

        long nextAlarmMillis = ReminderManager.getFirstAlarmMillis(dateInFuture.getTimeInMillis());

        Calendar today = new GregorianCalendar();
        Calendar nextAlarm = new GregorianCalendar();
        nextAlarm.setTimeInMillis(nextAlarmMillis);

        long millisToNextAlarm = nextAlarmMillis - today.getTimeInMillis();
        assertTrue(millisToNextAlarm >= 0);
        assertTrue(millisToNextAlarm <= 86400l * 1000);
        assertEquals(nextAlarm.get(Calendar.HOUR_OF_DAY), dateInFuture.get(Calendar.HOUR_OF_DAY));
        assertEquals(nextAlarm.get(Calendar.MINUTE), dateInFuture.get(Calendar.MINUTE));
    }


    @Test
    public void setFirstAlarmMillisForDateInTheFutureAndHourMinuteInTheFutureSetsNextAlarmToday() {
        Calendar dateInFuture = new GregorianCalendar();
        dateInFuture.add(Calendar.HOUR_OF_DAY, 1);
        dateInFuture.add(Calendar.DAY_OF_YEAR, 1);

        long nextAlarmMillis = ReminderManager.getFirstAlarmMillis(dateInFuture.getTimeInMillis());

        Calendar today = new GregorianCalendar();
        Calendar nextAlarm = new GregorianCalendar();
        nextAlarm.setTimeInMillis(nextAlarmMillis);

        long millisToNextAlarm = nextAlarmMillis - today.getTimeInMillis();
        assertTrue(millisToNextAlarm >= 0);
        assertTrue(millisToNextAlarm <= 86400l * 1000);
        assertEquals(nextAlarm.get(Calendar.HOUR_OF_DAY), dateInFuture.get(Calendar.HOUR_OF_DAY));
        assertEquals(nextAlarm.get(Calendar.MINUTE), dateInFuture.get(Calendar.MINUTE));
        assertEquals(nextAlarm.get(Calendar.DAY_OF_YEAR), today.get(Calendar.DAY_OF_YEAR));
    }

}