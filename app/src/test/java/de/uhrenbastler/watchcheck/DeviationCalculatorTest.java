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

    // Three logs of the same position will return one single deviation
    @Test
    public void testDeviationsThreeLogsOnePositionDifferentDeviations() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));       // day 1: 1 second
        logs.add(createLog(2L,86400000*10,2000,DeviationCalculator.ZO));    // day 10: 2 seconds
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),1,calc.getDeviations().size());
        assertEquals(0.2d, calc.getDeviations().get(DeviationCalculator.ZO),EPSILON);
    }


    @Test
    public void testDeviationsThreeLogsTwoPosition() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));   // Day 1: 1 second per day in Dial up
        logs.add(createLog(2L,86400000*2,3000,DeviationCalculator.ZU));   // Day 2: 2 seconds per day in Dial Down
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),2,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO), EPSILON);
        assertEquals(2d, calc.getDeviations().get(DeviationCalculator.ZU), EPSILON);
    }


    @Test
    public void testDeviationsFourLogsTwoPositions() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));   // Day 1: 1 second per day in Dial up
        logs.add(createLog(2L,86400000*2,3000,DeviationCalculator.ZU));   // Day 2: 2 seconds per day in Dial Down
        logs.add(createLog(3L,86400000*3,4000,DeviationCalculator.ZO)); // Day 3: 1 second in Dial up
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),2,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO), EPSILON);
        assertEquals(2d, calc.getDeviations().get(DeviationCalculator.ZU), EPSILON);
    }


    @Test
    public void testDeviationsFiveLogsTwoPositions() {
        ArrayList<Log> logs = new ArrayList<Log>();
        logs.add(createLog(0L,0,0,DeviationCalculator.ZO));
        logs.add(createLog(1L,86400000,1000,DeviationCalculator.ZO));   // Day 1: 1 second per day in Dial up
        logs.add(createLog(2L,86400000*2,3000,DeviationCalculator.ZU));   // Day 2: 2 seconds per day in Dial Down
        logs.add(createLog(3L,86400000*3,4000,DeviationCalculator.ZO)); // Day 3: 1 second in Dial up
        logs.add(createLog(4L,86400000*4,8000,DeviationCalculator.ZU)); // Day 4: 4 seconds in Dial down
        DeviationCalculator calc = new DeviationCalculator(logs);

        assertEquals("Deviations="+calc.getDeviations(),2,calc.getDeviations().size());
        assertEquals(1d, calc.getDeviations().get(DeviationCalculator.ZO), EPSILON);
        assertEquals(3d, calc.getDeviations().get(DeviationCalculator.ZU), EPSILON);
    }


    // ------------------------------------------ helpers ---------------------------------
    private Log createLog(Long id, long refTime, long deviationInMillis, String position) {
        Date ref = new Date(refTime);
        Date watch = new Date(refTime + deviationInMillis);
        return new Log(id,0,0,ref,watch,position,null,null);
    }
}