/*
 * VTodo class
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

/**
 * @author Ricardo_Lorenzo
 *
 */
public class VTodo extends VAction implements Serializable, Cloneable {
    public static final long serialVersionUID = 89472947947290678L;
    private String location;
    private int percent;
    private List<VAlarm> alarms;

    public VTodo() {
        super();
        lastModified = Calendar.getInstance().getTimeInMillis();
        alarms = new ArrayList<VAlarm>();
    }

    public List<VAlarm> getAlarms() {
        return alarms;
    }

    public void addAlarm(final VAlarm alarm) {
        if (alarm != null) {
            alarms.add(alarm);
        }
    }

    public void removeAlarms() {
        alarms = new ArrayList<VAlarm>();
    }

    public Calendar getDue() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(due);
        return date;
    }

    public int getPercent() {
        return percent;
    }

    public boolean hasDue() {
        if (due > 0) {
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        if (due > 0) {
            if (Calendar.getInstance().after(getDue())) {
                return true;
            }
        }
        return false;
    }

    public void setDue(final Calendar due) {
        this.due = due.getTimeInMillis();
    }

    public Calendar getDTStart() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dtstart);
        return date;
    }

    public boolean hasDTStart() {
        if (dtstart > 0) {
            return true;
        }
        return false;
    }

    public void setDTStart(final Calendar dtstart) {
        this.dtstart = dtstart.getTimeInMillis();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setStatus(final String status) throws Exception {
        List<String> _values = new ArrayList<String>(Arrays.asList(new String[] { "NEEDS-ACTION", "COMPLETED",
                "IN-PROCESS", "CANCELLED" }));
        if (!_values.contains(status.toUpperCase())) {
            throw new Exception("invalid status");
        }
        this.status = status.toUpperCase();
    }

    public void setPercent(final int percent) throws Exception {
        if (percent < 0 || percent > 100) {
            throw new Exception("invalid percent value");
        }
        this.percent = percent;
    }

    @Override
    public List<Period> getPeriods(final Period period) {
        return getPeriodsBetween(period.getStart(), period.getEnd());
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(final VTimeZone tz) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VTODO");
        sb.append(VCalendar.CRLF);
        sb.append("UID:");
        sb.append(getUid());
        sb.append(VCalendar.CRLF);
        sb.append("SUMMARY:");
        sb.append(summary);
        sb.append(VCalendar.CRLF);
        if (created > 0) {
            sb.append("CREATED");
            sb.append(getDate(tz, created));
            sb.append(VCalendar.CRLF);
        }
        sb.append("LAST-MODIFIED");
        sb.append(getDate(tz, lastModified));
        sb.append(VCalendar.CRLF);
        if (dtstamp > 0) {
            sb.append("DTSTAMP");
            sb.append(getDate(tz, dtstamp));
            sb.append(VCalendar.CRLF);
        }
        if (dtstart > 0) {
            sb.append("DTSTART");
            sb.append(getDate(tz, dtstart));
            sb.append(VCalendar.CRLF);
        }
        if (due > 0) {
            sb.append("DUE");
            sb.append(getDate(tz, due));
            sb.append(VCalendar.CRLF);
        }
        if (status != null) {
            sb.append("STATUS:");
            sb.append(status);
            sb.append(VCalendar.CRLF);
        }
        sb.append("PERCENT-COMPLETE:");
        sb.append(percent);
        sb.append(VCalendar.CRLF);
        if (description != null) {
            sb.append("DESCRIPTION:");
            sb.append(description);
            sb.append(VCalendar.CRLF);
        }
        if (location != null && !location.isEmpty()) {
            sb.append("LOCATION:");
            sb.append(location);
            sb.append(VCalendar.CRLF);
        }
        if (!categories.isEmpty()) {
            sb.append("CATEGORIES:");
            for (int i = 0; i < categories.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(categories.get(i));
            }
            sb.append(VCalendar.CRLF);
        }
        if (duration > 0) {
            sb.append("DURATION:");
            sb.append(getDuration());
            sb.append(VCalendar.CRLF);
        }
        if (!attendee.isEmpty()) {
            for (Person att : attendee.values()) {
                sb.append(att.toString());
                sb.append(VCalendar.CRLF);
            }
        }
        if (!organizer.isEmpty()) {
            for (Person att : organizer.values()) {
                sb.append(att.toString());
                sb.append(VCalendar.CRLF);
            }
        }
        if (rrule != null) {
            sb.append("RRULE:");
            sb.append(rrule.toString());
            sb.append(VCalendar.CRLF);
        }
        if (recurrenceId > 0) {
            sb.append("RECURRENCE-ID");
            sb.append(getDate(tz, recurrenceId));
            sb.append(VCalendar.CRLF);
        }
        for (String value : extendedSupport) {
            sb.append(value);
            sb.append(VCalendar.CRLF);
        }
        if (!alarms.isEmpty()) {
            for (VAlarm va : alarms) {
                sb.append(va.toString());
            }
        }
        sb.append("END:VTODO");
        sb.append(VCalendar.CRLF);
        return sb.toString();
    }

    @Override
    public VTodo clone() {
        return clone();
    }
}
