package com.appspot.afnf4199ga.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.appspot.afnf4199ga.twawm.Const;

public class MyTestUtils {

	public static String getResourceAsString(String path) {
		InputStream resourceAsStream = MyTestUtils.class.getResourceAsStream(path);
		String content = null;
		try {
			content = IOUtils.toString(resourceAsStream, Const.ROUTER_PAGE_CHARSET);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
}
