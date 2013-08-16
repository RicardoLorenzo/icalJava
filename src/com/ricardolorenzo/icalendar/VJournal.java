/*
 * VJournal class
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
public class VJournal extends VAction implements Serializable, Cloneable {
    public static final long serialVersionUID = 89472947947290876L;

    public VJournal() {
        lastModified = Calendar.getInstance().getTimeInMillis();
        categories = new ArrayList<String>();
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

    @Override
    public RRule getRRule() {
        return rrule;
    }

    @Override
    public void setRRule(final RRule rrule) {
        this.rrule = rrule;
    }

    public void setStatus(final String status) throws Exception {
        List<String> values = new ArrayList<String>(Arrays.asList(new String[] { "DRAFT", "FINAL", "CANCELLED" }));
        if (!values.contains(status.toUpperCase())) {
            throw new Exception("invalid status");
        }
        this.status = status.toUpperCase();
    }

    @Override
    public List<Period> getPeriods(final Period period) {
        return getPeriodsBetween(period.getStart(), period.getEnd());
    }

    public String toString(final VTimeZone tz) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VJOURNAL");
        sb.append(VCalendar.CRLF);
        sb.append("UID:");
        sb.append(getUid());
        sb.append(VCalendar.CRLF);
        sb.append("LAST-MODIFIED");
        sb.append(getDate(tz, lastModified));
        sb.append(VCalendar.CRLF);
        sb.append("DTSTART:");
        sb.append(getDate(tz, dtstart));
        sb.append(VCalendar.CRLF);
        sb.append("SUMMARY:");
        sb.append(summary);
        sb.append(VCalendar.CRLF);
        if (description != null) {
            sb.append("DESCRIPTION:");
            sb.append(status);
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
        if (rrule != null) {
            sb.append("RRULE:");
            sb.append(rrule.toString());
            sb.append(VCalendar.CRLF);
        }
        for (String value : extendedSupport) {
            sb.append(value);
            sb.append(VCalendar.CRLF);
        }
        sb.append("END:VJOURNAL");
        sb.append(VCalendar.CRLF);
        return sb.toString();
    }
}
