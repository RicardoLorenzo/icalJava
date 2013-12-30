/*
 * VEvent class
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
public class VEvent extends VAction implements Serializable, Cloneable {
    public static final long serialVersionUID = 89472947947290420L;
    private String location;
    private List<VAlarm> alarms;

    public VEvent() {
        super();
        lastModified = Calendar.getInstance().getTimeInMillis();
        alarms = new ArrayList<VAlarm>();
    }

    public boolean isAllDayEvent() {
        long difference = dtend - dtstart;
        if (difference < (86400000 + 1000) &&
                difference > (86400000 - 1000)) {
            return true;
        }
        return false;
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

    public Calendar getDTStart() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dtstart);
        return date;
    }

    public void setDTStart(final Calendar dtstart) {
        if (dtstart != null) {
            this.dtstart = dtstart.getTimeInMillis();
        }
    }

    public Calendar getDTEnd() {
        if (dtend == 0) {
            return null;
        }
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dtend);
        return date;
    }

    public void setDTEnd(final Calendar dtend) {
        if (dtend != null) {
            this.dtend = dtend.getTimeInMillis();
        } else {
            this.dtend = 0;
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setStatus(final String status) throws VCalendarException {
        List<String> values = new ArrayList<String>(
                Arrays.asList(new String[] { "TENTATIVE", "CONFIRMED", "CANCELLED" }));
        if (!values.contains(status.toUpperCase())) {
            throw new VCalendarException("invalid status");
        }
        this.status = status.toUpperCase();
    }

    public List<VAlarm> getActiveValarmsForDate(final Calendar event_date, final Calendar date) {
        List<VAlarm> alarms = new ArrayList<VAlarm>();
        return alarms;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(final VTimeZone tz) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VEVENT");
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
        sb.append("DTSTART");
        sb.append(getDate(tz, dtstart));
        sb.append(VCalendar.CRLF);
        if (dtend > 0) {
            sb.append("DTEND");
            sb.append(getDate(tz, dtend));
            sb.append(VCalendar.CRLF);
        }
        if (description != null) {
            sb.append("DESCRIPTION:");
            sb.append(description);
            sb.append(VCalendar.CRLF);
        }
        if (location != null) {
            sb.append("LOCATION:");
            sb.append(location);
            sb.append(VCalendar.CRLF);
        }
        if (status != null) {
            sb.append("STATUS:");
            sb.append(status);
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
        if (duration > 0) {
            sb.append("DURATION:");
            sb.append(getDuration());
            sb.append(VCalendar.CRLF);
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
        sb.append("END:VEVENT");
        sb.append(VCalendar.CRLF);
        return sb.toString();
    }

    @Override
    public VEvent clone() {
        return clone();
    }
}
