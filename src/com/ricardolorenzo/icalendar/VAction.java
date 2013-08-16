/*
 * VAction class
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ricardo_Lorenzo
 *
 */
public abstract class VAction implements Serializable, Cloneable {
    public static final long serialVersionUID = 2740274024782042L;
    private static final long MINUTE = 60 * 1000L;
    protected long created;
    protected long lastModified;
    protected long recurrenceId;
    protected long duration;
    protected long dtstamp;
    protected long dtstart;
    protected long dtend;
    protected long due;
    protected String uid;
    protected String summary;
    protected String description;
    protected String classType;
    protected String status;
    protected List<String> categories;
    protected Map<String, Person> organizer;
    protected Map<String, Person> attendee;
    protected List<String> extendedSupport;
    protected List<Calendar> exdate;
    protected RRule rrule;
    private int count;

    public VAction() {
        created = Calendar.getInstance().getTimeInMillis();
        lastModified = Calendar.getInstance().getTimeInMillis();
        dtend = 0;
        due = 0;
        attendee = new HashMap<String, Person>();
        organizer = new HashMap<String, Person>();
        exdate = new ArrayList<Calendar>();
        categories = new ArrayList<String>();
        extendedSupport = new ArrayList<String>();
        count = 0;
    }

    /**
     * @deprecated
     * @param mail
     * @param att
     * @throws Exception
     */
    @Deprecated
    public void addAttendee(final String mail, final Person att) throws VCalendarException {
        if (mail != null) {
            attendee.put(mail, att);
        }
    }

    public void addCategory(final String category) throws VCalendarException {
        if (category != null) {
            categories.add(category);
        }
    }

    public void addExtended(final String value) {
        if (value != null && !value.isEmpty()) {
            extendedSupport.add(value);
        }
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

    public String getDescription() {
        return description;
    }

    public void addExDate(final Calendar date) {
        if (date != null) {
            date.set(Calendar.MILLISECOND, 0);
            exdate.add(date);
        }
    }

    /**
     * Applies BYDAY rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByDayPeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByDay()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (String dayName : rrule.getByDay()) {
            if (!rrule.hasByYearDay() || !rrule.hasByMonthDay()) {
                if ("SU".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                } else if ("MO".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if ("TU".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if ("WE".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if ("TH".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if ("FR".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if ("SA".equals(dayName)) {
                    offset.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                }
            } else {
                // TODO: Not implemented
            }

            Period tempPeriod = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(tempPeriod, start, end)) {
                long key = tempPeriod.getEnd().getTimeInMillis() + tempPeriod.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, tempPeriod);
                }
            }
        }

        return new ArrayList<Period>(periods.values());
    }

    /**
     * Applies BYHOUR rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByHourPeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByHour()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (Integer hour : rrule.getByHour()) {
            offset.set(Calendar.HOUR_OF_DAY, hour);
            Period tempPeriod = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(tempPeriod, start, end)) {
                long key = tempPeriod.getEnd().getTimeInMillis() + tempPeriod.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, tempPeriod);
                }
            }
        }
        return new ArrayList<Period>(periods.values());
    }

    /**
     * Applies BYMINUTE rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByMinutePeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByMinute()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (Integer minute : rrule.getByMinute()) {
            offset.set(Calendar.MINUTE, minute);
            Period tempPeriod = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(tempPeriod, start, end)) {
                long key = tempPeriod.getEnd().getTimeInMillis() + tempPeriod.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, tempPeriod);
                }
            }
        }
        return new ArrayList<Period>(periods.values());
    }

    /**
     * Applies BYMONTHDAY rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByMonthDayPeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByMonthDay()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (Integer day : rrule.getByMonthDay()) {
            offset.set(Calendar.DAY_OF_MONTH, day);
            Period _p = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(_p, start, end)) {
                long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, _p);
                }
            }
        }
        return new ArrayList<Period>(periods.values());
    }

    /**
     * Applies BYMONTH rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByMonthPeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByMonth()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (Integer month : rrule.getByMonth()) {
            offset.roll(Calendar.MONTH, month);
            Period _p = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(_p, start, end)) {
                long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, _p);
                }
            }
        }
        return new ArrayList<Period>(periods.values());
    }

    /**
     * Applies BYWEEKNO rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByWeekNoPeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByWeekNo()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (Integer week : rrule.getByWeekNo()) {
            offset.set(Calendar.WEEK_OF_YEAR, week);
            Period _p = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(_p, start, end)) {
                long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, _p);
                }
            }
        }
        return new ArrayList<Period>(periods.values());
    }

    /**
     * Applies BYYEARDAY rules specified in RRULE.
     * @param dates
     * @return
     */
    private List<Period> getByYearDayPeriods(final Period p, final Calendar start, final Calendar end) {
        Map<Long, Period> periods = new HashMap<Long, Period>();
        if (!rrule.hasByYearDay()) {
            return new ArrayList<Period>(periods.values());
        }

        Calendar offset = p.getStart();
        for (Integer day : rrule.getByYearDay()) {
            offset.set(Calendar.DAY_OF_YEAR, day);
            Period _p = new Period((Calendar) offset.clone(), getEnd(offset));
            if (validatePeriod(_p, start, end)) {
                long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                if (!periods.containsKey(key)) {
                    periods.put(key, _p);
                }
            }
        }
        return new ArrayList<Period>(periods.values());
    }

    public List<String> getCategories() throws VCalendarException {
        return categories;
    }

    public String getClassType() {
        return classType;
    }

    public Calendar getCreated() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(created);
        return date;
    }

