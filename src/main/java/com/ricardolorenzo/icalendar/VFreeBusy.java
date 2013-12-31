/*
 * VFreeBusy class
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Ricardo Lorenzo <unshakablespirit@gmail.com>
 */
package com.ricardolorenzo.icalendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ricardo_Lorenzo
 * 
 */
public class VFreeBusy implements Serializable {
    public static final long serialVersionUID = 89472947947297236L;

    private VTimeZone tz;
    private long dtstart;
    private long dtend;
    private List<Period> freeBusy;
    protected Map<String, Person> organizer;
    protected Map<String, Person> attendee;

    public VFreeBusy(final VTimeZone tz) {
        if (tz == null) {
            this.tz = new VTimeZone(null);
        } else {
            this.tz = tz;
        }
        freeBusy = new ArrayList<Period>();
        attendee = new HashMap<String, Person>();
        organizer = new HashMap<String, Person>();
    }

    public void addBusy(final Calendar start, final Calendar end) {
        freeBusy.add(new Period(start, end));
    }

    public void addBusy(final Period p) {
        freeBusy.add(p);
    }

    public void addAllBusy(final List<Period> busy) {
        freeBusy.addAll(busy);
    }

    public Calendar getDTStart() {
        Calendar _c = Calendar.getInstance();
        _c.setTimeInMillis(dtstart);
        return _c;
    }

    public Calendar getDTEnd() {
        if (dtend == 0) {
            return null;
        }
        Calendar _c = Calendar.getInstance();
        _c.setTimeInMillis(dtend);
        return _c;
    }

    public List<Period> getFreeBusy() {
        return freeBusy;
    }

    public List<String> getAtendeesMailTo() {
        return new ArrayList<String>(attendee.keySet());
    }

    public List<Person> getAttendees() {
        return new ArrayList<Person>(attendee.values());
    }

    public List<String> getOrganizersMailTo() {
        return new ArrayList<String>(organizer.keySet());
    }

    public List<Person> getOrganizers() {
        return new ArrayList<Person>(organizer.values());
    }

    public VTimeZone getTimeZone() {
        return tz;
    }

    public boolean hasDTEnd() {
        if (dtend > 0) {
            return true;
        }
        return false;
    }

    public boolean hasDTStart() {
        if (dtstart > 0) {
            return true;
        }
        return false;
    }

    public void setAttendee(final String mail, final Person att) {
        if (mail != null) {
            attendee.put(mail, att);
        }
    }

    public void setOrganizer(final String mail, final Person att) {
        if (mail != null) {
            organizer.put(mail, att);
        }
    }

    public void setDTStart(final Calendar dtstart) {
        this.dtstart = dtstart.getTimeInMillis();
    }

    public void setDTEnd(final Calendar dtend) {
        if (dtend != null) {
            this.dtend = dtend.getTimeInMillis();
        } else {
            this.dtend = 0;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VFREEBUSY");
        sb.append(VCalendar.CRLF);
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
        if (dtstart > 0) {
            sb.append("DTSTART");
            sb.append(VAction.getDate(tz, dtstart));
            sb.append(VCalendar.CRLF);
        }
        if (dtend > 0) {
            sb.append("DTEND");
            sb.append(VAction.getDate(tz, dtend));
            sb.append(VCalendar.CRLF);
        }
        if (!freeBusy.isEmpty()) {
            for (Period busy : freeBusy) {
                sb.append("FREEBUSY;FBTYPE=BUSY-UNAVAILABLE:");
                sb.append(DateTime.getUTCTime(busy.getStart()));
                sb.append("/");
                sb.append(DateTime.getUTCTime(busy.getEnd()));
                sb.append(VCalendar.CRLF);
            }
        }
        sb.append("END:VFREEBUSY");
        sb.append(VCalendar.CRLF);

        return sb.toString();
    }
}
