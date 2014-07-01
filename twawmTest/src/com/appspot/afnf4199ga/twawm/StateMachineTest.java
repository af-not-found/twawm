package com.appspot.afnf4199ga.twawm;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class StateMachineTest extends DexmakerInstrumentationTestCase {

    public void testCreateRemainText() {
        assertEquals("", StateMachine.createRemainText(0, 0));
        assertEquals("", StateMachine.createRemainText(0, 1));

        assertEquals("", StateMachine.createRemainText(1, 0));
        assertEquals("(60m)", StateMachine.createRemainText(1, 1));
        assertEquals("(5m)", StateMachine.createRemainText(1, 11));
        assertEquals("(3m)", StateMachine.createRemainText(1, 22));

        assertEquals("(90m)", StateMachine.createRemainText(15, 10));
        assertEquals("(1.6h)", StateMachine.createRemainText(16, 10));
        assertEquals("(1.7h)", StateMachine.createRemainText(17, 10));
        assertEquals("(1.8h)", StateMachine.createRemainText(18, 10));
        assertEquals("(1.9h)", StateMachine.createRemainText(19, 10));

        assertEquals("(50h)", StateMachine.createRemainText(50, 1));
        assertEquals("(4.5h)", StateMachine.createRemainText(50, 11));
        assertEquals("(2.3h)", StateMachine.createRemainText(50, 22));

        assertEquals("(9.5h)", StateMachine.createRemainText(60, 6.3));
        assertEquals("(9.7h)", StateMachine.createRemainText(61, 6.3));
        assertEquals("(9.8h)", StateMachine.createRemainText(62, 6.3));
        assertEquals("(10h)", StateMachine.createRemainText(63, 6.3));
        assertEquals("(10h)", StateMachine.createRemainText(64, 6.3));
        assertEquals("(10h)", StateMachine.createRemainText(65, 6.3));

        assertEquals("(92m)", StateMachine.createRemainText(60, 39));
        assertEquals("(94m)", StateMachine.createRemainText(61, 39));
        assertEquals("(95m)", StateMachine.createRemainText(62, 39));
        assertEquals("(1.6h)", StateMachine.createRemainText(63, 39));
        assertEquals("(1.6h)", StateMachine.createRemainText(64, 39));
        assertEquals("(1.7h)", StateMachine.createRemainText(65, 39));

        assertEquals("(99h)", StateMachine.createRemainText(100, 1));
        assertEquals("(9.1h)", StateMachine.createRemainText(100, 11));
        assertEquals("(4.5h)", StateMachine.createRemainText(100, 22));
    }

    public void testBattRate1() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000;
        sm.prevUpdateTime = now;

        sm.updateAverage(null);
        assertEquals(0.0, sm.averageRate);

        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(0.0, sm.averageRate);

        now += deltaTime;
        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(0.0, sm.averageRate);

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(0.0, sm.averageRate);

        now += deltaTime;
        sm.updateBattCalc(48, now, null);
        assertEquals(200, (int) (sm.currentRate * 10));
        assertEquals(200, (int) (sm.averageRate * 10));

        now += deltaTime * 2;
        sm.updateBattCalc(46, now, null);
        assertEquals(200, (int) (sm.currentRate * 10));
        assertEquals(200, (int) (sm.averageRate * 10));
    }

    public void testBattRate2() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000;
        sm.prevUpdateTime = now;

        sm.pastRates.add(Double.valueOf(20));
        sm.pastRates.add(Double.valueOf(20));
        sm.pastRates.add(Double.valueOf(20));
        sm.updateAverage(null);
        assertEquals(200, (int) (sm.averageRate * 10));

        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(200, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(200, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(48, now, null);
        assertEquals(200, (int) (sm.currentRate * 10));
        assertEquals(200, (int) (sm.averageRate * 10));

        now += deltaTime * 2;
        sm.updateBattCalc(46, now, null);
        assertEquals(200, (int) (sm.currentRate * 10));
        assertEquals(200, (int) (sm.averageRate * 10));
    }

    public void testBattRate3() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000;
        sm.prevUpdateTime = now;

        sm.pastRates.add(Double.valueOf(10));
        sm.pastRates.add(Double.valueOf(15));
        sm.pastRates.add(Double.valueOf(20));
        sm.updateAverage(null);
        assertEquals(150, (int) (sm.averageRate * 10));

        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        now += deltaTime * 6;
        sm.updateBattCalc(46, now, null);
        assertEquals(100, (int) (sm.currentRate * 10));
        assertEquals(137, (int) (sm.averageRate * 10));

        now += deltaTime * 6;
        sm.updateBattCalc(45, now, null);
        assertEquals(66, (int) (sm.currentRate * 10));
        assertEquals(123, (int) (sm.averageRate * 10));

        now += deltaTime * 3;
        sm.updateBattCalc(41, now, null);
        assertEquals(106, (int) (sm.currentRate * 10));
        assertEquals(120, (int) (sm.averageRate * 10));
    }

    public void testBattRate4() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000;
        sm.prevUpdateTime = now;

        sm.pastRates.add(Double.valueOf(10));
        sm.pastRates.add(Double.valueOf(15));
        sm.pastRates.add(Double.valueOf(20));
        sm.updateAverage(null);
        assertEquals(150, (int) (sm.averageRate * 10));

        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        now += deltaTime * 2;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));
    }

    public void testBattRateFast() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000; // ここだけ1分
        sm.prevUpdateTime = now;

        sm.pastRates.add(Double.valueOf(10));
        sm.pastRates.add(Double.valueOf(15));
        sm.pastRates.add(Double.valueOf(20));
        sm.updateAverage(null);
        assertEquals(150, (int) (sm.averageRate * 10));

        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(150, (int) (sm.averageRate * 10));

        long d;
        d = (long) (deltaTime * 0.78);
        sm.updateBattCalc(48, now + d, null);

        assertEquals(256, (int) (sm.currentRate * 10));
        assertEquals(150, (int) (sm.averageRate * 10));

        d = (long) (deltaTime * 0.79);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(253, (int) (sm.currentRate * 10));
        assertEquals(150, (int) (sm.averageRate * 10));

        d = (long) (deltaTime * 0.80);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(250, (int) (sm.currentRate * 10));
        assertEquals(175, (int) (sm.averageRate * 10));

        d = (long) (deltaTime * 0.81);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(246, (int) (sm.currentRate * 10));
        assertEquals(174, (int) (sm.averageRate * 10));
    }

    public void testBattRateSlow() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000;
        sm.prevUpdateTime = now;

        sm.pastRates.add(Double.valueOf(3));
        sm.pastRates.add(Double.valueOf(7));
        sm.pastRates.add(Double.valueOf(5));
        sm.updateAverage(null);
        assertEquals(50, (int) (sm.averageRate * 10));

        sm.updateBattCalc(50, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(50, (int) (sm.averageRate * 10));

        now += deltaTime;
        sm.updateBattCalc(49, now, null);
        assertEquals(-1.0, sm.currentRate);
        assertEquals(50, (int) (sm.averageRate * 10));

        long d;
        d = (long) (deltaTime * 5.2);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(384, (int) (sm.currentRate * 100)); // ここだけ100倍
        assertEquals(500, (int) (sm.averageRate * 100)); // ここだけ100倍

        d = (long) (deltaTime * 5.1);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(392, (int) (sm.currentRate * 100)); // ここだけ100倍
        assertEquals(500, (int) (sm.averageRate * 100)); // ここだけ100倍

        d = (long) (deltaTime * 5.0);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(400, (int) (sm.currentRate * 100)); // ここだけ100倍
        assertEquals(475, (int) (sm.averageRate * 100)); // ここだけ100倍

        d = (long) (deltaTime * 4.9);
        sm.updateBattCalc(48, now + d, null);
        assertEquals(408, (int) (sm.currentRate * 100)); // ここだけ100倍
        assertEquals(477, (int) (sm.averageRate * 100)); // ここだけ100倍
    }

    public void testBattRateStartStop() {
        StateMachine sm = new StateMachine();
        long now = System.currentTimeMillis();
        long deltaTime = 3 * 60 * 1000;
        sm.prevUpdateTime = now;

        sm.pastRates.add(Double.valueOf(30));
        sm.pastRates.add(Double.valueOf(10));
        sm.pastRates.add(Double.valueOf(25));
        sm.pastRates.add(Double.valueOf(15));
        assertEquals("30000,10000,25000,15000,", MyStringUtlis.toString(sm.pastRates));

        sm.startBattCalc(null);
        assertEquals(4, sm.pastRates.size());

        now += deltaTime;
        sm.updateBattCalc(50, now, null);

        now += deltaTime;
        sm.updateBattCalc(49, now, null);

        now += deltaTime;
        sm.updateBattCalc(48, now, null);
        assertEquals(200, (int) (sm.currentRate * 10));

        assertEquals(4, sm.pastRates.size());
        assertEquals("30000,10000,25000,15000,", MyStringUtlis.toString(sm.pastRates));
        sm.stopBattCalc(null, now);
        assertEquals(5, sm.pastRates.size());
        assertEquals("30000,10000,25000,15000,20000,", MyStringUtlis.toString(sm.pastRates));

        sm.startBattCalc(null);
        assertEquals(5, sm.pastRates.size());

        now += deltaTime;
        sm.updateBattCalc(46, now, null);

        now += deltaTime;
        sm.updateBattCalc(45, now, null);

        now += deltaTime;
        sm.updateBattCalc(44, now, null);

        assertEquals(5, sm.pastRates.size());
        assertEquals("30000,10000,25000,15000,20000,", MyStringUtlis.toString(sm.pastRates));
        sm.stopBattCalc(null, now);
        assertEquals(5, sm.pastRates.size());
        assertEquals("10000,25000,15000,20000,20000,", MyStringUtlis.toString(sm.pastRates));

        sm.startBattCalc(null);
        assertEquals(5, sm.pastRates.size());

        now += deltaTime;
        sm.updateBattCalc(43, now, null);

        now += deltaTime;
        sm.updateBattCalc(42, now, null);

        now += deltaTime;
        sm.updateBattCalc(41, now, null);

        assertEquals(5, sm.pastRates.size());
        assertEquals("10000,25000,15000,20000,20000,", MyStringUtlis.toString(sm.pastRates));
        sm.savePastRates(null, now);
        assertEquals(5, sm.pastRates.size());
        assertEquals("25000,15000,20000,20000,20000,", MyStringUtlis.toString(sm.pastRates));

        now += deltaTime;
        sm.updateBattCalc(40, now, null);

        assertEquals(5, sm.pastRates.size());
        assertEquals("25000,15000,20000,20000,20000,", MyStringUtlis.toString(sm.pastRates));
        sm.savePastRates(null, now);
        assertEquals(5, sm.pastRates.size());
        assertEquals("15000,20000,20000,20000,20000,", MyStringUtlis.toString(sm.pastRates));

        now += deltaTime;
        sm.updateBattCalc(39, now, null);

        now += deltaTime;
        sm.updateBattCalc(38, now, null);

        now += deltaTime;
        sm.updateBattCalc(37, now, null);

        now += deltaTime;
        sm.updateBattCalc(36, now, null);

        now += deltaTime;
        sm.updateBattCalc(35, now, null);

        assertEquals(5, sm.pastRates.size());
        assertEquals("15000,20000,20000,20000,20000,", MyStringUtlis.toString(sm.pastRates));

        now += deltaTime;
        sm.updateBattCalc(34, now, null);

        assertEquals(5, sm.pastRates.size());
        assertEquals("20000,20000,20000,20000,20000,", MyStringUtlis.toString(sm.pastRates));
    }
}
