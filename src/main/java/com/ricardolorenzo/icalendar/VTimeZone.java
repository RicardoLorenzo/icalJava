/*
 * VTimeZone class
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Ricardo_Lorenzo
 *
 */
public class VTimeZone implements Serializable, Cloneable {
    public static final long serialVersionUID = 89472947947290954L;

    private TimeZone tz;
    private RRule standardRrule;
    private RRule dayLightRrule;

    public VTimeZone(final String name) {
        if (name == null) {
            tz = TimeZone.getDefault();
        } else {
            tz = TimeZone.getTimeZone(name);
        }

        try {
            standardRrule = new RRule();
            dayLightRrule = new RRule();
            standardRrule.setFrequency("YEARLY");
            dayLightRrule.setFrequency("YEARLY");
            standardRrule.setByMonth(new ArrayList<Integer>(Arrays.asList(new Integer[] { 9 })));
            dayLightRrule.setByMonth(new ArrayList<Integer>(Arrays.asList(new Integer[] { 3 })));
            List<String> days = new ArrayList<String>();
            days.add("-1SU");
            standardRrule.setByDay(days);
            days.clear();
            days.add("-1SU");
            dayLightRrule.setByDay(days);
        } catch (Exception _ex) {
        }
    }

    public String getDayLightDTStart() {
        Calendar startDate = getStartCalendar(dayLightRrule);
        return DateTime.getTime(startDate);
    }

    public RRule getDayLightRRule() {
        return dayLightRrule;
    }

    public String getDayLightTZName() {
        return tz.getDisplayName(true, TimeZone.LONG);
    }

    public String getDayLightOffsetFrom() {
        return getOffset(0);
    }

    public String getDayLightOffsetTo() {
        return getOffset(1);
    }

    private String getOffset(final int correction) {
        int offset = (tz.getRawOffset() / (60 * 60 * 1000)) + correction;
        StringBuilder sb = new StringBuilder();

        if (offset < 0) {
            sb.append("-");
        } else {
            sb.append("+");
        }

        if (Math.abs(offset) < 10) {
            sb.append("0");
        }

        sb.append(Math.abs(offset));
        sb.append("00");

        return sb.toString();
    }

    public String getStandardDTStart() {
        Calendar startDate = getStartCalendar(standardRrule);
        return DateTime.getTime(startDate);
    }

    public RRule getStandardRRule() {
        return standardRrule;
    }

    public String getStandardTZName() {
        return tz.getDisplayName(false, TimeZone.LONG);
    }

    public String getStandardOffsetFrom() {
        return getOffset(1);
    }

    public String getStandardOffsetTo() {
        return getOffset(0);
    }

    private Calendar getStartCalendar(final RRule rrule) {
        Calendar offset = Calendar.getInstance();
        if (!"YEARLY".equals(rrule.getFrequency())) {
            return null;
        }

        if (!rrule.hasByMonth()) {
            return null;
        }

        offset.set(Calendar.HOUR_OF_DAY, 2);
        offset.set(Calendar.MINUTE, 0);
        offset.set(Calendar.SECOND, 0);
        for (Integer i : rrule.getByMonth()) {
            offset.set(Calendar.MONTH, (i - 1));
            break;
        }

        if (rrule.hasByDay()) {
            for (String day : rrule.getByDay()) {
                int num = 0;
                if (day.length() > 2) {
                    try {
                        num = Integer.parseInt(day.substring(0, day.length() - 2));
                    } catch (NumberFormatException _ex) {
                    }
                    day = day.substring(day.length() - 2);
                }

                if ("SU".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                } else if ("MO".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if ("TU".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if ("WE".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if ("TH".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if ("FR".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if ("SA".equals(day)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                }

                if (num != 0) {
                    offset.set(Calendar.DAY_OF_WEEK_IN_MONTH, num);
                }
            }
        }
        return offset;
    }

    public String getTZID() {
        return tz.getID();
    }

    public TimeZone getTimeZone() {
        return tz;
    }

    public void setDayLightRRule(final RRule rrule) {
        dayLightRrule = rrule;
    }

    public void setStandardRRule(final RRule rrule) {
        standardRrule = rrule;
    }

    public void setTZID(final String name) {
        tz.setID(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VTIMEZONE");
        sb.append(VCalendar.CRLF);

        sb.append("TZID:");
        sb.append(getTZID());
        sb.append(VCalendar.CRLF);

        sb.append("BEGIN:DAYLIGHT");
        sb.append(VCalendar.CRLF);
        sb.append("TZNAME:");
        sb.append(getDayLightTZName());
        sb.append(VCalendar.CRLF);
        sb.append("DTSTART:");
        sb.append(getDayLightDTStart());
        sb.append(VCalendar.CRLF);
        sb.append("TZOFFSETFROM:");
        sb.append(getDayLightOffsetFrom());
        sb.append(VCalendar.CRLF);
        sb.append("TZOFFSETTO:");
        sb.append(getDayLightOffsetTo());
        sb.append(VCalendar.CRLF);
        sb.append("RRULE:");
        sb.append(dayLightRrule.toString());
        sb.append(VCalendar.CRLF);
        sb.append("END:DAYLIGHT");
        sb.append(VCalendar.CRLF);

        sb.append("BEGIN:STANDARD");
        sb.append(VCalendar.CRLF);
        sb.append("TZNAME:");
        sb.append(getStandardTZName());
        sb.append(VCalendar.CRLF);
        sb.append("DTSTART:");
        sb.append(getStandardDTStart());
        sb.append(VCalendar.CRLF);
        sb.append("TZOFFSETFROM:");
        sb.append(getStandardOffsetFrom());
        sb.append(VCalendar.CRLF);
        sb.append("TZOFFSETTO:");
        sb.append(getStandardOffsetTo());
        sb.append(VCalendar.CRLF);
        sb.append("RRULE:");
        sb.append(standardRrule.toString());
        sb.append(VCalendar.CRLF);
        sb.append("END:STANDARD");
        sb.append(VCalendar.CRLF);

        sb.append("END:VTIMEZONE");
        sb.append(VCalendar.CRLF);

        return sb.toString();
    }
}