    protected static String getDate(VTimeZone tz, final long date) {
        StringBuilder sb = new StringBuilder();
        if (tz == null) {
            tz = new VTimeZone(null);
        }
        sb.append(";TZID=");
        sb.append(tz.getTZID());
        sb.append(":");
        sb.append(DateTime.getTime(tz.getTimeZone(), date));
        return sb.toString();
    }

    public Calendar getDTStamp() {
        Calendar date = Calendar.getInstance();
        if (dtstamp > 0) {
            date.setTimeInMillis(dtstamp);
        }
        return date;
    }

    public String getDuration() {
        StringBuilder sb = new StringBuilder();
        float hours = duration / (60 * 60 * 1000L);
        sb.append("P");

        if (hours > 24) {
            int days = Math.round(hours);
            hours = hours - (days * 24);
            sb.append(days);
            sb.append("D");
        }

        sb.append("T");
        sb.append((int) hours);
        sb.append("H");

        return sb.toString();
    }

    public int getDurationDays() {
        return getDurationHours() / 24;
    }

    public int getDurationHours() {
        return getDurationMinutes() / 60;
    }

    public int getDurationMinutes() {
        long end;
        if (dtend > 0) {
            end = dtend;
        } else if (due > 0) {
            end = due;
        } else {
            return 0;
        }

        return (int) ((end - dtstart) / MINUTE);
    }

    private Calendar getEnd(final Calendar date) {
        long diff = 0;
        Calendar endDate = Calendar.getInstance();
        if (dtend > 0) {
            diff = dtend - dtstart;
        } else if (due > 0) {
            diff = due + dtstart;
        }
        endDate.setTimeInMillis(date.getTimeInMillis() + diff);
        return endDate;
    }

    public List<Calendar> getExDates() {
        return exdate;
    }

