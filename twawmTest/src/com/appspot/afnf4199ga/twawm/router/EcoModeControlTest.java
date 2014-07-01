package com.appspot.afnf4199ga.twawm.router;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

import com.appspot.afnf4199ga.utils.MyTestUtils;

public class EcoModeControlTest extends DexmakerInstrumentationTestCase {

    public void testParseContent1_enabled() {
        EcoModeControl emc = new EcoModeControl(null);
        String content = MyTestUtils.getResourceAsString("/test-data/eco_mode_main/3800_eco_enabled.htm");
        boolean success = emc.parseContent(content);

        assertEquals(true, success);
        assertEquals(null, emc.nextEcoCharge);
        assertEquals(Boolean.TRUE, emc.currentEcoCharge);
        assertEquals(
                "[ECO_TIME_MM=30, BLUETOOTH_HIBE=0, POWER_SLEEP=0, RTAP_MODE=rt, DISABLED_CHECKBOX=, CHECK_ACTION_MODE=1, SESSION_ID=42934560914750918265098142750923865, SUPLLY_SELECT=20, ECO_LED_TIME=30, ECO_MODE_SELECT=stanby]",
                emc.currentValues.toString());
    }

    public void testParseContent2_disabled() {
        EcoModeControl emc = new EcoModeControl(false);
        String content = MyTestUtils.getResourceAsString("/test-data/eco_mode_main/3800_eco_disabled.htm");
        boolean success = emc.parseContent(content);

        assertEquals(true, success);
        assertEquals(Boolean.FALSE, emc.nextEcoCharge);
        assertEquals(Boolean.FALSE, emc.currentEcoCharge);
        assertEquals(
                "[ECO_TIME_MM=10, POWER_SLEEP=0, RTAP_MODE=rt, DISABLED_CHECKBOX=, CHECK_ACTION_MODE=1, SESSION_ID=42934560914750918265098142750923865, SUPLLY_SELECT=20, ECO_LED_TIME=15, ECO_MODE_SELECT=none]",
                emc.currentValues.toString());
    }

    public void testParseContent3_error() {
        EcoModeControl emc = new EcoModeControl(null);
        boolean success = emc.parseContent("aaa");

        assertEquals(true, success);
        assertEquals(null, emc.nextEcoCharge);
        assertEquals(null, emc.currentEcoCharge);
        assertEquals(0, emc.currentValues.size());
    }

    public void testParseContent4_error() {
        EcoModeControl emc = new EcoModeControl(true);
        boolean success = emc.parseContent(null);

        assertEquals(false, success);
        assertEquals(Boolean.TRUE, emc.nextEcoCharge);
        assertEquals(null, emc.currentEcoCharge);
        assertEquals(0, emc.currentValues.size());
    }
}
