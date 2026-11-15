package com.example.myapplication;

import java.util.Calendar;
import java.util.Locale;

public class DateUtil {
    private static final String[] MONTHS = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    public static int of(int year, int month1to12, int day) { return year * 10000 + month1to12 * 100 + day; }

    public static int today() { return offset(0); }

    public static int offset(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, days);
        return of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }
    public static String formatMillis(long ms) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(ms);
        return format(of(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1,
                c.get(java.util.Calendar.DAY_OF_MONTH)));
    }

    public static String format(int ymd) {
        if (ymd == 0) return "No due date";
        return (ymd % 100) + " " + MONTHS[(ymd / 100) % 100 - 1] + " " + (ymd / 10000);
    }

    public static String formatMDY(int ymd) {
        return String.format(Locale.US, "%02d/%02d/%04d", (ymd / 100) % 100, ymd % 100, ymd / 10000);
    }
}