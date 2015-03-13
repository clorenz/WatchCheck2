package de.uhrenbastler.watchcheck;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import watchcheck.db.Log;

import static org.junit.Assert.*;

public class DeviationCalculatorTest {

    public static final double EPSILON = 0.001;

    @Test
    public void testEmptyDeviations() {
        DeviationCalculator calc = new DeviationCalculator(new ArrayList<Log>());

        assertTrue(calc.getDeviations().isEmpty());
    }

    // One single log can never return a deviation
    @Test
    public void testDeviationsOneLogOnePositionReturnsEmptyList() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L, 0, 1000, DeviationCalculator.ZO));
        DeviationCalculator calc = new DeviationCalculator(logs);
        assertTrue(calc.getDeviations().isEmpty());
    }

    // Two logs will return one single deviation
    @Test
    public void testDeviationsTwoLogsOnePosition() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),1,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO), EPSILON);
    }

    // Three logs of the same position will return one single deviation
    @Test
    public void testDeviationsThreeLogsOnePosition() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000*2,2000,DeviationCalculator.ZO));
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),1,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO),EPSILON);
    }


    // ------------------------------------------ helpers ---------------------------------
    private Log createLog(Long id, long refTime, long deviationInMillis, String position) {
        Date ref = new Date(refTime);
        Date watch = new Date(refTime + deviationInMillis);
        return new Log(id,0,0,ref,watch,position,null,null);
    }
}