    public Calendar getLastModified() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(lastModified);
        return date;
    }

    private Period getNextPeriod(final Calendar date, final Calendar start, final Calendar end) {
        Calendar offset = (Calendar) date.clone();
        if (rrule != null) {
            if ("MINUTELY".equals(rrule.getFrequency())) {
                if (rrule.hasInterval()) {
                    offset.add(Calendar.MINUTE, rrule.getInterval());
                } else {
                    offset.add(Calendar.MINUTE, 1);
                }
            } else if ("HOURLY".equals(rrule.getFrequency())) {
                if (rrule.hasInterval()) {
                    offset.add(Calendar.HOUR_OF_DAY, rrule.getInterval());
                } else {
                    offset.add(Calendar.HOUR_OF_DAY, 1);
                }
            } else if ("DAILY".equals(rrule.getFrequency())) {
                if (rrule.hasInterval()) {
                    offset.add(Calendar.DAY_OF_MONTH, rrule.getInterval());
                } else {
                    offset.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else if ("WEEKLY".equals(rrule.getFrequency())) {
                if (rrule.hasInterval()) {
                    offset.add(Calendar.WEEK_OF_YEAR, rrule.getInterval());
                } else {
                    offset.add(Calendar.WEEK_OF_YEAR, 1);
                }
            } else if ("MONTHLY".equals(rrule.getFrequency())) {
                if (rrule.hasInterval()) {
                    offset.add(Calendar.MONTH, rrule.getInterval());
                } else {
                    offset.add(Calendar.MONTH, 1);
                }
            } else if ("YEARLY".equals(rrule.getFrequency())) {
                if (rrule.hasInterval()) {
                    offset.add(Calendar.YEAR, rrule.getInterval());
                } else {
                    offset.add(Calendar.YEAR, 1);
                }
            }

            return new Period(offset, getEnd(offset));
        }
        return null;
    }

    public List<Period> getPeriods(final Period period) {
        return getPeriodsBetween(period.getStart(), period.getEnd());
    }

    protected List<Period> getPeriodsBetween(final Calendar start_date, final Calendar end_date) {
        Map<Long, Period> dates = new HashMap<Long, Period>();
        start_date.set(Calendar.MILLISECOND, 0);
        end_date.set(Calendar.MILLISECOND, 0);

        Calendar start = (Calendar) start_date.clone();
        Calendar end = (Calendar) end_date.clone();

        if (start.after(end)) {
            return new ArrayList<Period>(dates.values());
        }
        Calendar dateStart = Calendar.getInstance();
        dateStart.setTimeInMillis(dtstart);
        dateStart.set(Calendar.MILLISECOND, 0);

        Calendar dateEnd = (Calendar) dateStart.clone();
        if (dtend > 0) {
            dateEnd = Calendar.getInstance();
            dateEnd.setTimeInMillis(dtend);
            dateEnd.set(Calendar.MILLISECOND, 0);

            if (dateEnd.before(end) && rrule == null) {
                end = (Calendar) dateEnd.clone();
            }
        } else if (due > 0) {
            dateEnd = Calendar.getInstance();
            dateEnd.setTimeInMillis(due);
            dateEnd.set(Calendar.MILLISECOND, 0);

            if (dateEnd.before(end) && rrule == null) {
                end = (Calendar) dateEnd.clone();
            }
        }

        if (dateStart.after(end)) {
            return new ArrayList<Period>(dates.values());
        }

        if (dateStart.after(start) || (dateEnd.after(start))) {
            Period _p = new Period((Calendar) dateStart.clone(), (Calendar) dateEnd.clone());
            if (validatePeriod(_p, start, end)) {
                dates.put((dateEnd.getTimeInMillis() + dateStart.getTimeInMillis()), _p);
            }
        }

        if (rrule != null) {
            Calendar offset = (Calendar) dateStart.clone();

            if (rrule.hasUntil()) {
                if (end.after(rrule.getUntil())) {
                    end = rrule.getUntil();
                }
            }

            if (rrule.hasWeekStart()) {
                if ("SU".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.SUNDAY);
                    end.setFirstDayOfWeek(Calendar.SUNDAY);
                    offset.setFirstDayOfWeek(Calendar.SUNDAY);
                } else if ("MO".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.MONDAY);
                    end.setFirstDayOfWeek(Calendar.MONDAY);
                    offset.setFirstDayOfWeek(Calendar.MONDAY);
                } else if ("TU".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.TUESDAY);
                    end.setFirstDayOfWeek(Calendar.TUESDAY);
                    offset.setFirstDayOfWeek(Calendar.TUESDAY);
                } else if ("WE".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.WEDNESDAY);
                    end.setFirstDayOfWeek(Calendar.WEDNESDAY);
                    offset.setFirstDayOfWeek(Calendar.WEDNESDAY);
                } else if ("TH".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.THURSDAY);
                    end.setFirstDayOfWeek(Calendar.THURSDAY);
                    offset.setFirstDayOfWeek(Calendar.THURSDAY);
                } else if ("FR".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.FRIDAY);
                    end.setFirstDayOfWeek(Calendar.FRIDAY);
                    offset.setFirstDayOfWeek(Calendar.FRIDAY);
                } else if ("SA".equals(rrule.getWeekStart())) {
                    start.setFirstDayOfWeek(Calendar.SATURDAY);
                    end.setFirstDayOfWeek(Calendar.SATURDAY);
                    offset.setFirstDayOfWeek(Calendar.SATURDAY);
                }
            }

            for (Period p = new Period(dateStart, dateEnd); p != null; p = getNextPeriod(p.getStart(), start, end)) {
                if (rrule.hasByMonth()) {
                    for (Period _p : getByMonthPeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (rrule.hasByWeekNo()) {
                    for (Period _p : getByWeekNoPeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (rrule.hasByYearDay()) {
                    for (Period _p : getByYearDayPeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (rrule.hasByMonthDay()) {
                    for (Period _p : getByMonthDayPeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (rrule.hasByDay()) {
                    for (Period _p : getByDayPeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (rrule.hasByHour()) {
                    for (Period _p : getByHourPeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (rrule.hasByMinute()) {
                    for (Period _p : getByMinutePeriods(p, start, end)) {
                        long key = _p.getEnd().getTimeInMillis() + _p.getStart().getTimeInMillis();
                        if (!dates.containsKey(key)) {
                            dates.put(key, _p);
                        }
                    }
                }

                if (validatePeriod(p, start, end)) {
                    long key = p.getEnd().getTimeInMillis() + p.getStart().getTimeInMillis();
                    if (!dates.containsKey(key)) {
                        dates.put(key, p);
                    }
                } else if (p.getEnd().after(end)) {
                    break;
                }
            }
        }

        return new ArrayList<Period>(dates.values());
    }

    public String getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public Calendar getRecurrenceId() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(recurrenceId);
        return date;
    }

    public RRule getRRule() {
        return rrule;
    }

    public String getUid() {
        if (uid == null) {
            uid = randomUid();
        }
        return uid;
    }

    public boolean hasRecurrence() {
        if (recurrenceId > 0) {
            return true;
        }
        return false;
    }

    public boolean hasAttendee() {
        if (!attendee.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean hasLastModified() {
        if (lastModified > 0) {
            return true;
        }
        return false;
    }

    private boolean isExdate(final Calendar date) {
        date.set(Calendar.MILLISECOND, 0);
        for (Calendar exdate : this.exdate) {
            if (exdate.getTimeInMillis() == date.getTimeInMillis()) {
                return true;
            }
        }
        return false;
    }

    public static String randomUid() {
        return UUID.randomUUID().toString();
    }

    public void removeAttendee(final String mail) throws VCalendarException {
        attendee.remove(mail);
    }

    public void removeRRule() {
        rrule = null;
    }

    public void setAttendee(final String mail, final Person att) throws VCalendarException {
        if (mail != null) {
            attendee.put(mail, att);
        }
    }

    public void setOrganizer(final String mail, final Person att) throws VCalendarException {
        if (mail != null) {
            organizer.put(mail, att);
        }
    }

    public void setCategories(final String[] categories) throws Exception {
        if (categories != null) {
            this.categories.addAll(Arrays.asList(categories));
        }
    }

    public void setClassType(final String classType) {
        this.classType = classType;
    }

    public void setCreated(final Calendar created) {
        this.created = created.getTimeInMillis();
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDTStamp(final Calendar date) {
        dtstamp = date.getTimeInMillis();
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public void setLastModified(final Calendar lastModified) {
        if (lastModified != null) {
            this.lastModified = lastModified.getTimeInMillis();
        }
    }

    public void setRecurrenceId(final Calendar date) {
        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        recurrenceId = date.getTimeInMillis();
    }

    public void setRRule(final RRule rrule) {
        this.rrule = rrule;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    private boolean validatePeriod(final Period p, final Calendar start, final Calendar end) {
        if (p == null) {
            return false;
        }

        if (rrule != null) {
            if (rrule.hasCount()) {
                if (count > rrule.getCount()) {
                    return false;
                }
                count++;
            }

            if (rrule.hasUntil()) {
                Calendar until = rrule.getUntil();
                until.set(Calendar.HOUR_OF_DAY, until.getMaximum(Calendar.HOUR_OF_DAY));
                until.set(Calendar.MINUTE, until.getMaximum(Calendar.MINUTE));
                until.set(Calendar.SECOND, until.getMaximum(Calendar.SECOND));
                if (until.getTimeInMillis() < start.getTimeInMillis()) {
                    return false;
                }
            }
        }

        if (p.getStart().getTimeInMillis() >= end.getTimeInMillis()) {
            return false;
        }

        if (p.getEnd().getTimeInMillis() <= start.getTimeInMillis()) {
            return false;
        }

        if (isExdate(p.getStart())) {
            return false;
        }

        return true;
    }
}
