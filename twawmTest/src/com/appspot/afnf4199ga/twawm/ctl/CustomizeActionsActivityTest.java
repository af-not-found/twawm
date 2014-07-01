package com.appspot.afnf4199ga.twawm.ctl;

import java.util.ArrayList;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

public class CustomizeActionsActivityTest extends DexmakerInstrumentationTestCase {

	public void testConstructListItemArrayFromCustomizedData_01() {

		String[] labels = { "label1", "label2", "label3", "label4", "label5" };
		String[] customizedDatas = { null, "", "a1,", "a1,b1,", "a1,b1,c1,d1,e1,f1,", "A1,", "zzzzzzzzzzzzzzz" };

		for (String customizedData : customizedDatas) {
			ArrayList<ListItem> array = CustomizeActionsActivity.constructListItemArrayFromCustomizedData(labels, customizedData);
			assertEquals(5, array.size());
			int i = 0;
			assertEquals("a,label1,1", array.get(i++).toString());
			assertEquals("b,label2,1", array.get(i++).toString());
			assertEquals("c,label3,1", array.get(i++).toString());
			assertEquals("d,label4,1", array.get(i++).toString());
			assertEquals("e,label5,1", array.get(i++).toString());
		}
	}

	public void testConstructListItemArrayFromCustomizedData_02() {

		String[] labels = { "label1", "label2", "label3", "label4", "label5" };
		String[] customizedDatas = { "a1,b0,", "a1,b0,c1,d1,e1,f1," };

		for (String customizedData : customizedDatas) {
			ArrayList<ListItem> array = CustomizeActionsActivity.constructListItemArrayFromCustomizedData(labels, customizedData);
			assertEquals(5, array.size());
			int i = 0;
			assertEquals("a,label1,1", array.get(i++).toString());
			assertEquals("b,label2,0", array.get(i++).toString());
			assertEquals("c,label3,1", array.get(i++).toString());
			assertEquals("d,label4,1", array.get(i++).toString());
			assertEquals("e,label5,1", array.get(i++).toString());
		}
	}

	public void testConstructListItemArrayFromCustomizedData_03() {

		String[] labels = { "label1", "label2", "label3", "label4", "label5" };
		String[] customizedDatas = { "a1,c0,b1", "a1,c0,b1,d1,e1" };

		for (String customizedData : customizedDatas) {
			ArrayList<ListItem> array = CustomizeActionsActivity.constructListItemArrayFromCustomizedData(labels, customizedData);
			assertEquals(5, array.size());
			int i = 0;
			assertEquals("a,label1,1", array.get(i++).toString());
			assertEquals("c,label3,0", array.get(i++).toString());
			assertEquals("b,label2,1", array.get(i++).toString());
			assertEquals("d,label4,1", array.get(i++).toString());
			assertEquals("e,label5,1", array.get(i++).toString());
		}
	}

	public void testConstructListItemArrayFromCustomizedData_04() {

		String[] labels = { "label1", "label2", "label3", "label4", "label5" };
		String[] customizedDatas = { "c1,a1,b0", "c1,a1,b0,d1,e1" };

		for (String customizedData : customizedDatas) {
			ArrayList<ListItem> array = CustomizeActionsActivity.constructListItemArrayFromCustomizedData(labels, customizedData);
			assertEquals(5, array.size());
			int i = 0;
			assertEquals("c,label3,1", array.get(i++).toString());
			assertEquals("a,label1,1", array.get(i++).toString());
			assertEquals("b,label2,0", array.get(i++).toString());
			assertEquals("d,label4,1", array.get(i++).toString());
			assertEquals("e,label5,1", array.get(i++).toString());
		}
	}

	public void testConstructListItemArrayFromCustomizedData_05() {

		String[] labels = { "label1", "label2", "label3", "label4", "label5" };
		String[] customizedDatas = { "e0,a1,b0,d1,c1" };

		for (String customizedData : customizedDatas) {
			ArrayList<ListItem> array = CustomizeActionsActivity.constructListItemArrayFromCustomizedData(labels, customizedData);
			assertEquals(5, array.size());
			int i = 0;
			assertEquals("e,label5,0", array.get(i++).toString());
			assertEquals("a,label1,1", array.get(i++).toString());
			assertEquals("b,label2,0", array.get(i++).toString());
			assertEquals("d,label4,1", array.get(i++).toString());
			assertEquals("c,label3,1", array.get(i++).toString());
		}
	}
}
