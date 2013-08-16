/*
 * VAlarm class
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
import java.util.List;

/**
 * @author Ricardo_Lorenzo
 *
 */
public class VAlarm implements Serializable {
    public static final long serialVersionUID = 89472947947291165L;

    private String description;
    private String action;
    private String attendee;
    private String attach;
    private Duration duration;
    private Trigger trigger;
    private List<String> extended_support;
    private int repeat;

    public VAlarm() {
        extended_support = new ArrayList<String>();
        repeat = 0;
    }

    public void addExtended(final String value) {
        if (value != null && !value.isEmpty()) {
            extended_support.add(value);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getAttendee() {
        return attendee;
    }

    public void setAttendee(final String attendee) {
        this.attendee = attendee;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(final String attach) {
        this.attach = attach;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(final Trigger t) throws Exception {
        trigger = t;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(final Duration duration) {
        this.duration = duration;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(final int repeat) {
        this.repeat = repeat;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VALARM");
        sb.append(VCalendar.CRLF);
        if (description != null) {
            sb.append("DESCRIPTION:");
            sb.append(description);
            sb.append(VCalendar.CRLF);
        }
        if (trigger != null) {
            sb.append(trigger.toString());
            sb.append(VCalendar.CRLF);
        }
        if (duration != null) {
            sb.append("DURATION:");
            sb.append(duration.toString());
            sb.append(VCalendar.CRLF);
        }
        if (action != null) {
            sb.append("ACTION:");
            sb.append(action);
            sb.append(VCalendar.CRLF);
        }
        for (String value : extended_support) {
            sb.append(value);
            sb.append(VCalendar.CRLF);
        }
        sb.append("END:VALARM");
        sb.append(VCalendar.CRLF);
        return sb.toString();
    }
}
