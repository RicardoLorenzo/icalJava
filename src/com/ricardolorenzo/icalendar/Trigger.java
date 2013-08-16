/*
 * Trigger class
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
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ricardo_Lorenzo
 *
 */
public class Trigger implements Serializable {
    public static final long serialVersionUID = 89472947947291134L;

    private Calendar date;
    private String value;
    private String related;
    private Duration duration;

    public Trigger(final String value) {
        if (value.contains(";")) {
            StringTokenizer st = new StringTokenizer(value.substring(value.indexOf(";"), value.indexOf(":")), ";");
            while (st.hasMoreTokens()) {
                String v = st.nextToken();
                if (v.startsWith("RELATED")) {
                    related = v.substring(v.indexOf("=") + 1);
                } else if (v.startsWith("VALUE")) {
                    this.value = v.substring(v.indexOf("=") + 1);
                }
            }
        }
        if (validateDate(value.substring(value.indexOf(":") + 1))) {
            date = DateTime.getCalendarFromString(null, value.substring(value.indexOf(":") + 1));
        } else {
            duration = new Duration(value.substring(value.indexOf(":") + 1));
        }
    }

    public Trigger(final Duration duration) {
        this.duration = duration;
    }

    public Trigger(final Calendar date) {
        this.date = date;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getRelated() {
        return related;
    }

    public String getValue() {
        return value;
    }

    public Calendar getDateTime() {
        return date;
    }

    public boolean isDateTime() {
        return date != null;
    }

    public void setRelated(final String related) {
        this.related = related;
    }

    private boolean validateDate(final String date) {
        Pattern p = Pattern.compile("^[0-9]+|[0-9]+[T][0-9]+|[0-9]+[T][0-9][Z]");
        Matcher m = p.matcher(date);
        return m.matches();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TRIGGER");
        if (related != null) {
            sb.append(";");
            sb.append("RELATED=");
            sb.append(related);
        }
        if (isDateTime()) {
            sb.append(";VALUE=DATE-TIME");
            sb.append(":");
            sb.append(DateTime.getTime(date));
        } else {
            sb.append(";VALUE=DURATION");
            sb.append(":");
            sb.append(duration.toString());
        }
        return sb.toString();
    }
}
