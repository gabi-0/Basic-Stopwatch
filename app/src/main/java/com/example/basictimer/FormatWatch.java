package com.example.basictimer;

import java.util.Locale;

public class FormatWatch {

    public static String timestampAccurateFormat(long dif) {
        dif /= 10;
        long mil = dif%100;
        dif /= 100;
        return String.format(Locale.US, "%02d:%02d.%02d", dif/60, dif%60, mil);
    }

    public static String timestampFormat(long dif) {
        dif /= 1000;
        return String.format(Locale.US, "%02d:%02d", dif/60, dif%60);
    }
}
