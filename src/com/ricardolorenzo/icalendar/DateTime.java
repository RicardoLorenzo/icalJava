/*
 * DateTime class
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Ricardo Lorenzo <unshakablespirit@gmail.com>
 * 
 */
package com.ricardolorenzo.icalendar;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Ricardo_Lorenzo
 *
 */
public class DateTime {

    public DateTime() {
    }

    public static final Calendar getCalendar(final long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        return date;
    }

    public static final Calendar getCalendarFromString(final TimeZone tz, String value) {
        Calendar date = Calendar.getInstance();
        if (tz != null && !value.endsWith("Z")) {
            date.setTimeZone(tz);
        } else {
            date.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        date.set(Calendar.YEAR, Integer.parseInt(value.substring(0, 4)));
        date.set(Calendar.MONTH, Integer.parseInt(value.substring(4, 6)) - 1);
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(value.substring(6, 8)));
        if (value.indexOf("T") > 0) {
            value = value.substring(value.indexOf("T") + 1);
            date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(value.substring(0, 2)));
            date.set(Calendar.MINUTE, Integer.parseInt(value.substring(2, 4)));
            if (value.length() > 4) {
                date.set(Calendar.SECOND, Integer.parseInt(value.substring(4, 6)));
            }
        } else {
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
        }
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }

    public static final String getDate(final long time) {
        StringBuilder sb = new StringBuilder();

        Calendar utcDate = Calendar.getInstance();
        utcDate.setTimeInMillis(time);

        sb.append(utcDate.get(Calendar.YEAR));
        sb.append(getStringValue(utcDate.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(utcDate.get(Calendar.DAY_OF_MONTH)));

        return sb.toString();
    }

    public static final String getDate(final Calendar _c) {
        StringBuilder sb = new StringBuilder();

        Calendar utcDate = Calendar.getInstance();
        utcDate.setTimeInMillis(_c.getTimeInMillis());

        sb.append(utcDate.get(Calendar.YEAR));
        sb.append(getStringValue(utcDate.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(utcDate.get(Calendar.DAY_OF_MONTH)));

        return sb.toString();
    }

    public static final String getTime() {
        StringBuilder sb = new StringBuilder();

        Calendar date = Calendar.getInstance();
        sb.append(date.get(Calendar.YEAR));
        sb.append(getStringValue(date.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(date.get(Calendar.DAY_OF_MONTH)));
        sb.append("T");
        sb.append(getStringValue(date.get(Calendar.HOUR_OF_DAY)));
        sb.append(getStringValue(date.get(Calendar.MINUTE)));
        sb.append(getStringValue(date.get(Calendar.SECOND)));

        return sb.toString();
    }

    public static final String getTime(final TimeZone tz, final long time) {
        StringBuilder sb = new StringBuilder();

        Calendar date = Calendar.getInstance();
        if (tz != null) {
            date = Calendar.getInstance(tz);
        }
        date.setTimeInMillis(time);

        sb.append(date.get(Calendar.YEAR));
        sb.append(getStringValue(date.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(date.get(Calendar.DAY_OF_MONTH)));
        if (date.get(Calendar.HOUR_OF_DAY) != 0 ||
                date.get(Calendar.MINUTE) != 0 ||
                date.get(Calendar.SECOND) != 0) {
            sb.append("T");
            sb.append(getStringValue(date.get(Calendar.HOUR_OF_DAY)));
            sb.append(getStringValue(date.get(Calendar.MINUTE)));
            sb.append(getStringValue(date.get(Calendar.SECOND)));
        }
        return sb.toString();
    }

    public static final String getTime(final Calendar date) {
        StringBuilder sb = new StringBuilder();

        sb.append(date.get(Calendar.YEAR));
        sb.append(getStringValue(date.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(date.get(Calendar.DAY_OF_MONTH)));
        if (date.get(Calendar.HOUR_OF_DAY) != 0 &&
                date.get(Calendar.MINUTE) != 0 &&
                date.get(Calendar.SECOND) != 0) {
            sb.append("T");
            sb.append(getStringValue(date.get(Calendar.HOUR_OF_DAY)));
            sb.append(getStringValue(date.get(Calendar.MINUTE)));
            sb.append(getStringValue(date.get(Calendar.SECOND)));
        }
        return sb.toString();
    }

    public static final String getUTCTime(final long time) {
        StringBuilder sb = new StringBuilder();

        Calendar utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcDate.setTimeInMillis(time);

        sb.append(utcDate.get(Calendar.YEAR));
        sb.append(getStringValue(utcDate.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(utcDate.get(Calendar.DAY_OF_MONTH)));
        sb.append("T");
        sb.append(getStringValue(utcDate.get(Calendar.HOUR_OF_DAY)));
        sb.append(getStringValue(utcDate.get(Calendar.MINUTE)));
        sb.append(getStringValue(utcDate.get(Calendar.SECOND)));
        sb.append("Z");

        return sb.toString();
    }

    public static final String getUTCTime(final Calendar _c) {
        StringBuilder sb = new StringBuilder();

        Calendar utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcDate.setTimeInMillis(_c.getTimeInMillis());

        sb.append(utcDate.get(Calendar.YEAR));
        sb.append(getStringValue(utcDate.get(Calendar.MONTH) + 1));
        sb.append(getStringValue(utcDate.get(Calendar.DAY_OF_MONTH)));
        sb.append("T");
        sb.append(getStringValue(utcDate.get(Calendar.HOUR_OF_DAY)));
        sb.append(getStringValue(utcDate.get(Calendar.MINUTE)));
        sb.append(getStringValue(utcDate.get(Calendar.SECOND)));
        sb.append("Z");

        return sb.toString();
    }

    private static final String getStringValue(final int i) {
        StringBuilder sb = new StringBuilder();
        if (i < 10) {
            sb.append("0");
        }
        sb.append(i);
        return sb.toString();
    }

    public static final int getWeeksForYear(final int year) {
        Calendar date = Calendar.getInstance();
        date.set(year, 0, 1);
        return date.getMaximum(Calendar.WEEK_OF_YEAR);
    }

    public static final int getDaysForMonth(final int year, final int month) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, 1);
        return date.getMaximum(Calendar.DAY_OF_MONTH);
    }

    public static final int getDaysForYear(final int year) {
        Calendar date = Calendar.getInstance();
        date.set(year, 0, 1);
        return date.getMaximum(Calendar.DAY_OF_YEAR);
    }

    public static final int getMonthsBetween(final Calendar date1, final Calendar date2) {
        int months = 0;
        if (date1.before(date2)) {
            while (date1.get(Calendar.MONTH) < date2.get(Calendar.MONTH)) {
                months++;
                date1.add(Calendar.MONTH, 1);
            }
            if (date1.get(Calendar.DAY_OF_MONTH) > date2.get(Calendar.DAY_OF_MONTH)) {
                months--;
            }
        } else {
            while (date1.get(Calendar.MONTH) > date2.get(Calendar.MONTH)) {
                months--;
                date1.add(Calendar.MONTH, -1);
            }
            if (date1.get(Calendar.DAY_OF_MONTH) < date2.get(Calendar.DAY_OF_MONTH)) {
                months++;
            }
        }
        return Math.abs(months);
    }

    public static final int getWeeksBetween(final Calendar date1, final Calendar date2) {
        int weeks = 0;
        if (date1.before(date2)) {
            while (date1.get(Calendar.WEEK_OF_YEAR) < date2.get(Calendar.WEEK_OF_YEAR)) {
                weeks++;
                date1.add(Calendar.WEEK_OF_YEAR, 1);
            }
            if (date1.get(Calendar.DAY_OF_WEEK) > date2.get(Calendar.DAY_OF_WEEK)) {
                weeks--;
            }
        } else {
            while (date1.get(Calendar.WEEK_OF_YEAR) > date2.get(Calendar.WEEK_OF_YEAR)) {
                weeks--;
                date1.add(Calendar.WEEK_OF_YEAR, -1);
            }
            if (date1.get(Calendar.DAY_OF_WEEK) > date2.get(Calendar.DAY_OF_WEEK)) {
                weeks++;
            }
        }
        return Math.abs(weeks);
    }

    public static final long getDaysBetween(final Calendar date1, final Calendar date2) {
        return getHoursBetween(date1, date2) / 24;
    }

    public static final long getHoursBetween(final Calendar date1, final Calendar date2) {
        return getMinutesBetween(date1, date2) / 60;
    }

    public static final long getMinutesBetween(final Calendar date1, final Calendar date2) {
        return getMillisBetween(date1, date2) / (1000 * 60);
    }

    public static final long getMillisBetween(final Calendar date1, final Calendar date2) {
        return Math.abs(date2.getTimeInMillis() - date1.getTimeInMillis());
    }

    public static final long getMillisForDuration(final String trigger) {
        long millis = 0, sign = 1;
        char[] chars = trigger.toCharArray();
        for (int offset = 0; offset < chars.length; offset++) {
            if (chars[offset] == '-') {
                sign = -1;
                continue;
            }
            if (Character.isDigit(chars[offset])) {
                long temp_millis = 1000L;
                String number = "";
                for (; Character.isDigit(chars[offset]); offset++) {
                    number = number.concat(String.valueOf(chars[offset]));
                }
                temp_millis = temp_millis * Long.parseLong(number);
                if (chars[offset] == 'W') {
                    temp_millis = temp_millis * 60 * 60 * 24 * 7;
                } else if (chars[offset] == 'D') {
                    temp_millis = temp_millis * 60 * 60 * 24;
                } else if (chars[offset] == 'H') {
                    temp_millis = temp_millis * 60 * 60;
                } else if (chars[offset] == 'M') {
                    temp_millis = temp_millis * 60;
                }
                millis += temp_millis;
            }
        }
        return millis * sign;
    }
}
