/*
 * Period class
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
import java.util.List;

/**
 * @author Ricardo_Lorenzo
 * 
 */
public class Period implements Serializable {
    public static final long serialVersionUID = 89472947947294543L;

    public static Period getDayPeriod(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        final Calendar endDate = (java.util.Calendar) date.clone();
        endDate.add(Calendar.DAY_OF_MONTH, 1);
        return new Period(date, endDate);
    }

    public static Period getMonthPeriod(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        final Calendar endDate = (java.util.Calendar) date.clone();
        endDate.add(Calendar.MONTH, 1);
        return new Period(date, endDate);
    }

    public static Period getWeekPeriod(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        final Calendar endDate = (java.util.Calendar) date.clone();
        endDate.add(Calendar.WEEK_OF_MONTH, 1);
        return new Period(date, endDate);
    }

    private final Calendar start;

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

    public Calendar getEnd() {
        return this.end;
    }

    /**
     * Return the <code>Integer</code> representation of the week days for this period in a specific
     * week date
     * 
     * @param date
     * @return
     */
    public List<Integer> getMonthDays(final Calendar date) {
        final List<Integer> days = new ArrayList<Integer>();
        final Period mp = getMonthPeriod(date);
        final Calendar offset = Calendar.class.cast(mp.getStart().clone());
        for (; offset.before(this.end) && offset.before(mp.getEnd()); offset.add(Calendar.DAY_OF_MONTH, 1)) {
            days.add(offset.get(Calendar.DAY_OF_MONTH));
        }
        return days;
    }

    public Calendar getStart() {
        return this.start;
    }

    /**
     * Return the <code>Integer</code> representation of the week days for this period in a specific
     * week date
     * 
     * @param date
     * @return
     */
    public List<Integer> getWeekDays(final Calendar date) {
        final List<Integer> days = new ArrayList<Integer>();
        final Period mp = getWeekPeriod(date);
        final Calendar offset = Calendar.class.cast(mp.getStart().clone());
        for (; offset.before(this.end) && offset.before(mp.getEnd()); offset.add(Calendar.DAY_OF_WEEK, 1)) {
            days.add(offset.get(Calendar.DAY_OF_WEEK));
        }
        return days;
    }

    public boolean overlap(final Period p) {
        if (p == null) {
            return false;
        }
        if (this.start.equals(p.getStart())) {
            return true;
        } else if (this.end.equals(p.getEnd())) {
            return true;
        } else if (this.start.before(p.getStart()) && this.end.after(p.getStart())) {
            return true;
        } else if (this.start.before(p.getEnd()) && this.end.after(p.getEnd())) {
            return true;
        } else if (p.getStart().before(this.start) && p.getEnd().after(this.end)) {
            return true;
        }
        return false;
    }
}
