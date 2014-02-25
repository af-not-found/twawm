package com.appspot.afnf4199ga.twawm.ctl;

public class ListItem {

	public char value;

	public String label;

	public boolean checked;

	public ListItem(char value, String label, boolean checked) {
		this.value = value;
		this.label = label;
		this.checked = checked;
	}

	@Override
	public String toString() {
		return value + "," + label + "," + (checked ? 1 : 0);
	}

	public int getIndex() {
		return value - 'a';
	}
}
