package com.appspot.afnf4199ga.twawm;

import java.util.EnumSet;
import java.util.Iterator;

import net.afnf.and.twawm2.R;
import android.content.Context;

import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class TwawmUtils {

    // リモート起動設定
    public static enum BT_RESUME_TYPE {
        FAST, NORMAL_FORCE, BT_RESTART, NORMAL;

        public static BT_RESUME_TYPE ordinalOf(int ordinal) {
            Iterator<BT_RESUME_TYPE> ite = EnumSet.allOf(BT_RESUME_TYPE.class).iterator();
            while (ite.hasNext()) {
                BT_RESUME_TYPE e = ite.next();
                if (e.ordinal() == ordinal)
                    return e;
            }
            return null;
        }
    }

    public static int getBtRetryCount(BT_RESUME_TYPE btResumeType) {
        int retry = 1; // BT_RESUME_TYPE.FAST

        if (btResumeType == BT_RESUME_TYPE.NORMAL) {
            retry = 2;
        }
        else if (btResumeType == BT_RESUME_TYPE.NORMAL_FORCE) {
            retry = 3;
        }
        else if (btResumeType == BT_RESUME_TYPE.BT_RESTART) {
            retry = 3;
        }
        return retry;
    }

    public static int getValue2ColorIndex(Context context, String color_value) {

        if (MyStringUtlis.isEmpty(color_value) == false) {
            if (color_value.equals(context.getString(R.string.color_black_value))) {
                return R.color.black;
            }
            else if (color_value.equals(context.getString(R.string.color_gray_value))) {
                return R.color.gray;
            }
            else if (color_value.equals(context.getString(R.string.color_white_value))) {
                return R.color.white;
            }
            else if (color_value.equals(context.getString(R.string.color_red_value))) {
                return R.color.red;
            }
            else if (color_value.equals(context.getString(R.string.color_green_value))) {
                return R.color.green;
            }
            else if (color_value.equals(context.getString(R.string.color_blue_value))) {
                return R.color.blue;
            }
            else if (color_value.equals(context.getString(R.string.color_none_value))) {
                return R.color.full_transparent;
            }
        }

        return R.color.red;
    }

    static int[] R_BLACK = { R.drawable.backbround_black, R.drawable.backbround_white };
    static int[] R_BLACK_TRANS = { R.drawable.backbround_black_trans, R.drawable.backbround_white_trans };
    static int[] R_WHITE = { R.drawable.backbround_white, R.drawable.backbround_black };
    static int[] R_WHITE_TRANS = { R.drawable.backbround_white_trans, R.drawable.backbround_black_trans };
    static int[] R_NONE = { 0, R.drawable.backbround_white_trans };

    public static int[] getValue2ResourceIndex(Context context, String color_value) {

        if (MyStringUtlis.isEmpty(color_value) == false) {
            if (color_value.equals(context.getString(R.string.color_black_value))) {
                return R_BLACK;
            }
            else if (color_value.equals(context.getString(R.string.color_black_trans_value))) {
                return R_BLACK_TRANS;
            }
            else if (color_value.equals(context.getString(R.string.color_white_value))) {
                return R_WHITE;
            }
            else if (color_value.equals(context.getString(R.string.color_white_trans_value))) {
                return R_WHITE_TRANS;
            }
            else if (color_value.equals(context.getString(R.string.color_none_value))) {
                return R_NONE; // 「0 to remove the background」らしい
            }
        }

        return R_BLACK_TRANS;
    }
}
