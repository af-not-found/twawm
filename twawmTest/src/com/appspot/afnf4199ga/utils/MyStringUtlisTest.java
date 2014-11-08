package com.appspot.afnf4199ga.utils;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

public class MyStringUtlisTest extends DexmakerInstrumentationTestCase {

    public void testIsEmpty() {
        assertTrue(MyStringUtlis.isEmpty(null));
        assertTrue(MyStringUtlis.isEmpty(""));
        assertFalse(MyStringUtlis.isEmpty(" "));
        assertFalse(MyStringUtlis.isEmpty("a"));
    }

    public void testEqauls() {
        assertTrue(MyStringUtlis.eqauls("", ""));
        assertTrue(MyStringUtlis.eqauls("aa", "aa"));
        assertTrue(MyStringUtlis.eqauls(null, ""));
        assertTrue(MyStringUtlis.eqauls(null, null));
        assertTrue(MyStringUtlis.eqauls("", ""));
        assertFalse(MyStringUtlis.eqauls(null, "cc"));
        assertFalse(MyStringUtlis.eqauls("bb", ""));
        assertFalse(MyStringUtlis.eqauls("bb", "cc"));
    }

    public void testNormalize() {
        assertEquals("", MyStringUtlis.normalize(null));
        assertEquals("", MyStringUtlis.normalize(""));
        assertEquals("", MyStringUtlis.normalize(" "));
        assertEquals("a", MyStringUtlis.normalize(" a"));
        assertEquals("", MyStringUtlis.normalize("     "));
        assertEquals(" ", MyStringUtlis.normalize("　   "));
        assertEquals("Aterm WM3600R", MyStringUtlis.normalize("   A t e r m　W M 3 6 0 0 R"));
    }

    public void testCount() {
        assertEquals(0, MyStringUtlis.count(null, 'a'));
        assertEquals(0, MyStringUtlis.count("aaa", ' '));
        assertEquals(2, MyStringUtlis.count("aa", 'a'));
        assertEquals(6, MyStringUtlis.count("■■■■■■□□□□］　", '■'));
    }

    public void testSubStringBefore() {
        assertEquals("", MyStringUtlis.subStringBefore(null, "("));
        assertEquals("", MyStringUtlis.subStringBefore("", "("));
        assertEquals(" ", MyStringUtlis.subStringBefore(" ", "("));
        assertEquals("aaa ", MyStringUtlis.subStringBefore("aaa (bb)", "("));
        assertEquals("aaa ", MyStringUtlis.subStringBefore("aaa ", "("));
        assertEquals(" aa ", MyStringUtlis.subStringBefore(" aa ", "("));
    }

    public void testTrimQuote(String str) {
        assertEquals("", MyStringUtlis.trimQuote(""));
        assertEquals(null, MyStringUtlis.trimQuote(null));
        assertEquals("", MyStringUtlis.trimQuote("\"\""));
        assertEquals("aaaa", MyStringUtlis.trimQuote("\"aaaa\""));
        assertEquals("\"aaaa\"", MyStringUtlis.trimQuote("\"\"aaaa\"\""));
    }
}
