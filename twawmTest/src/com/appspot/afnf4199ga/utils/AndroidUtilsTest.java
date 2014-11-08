package com.appspot.afnf4199ga.utils;

import java.util.HashMap;
import java.util.Map;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AndroidUtilsTest extends DexmakerInstrumentationTestCase {

    public void testIndexOf() {
        assertEquals(-1, AndroidUtils.indexOf(null, null));
        assertEquals(-1, AndroidUtils.indexOf(null, ""));
        assertEquals(-1, AndroidUtils.indexOf(null, "a"));

        String[] data1 = {};
        assertEquals(-1, AndroidUtils.indexOf(data1, null));
        assertEquals(-1, AndroidUtils.indexOf(data1, ""));
        assertEquals(-1, AndroidUtils.indexOf(data1, "a"));

        String[] data2 = { null, null, null };
        assertEquals(-1, AndroidUtils.indexOf(data2, null));
        assertEquals(-1, AndroidUtils.indexOf(data2, ""));
        assertEquals(-1, AndroidUtils.indexOf(data2, "a"));

        String[] data3 = { null, "", "aa", "a", null, "aa" };
        assertEquals(-1, AndroidUtils.indexOf(data3, null));
        assertEquals(1, AndroidUtils.indexOf(data3, ""));
        assertEquals(2, AndroidUtils.indexOf(data3, "aa"));
        assertEquals(3, AndroidUtils.indexOf(data3, "a"));
    }

    public void testIntToIpaddr() {
        assertEquals("0.0.0.0", AndroidUtils.intToIpaddr(0));
        assertEquals("1.0.0.0", AndroidUtils.intToIpaddr(1));
        assertEquals("255.255.255.0", AndroidUtils.intToIpaddr(0x00ffffff));
        assertEquals("255.255.0.255", AndroidUtils.intToIpaddr(0xff00ffff));
        assertEquals("255.0.255.255", AndroidUtils.intToIpaddr(0xffff00ff));
        assertEquals("0.255.255.255", AndroidUtils.intToIpaddr(0xffffff00));
        assertEquals("255.255.255.255", AndroidUtils.intToIpaddr(0xffffffff));
    }

    public void testGetIntFromEditPref11() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                mockMap.put("key1", "11");
                return mockMap;
            }
        });
        // 実行
        int value = AndroidUtils.getPrefInt(mock, "key1", "21");
        assertEquals(11, value);
    }

    public void testGetIntFromEditPref12() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                mockMap.put("key1", "12");
                return mockMap;
            }
        });
        // 実行
        int value = AndroidUtils.getPrefInt(mock, "key1", "22");
        assertEquals(12, value);
    }

    public void testGetIntFromEditPref21() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                return mockMap;
            }
        });
        // 実行
        int value = AndroidUtils.getPrefInt(mock, "key1", "21");
        assertEquals(21, value);
    }

    public void testGetIntFromEditPref22() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                return mockMap;
            }
        });
        // 実行
        int value = AndroidUtils.getPrefInt(mock, "key1", "22");
        assertEquals(22, value);
    }

    public void testGetIntFromEditPref31() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                mockMap.put("key1", "");
                return mockMap;
            }
        });
        // 実行
        int value = AndroidUtils.getPrefInt(mock, "key1", "31");
        assertEquals(31, value);
    }

    public void testGetIntFromEditPref32() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                mockMap.put("key1", "aaa");
                return mockMap;
            }
        });
        // 実行
        int value = AndroidUtils.getPrefInt(mock, "key1", "32");
        assertEquals(32, value);
    }

    public void testGetPrefValue_s1() {

        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, String> mockMap = new HashMap<String, String>();
                mockMap.put("key1", "value1");
                mockMap.put("key2", "value2");
                mockMap.put("key3", "");
                mockMap.put("key4", null);
                return mockMap;
            }
        });

        assertEquals("value1", AndroidUtils.getPrefString(mock, "key1"));
        assertEquals("value2", AndroidUtils.getPrefString(mock, "key2"));
        assertNull(AndroidUtils.getPrefString(mock, "key3"));
        assertNull(AndroidUtils.getPrefString(mock, "key4"));
        assertNull(AndroidUtils.getPrefString(mock, "key5"));
    }

    public void testGetPrefValue_b1() {

        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.getAll()).thenAnswer(new Answer<Map<String, ?>>() {
            public Map<String, ?> answer(InvocationOnMock a) {
                HashMap<String, Boolean> mockMap = new HashMap<String, Boolean>();
                mockMap.put("key1", Boolean.TRUE);
                mockMap.put("key2", Boolean.FALSE);
                mockMap.put("key3", Boolean.FALSE);
                mockMap.put("key4", null);
                return mockMap;
            }
        });

        assertEquals(Boolean.TRUE, AndroidUtils.getPrefBoolean(mock, "key1"));
        assertEquals(Boolean.FALSE, AndroidUtils.getPrefBoolean(mock, "key2"));
        assertEquals(Boolean.FALSE, AndroidUtils.getPrefBoolean(mock, "key3"));
        assertNull(AndroidUtils.getPrefBoolean(mock, "key4"));
        assertNull(AndroidUtils.getPrefBoolean(mock, "key5"));
    }

    public void testUpdatePrefValueByModel_s1() {
        Editor mockEditor = Mockito.mock(Editor.class);
        Mockito.when(mockEditor.putString("key1", "v11")).thenReturn(mockEditor);
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.edit()).thenReturn(mockEditor);

        Object value = AndroidUtils.updatePrefValueByModel(mock, "key1", "m1", "#M1#M2#M3#", "v11", "v12");
        assertEquals("v11", value);

        // 呼び出し有り
        Mockito.verify(mock).edit();
        Mockito.verify(mockEditor).putString("key1", "v11");
        Mockito.verify(mockEditor).commit();

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
        Mockito.verifyNoMoreInteractions(mockEditor);
    }

    public void testUpdatePrefValueByModel_s2() {
        Editor mockEditor = Mockito.mock(Editor.class);
        Mockito.when(mockEditor.putString("key1", "v12")).thenReturn(mockEditor);
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.edit()).thenReturn(mockEditor);

        Object value = AndroidUtils.updatePrefValueByModel(mock, "key1", "xx", "#M1#M2#M3#", "v11", "v12");
        assertEquals("v12", value);

        // 呼び出し有り
        Mockito.verify(mock).edit();
        Mockito.verify(mockEditor).putString("key1", "v12");
        Mockito.verify(mockEditor).commit();

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
        Mockito.verifyNoMoreInteractions(mockEditor);
    }

    public void testUpdatePrefValueByModel_s3() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);

        Object value = AndroidUtils.updatePrefValueByModel(mock, "key1", "xx", "#M1#M2#M3#", "v11", null);
        assertNull(value);

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
    }

    public void testUpdatePrefValueByModel_b1() {
        Editor mockEditor = Mockito.mock(Editor.class);
        Mockito.when(mockEditor.putBoolean("key1", Boolean.TRUE)).thenReturn(mockEditor);
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.edit()).thenReturn(mockEditor);

        Object value = AndroidUtils.updatePrefValueByModel(mock, "key1", "m1", "#M1#M2#M3#", Boolean.TRUE, Boolean.FALSE);
        assertEquals(Boolean.TRUE, value);

        // 呼び出し有り
        Mockito.verify(mock).edit();
        Mockito.verify(mockEditor).putBoolean("key1", Boolean.TRUE);
        Mockito.verify(mockEditor).commit();

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
        Mockito.verifyNoMoreInteractions(mockEditor);
    }

    public void testUpdatePrefValueByModel_b2() {
        Editor mockEditor = Mockito.mock(Editor.class);
        Mockito.when(mockEditor.putBoolean("key1", Boolean.FALSE)).thenReturn(mockEditor);
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);
        Mockito.when(mock.edit()).thenReturn(mockEditor);

        Object value = AndroidUtils.updatePrefValueByModel(mock, "key1", "xx", "#M1#M2#M3#", Boolean.TRUE, Boolean.FALSE);
        assertEquals(Boolean.FALSE, value);

        // 呼び出し有り
        Mockito.verify(mock).edit();
        Mockito.verify(mockEditor).putBoolean("key1", Boolean.FALSE);
        Mockito.verify(mockEditor).commit();

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
        Mockito.verifyNoMoreInteractions(mockEditor);
    }

    public void testUpdatePrefValueByModel_b3() {
        SharedPreferences mock = Mockito.mock(SharedPreferences.class);

        Object value = AndroidUtils.updatePrefValueByModel(mock, "key1", "xx", "#M1#M2#M3#", Boolean.TRUE, null);
        assertNull(value);

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
    }
}
