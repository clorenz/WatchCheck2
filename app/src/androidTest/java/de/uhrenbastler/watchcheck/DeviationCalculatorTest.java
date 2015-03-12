package de.uhrenbastler.watchcheck;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import watchcheck.db.Log;

public class DeviationCalculatorTest extends TestCase {

    public void testEmptyDeviations() {
        DeviationCalculator calc = new DeviationCalculator(new ArrayList<Log>());

        assertTrue(calc.getDeviations().isEmpty());
    }

    // One single log can never return a deviation
    public void testDeviationsOneLogOnePositionReturnsEmptyList() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L, 0, 1000, DeviationCalculator.ZO));
        DeviationCalculator calc = new DeviationCalculator(logs);
        assertTrue(calc.getDeviations().isEmpty());
    }

    // Two logs will return one single deviation
    public void testDeviationsTwoLogsOnePosition() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),1,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO));
    }

    // Three logs of the same position will return one single deviation
    public void testDeviationsThreeLogsOnePosition() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000*2,2000,DeviationCalculator.ZO));
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),1,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO));
    }

    private Log createLog(Long id, long refTime, long deviationInMillis, String position) {
        Date ref = new Date(refTime);
        Date watch = new Date(refTime + deviationInMillis);
        return new Log(id,0,0,ref,watch,position,null,null);
    }
}