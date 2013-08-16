/*
 * Period class
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

/**
 * @author Ricardo_Lorenzo
 *
 */
public class Period implements Serializable {
    public static final long serialVersionUID = 89472947947294543L;

    private Calendar start;
    private Calendar end;

    public Period(final Calendar start, final Calendar end) {
        this.start = start;
        if (end == null) {
            this.end = start;
        } else {
            this.end = end;
        }
        this.start.set(Calendar.MILLISECOND, 0);
        this.end.set(Calendar.MILLISECOND, 0);
    }

    public Calendar getStart() {
        return start;
    }

    public Calendar getEnd() {
        return end;
    }

    public boolean overlap(final Period p) {
        if (p == null) {
            return false;
        }
        if (start.equals(p.getStart())) {
            return true;
        } else if (end.equals(p.getEnd())) {
            return true;
        } else if (start.before(p.getStart()) && end.after(p.getStart())) {
            return true;
        } else if (start.before(p.getEnd()) && end.after(p.getEnd())) {
            return true;
        } else if (p.getStart().before(start) && p.getEnd().after(end)) {
            return true;
        }
        return false;
    }
}
