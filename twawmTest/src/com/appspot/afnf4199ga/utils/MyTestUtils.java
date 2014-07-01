package com.appspot.afnf4199ga.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.appspot.afnf4199ga.twawm.Const;

public class MyTestUtils {

    public static String getResourceAsString(String path) {
        return getResourceAsString(path, true);
    }

    public static String getResourceAsString(String path, boolean eucjp) {
        InputStream resourceAsStream = MyTestUtils.class.getResourceAsStream(path);
        String content = null;
        try {
            content = IOUtils.toString(resourceAsStream, eucjp ? Const.ROUTER_PAGE_CHARSET_WM : Const.ROUTER_PAGE_CHARSET_NAD);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
