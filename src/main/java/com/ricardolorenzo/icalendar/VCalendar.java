/*
 * VCalendar class
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.ricardolorenzo.file.io.FileUtils;
import com.ricardolorenzo.file.lock.FileLockException;

/**
 * @author Ricardo_Lorenzo
 * 
 */
public class VCalendar implements Serializable {
    public static final long serialVersionUID = 987294720947290472L;
    protected static final String CRLF = "\r\n";
    public static final String prodid = "-//Ricardo Lorenzo//NONSGML Ricardo Lorenzo//EN";
    public static final String version = "2.0";
    private VTimeZone vtimezone;
    private VFreeBusy vfreebusy;
    private Map<String, VEvent> vevent;
    private Map<String, VTodo> vtodo;
    private Map<String, VJournal> vjournal;
    private String method;
    transient private File ical_file;
    transient private String line;
    transient private BufferedReader buffer;

    public VCalendar() throws VCalendarException {
        vtimezone = new VTimeZone(null);
        vevent = new HashMap<String, VEvent>();
        vtodo = new HashMap<String, VTodo>();
        vjournal = new HashMap<String, VJournal>();
    }

    public VCalendar(final File icalendar) throws VCalendarException {
        this();
        ical_file = icalendar;

        if (ical_file.exists()) {
            try {
                InputStream is = new BufferedInputStream(new ByteArrayInputStream(readBytes(new FileInputStream(
                        ical_file))));
                buffer = new BufferedReader(new InputStreamReader(is));
                try {
                    parse();
                } finally {
                    buffer.close();
                }
            } catch (IOException e) {
                throw new VCalendarException(e);
            }
        }
    }

    public VCalendar(final String content) throws VCalendarException {
        this();
        try {
            buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.trim().getBytes())));
            try {
                parse();
            } finally {
                buffer.close();
            }
        } catch (IOException e) {
            throw new VCalendarException(e);
        }
    }

    public VCalendar(final InputStream is) throws VCalendarException {
        this();
        try {
            buffer = new BufferedReader(new InputStreamReader(is));
            try {
                parse();
            } finally {
                buffer.close();
            }
        } catch (IOException e) {
            throw new VCalendarException(e);
        }
    }

    /**
     * Add VEvent object
     * 
     * @param ve
     */
    public void addVevent(final VEvent ve) {
        vevent.put(ve.getUid(), ve);
    }

    /**
     * Add VJournal object
     * 
     * @param vj
     */
    public void addVjournal(final VJournal vj) {
        vjournal.put(vj.getUid(), vj);
    }

    /**
     * Add VTodo object
     * 
     * @param vt
     */
    public void addVtodo(final VTodo vt) {
        vtodo.put(vt.getUid(), vt);
    }

    /**
     * Return all active VTodo objects
     * 
     * @return
     */
    public List<VTodo> getActiveVtodos() {
        List<VTodo> vtodos = new ArrayList<VTodo>();
        for (Entry<String, VTodo> e : vtodo.entrySet()) {
            VTodo _vt = e.getValue();
            if (isActiveStatus(_vt.getStatus())) {
                vtodos.add(_vt);
            }
        }
        return vtodos;
    }

    /**
     * Return all VEvent objects related to the calendar object day.
     * 
     * @param date
     * @return
     */
    public List<VEvent> getDayVevents(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar _end = (java.util.Calendar) date.clone();
        _end.add(Calendar.DAY_OF_MONTH, 1);

        return getVevents(new Period(date, _end));
    }

    /**
     * Return all VJournal objects related to the calendar object day.
     * 
     * @param date
     * @return
     */
    public List<VJournal> getDayVjournals(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar _end = (java.util.Calendar) date.clone();
        _end.add(Calendar.DAY_OF_MONTH, 1);

        return getVjournals(new Period(date, _end));
    }

    /**
     * Return a map of VEvent objects related to the calendar object day. The key of this map is an
     * <code>Integer</code> with the day hour (24h format).
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VEvent>> getDayVeventsMap(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar endDate = (java.util.Calendar) date.clone();
        endDate.add(Calendar.DAY_OF_MONTH, 1);

        Map<Integer, List<VEvent>> day_events = new HashMap<Integer, List<VEvent>>();
        for (VEvent _ve : getVevents(new Period(date, endDate))) {
            List<Period> periods = _ve.getPeriods(new Period(date, endDate));
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    Map<String, VEvent> vevents = new HashMap<String, VEvent>();
                    if (day_events.containsKey(p.getStart().get(Calendar.HOUR_OF_DAY))) {
                        for (VEvent _tve : day_events.get(p.getStart().get(Calendar.HOUR_OF_DAY))) {
                            vevents.put(_tve.getUid(), _tve);
                        }
                    }
                    if (!vevents.containsKey(_ve.getUid())) {
                        vevents.put(_ve.getUid(), _ve);
                    }
                    day_events.put(p.getStart().get(Calendar.HOUR_OF_DAY), new ArrayList<VEvent>(vevents.values()));
                }
            }
        }
        return day_events;
    }

    /**
     * Return all VTodo objects related to the calendar object day.
     * 
     * @param date
     * @return
     */
    public List<VTodo> getDayVtodos(final Calendar date) {
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar _end = (java.util.Calendar) date.clone();
        _end.add(Calendar.DAY_OF_MONTH, 1);

        return getVtodos(new Period(date, _end));
    }

    /**
     * Return the method
     * 
     * @return
     */
    public String getMethod() {
        if (method == null) {
            return "";
        }
        return method;
    }

    /**
     * Return all VEvent objects related to the calendar object month.
     * 
     * @param date
     * @return
     */
    public List<VEvent> getMonthVevents(final Calendar date) {
        date.set(java.util.Calendar.DAY_OF_MONTH, 1);
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar _end = (java.util.Calendar) date.clone();
        _end.add(java.util.Calendar.MONTH, 1);

        return getVevents(new Period(date, _end));
    }

    /**
     * Return a map of VEvent objects related to the calendar object month. The key of this map is
     * an <code>Integer</code> with the month day hour.
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VEvent>> getMonthVeventsMap(final Calendar date) {
        date.set(java.util.Calendar.DAY_OF_MONTH, 1);
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar endDate = (java.util.Calendar) date.clone();
        endDate.add(java.util.Calendar.MONTH, 1);

        Map<Integer, List<VEvent>> month_events = new HashMap<Integer, List<VEvent>>();
        for (VEvent ve : getVevents(new Period(date, endDate))) {
            List<Period> periods = ve.getPeriods(new Period(date, endDate));
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    Calendar offset = (Calendar) p.getStart().clone();
                    for (; offset.before(p.getEnd()) && offset.before(endDate); offset.add(Calendar.DAY_OF_MONTH, 1)) {
                        ArrayList<VEvent> vevents = new ArrayList<VEvent>();
                        if (month_events.containsKey(offset.get(Calendar.DAY_OF_MONTH))) {
                            vevents.addAll(month_events.get(offset.get(Calendar.DAY_OF_MONTH)));
                        }
                        vevents.add(ve);
                        month_events.put(offset.get(Calendar.DAY_OF_MONTH), vevents);
                    }
                }
            }
        }
        return month_events;
    }

    /**
     * Return all VJournal objects related to the calendar object month.
     * 
     * @param date
     * @return
     */
    public List<VJournal> getMonthVjournals(final Calendar date) {
        date.set(java.util.Calendar.DAY_OF_MONTH, 1);
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar _end = (java.util.Calendar) date.clone();
        _end.add(java.util.Calendar.MONTH, 1);

        return getVjournals(new Period(date, _end));
    }

    /**
     * Return a VJournal map related to the calendar object month. The key of the map is an
     * <code>Integer</code> with the month day.
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VJournal>> getMonthVjournalsMap(final Calendar date) {
        date.set(java.util.Calendar.DAY_OF_MONTH, 1);
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar endDate = (java.util.Calendar) date.clone();
        endDate.add(java.util.Calendar.MONTH, 1);

        List<VJournal> journals = getVjournals(new Period(date, endDate));
        Map<Integer, List<VJournal>> month_journals = new HashMap<Integer, List<VJournal>>();
        for (VJournal vj : journals) {
            List<Period> periods = vj.getPeriods(new Period(date, endDate));
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    Integer _day = Integer.valueOf(p.getStart().get(Calendar.DAY_OF_MONTH));
                    List<VJournal> vjournals = new ArrayList<VJournal>();
                    if (month_journals.containsKey(_day)) {
                        vjournals = month_journals.get(_day);
                    }
                    if (!vjournals.contains(vj)) {
                        vjournals.add(vj);
                    }
                    month_journals.put(_day, vjournals);
                }
            }
        }
        return month_journals;
    }

    /**
     * Return a list of recurrent VEvent objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VEvent> getRecurrenceVevents(final Period period) {
        List<VEvent> vevents = new ArrayList<VEvent>();
        for (Entry<String, VEvent> e : vevent.entrySet()) {
            VEvent ve = e.getValue();
            List<Period> periods = ve.getPeriods(period);
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    VEvent vep = new VEvent();
                    vep.setSummary(ve.getSummary());
                    vep.setDTStamp(ve.getDTStamp());
                    vep.setUid(ve.getUid());
                    vep.setDTStart(p.getStart());
                    vep.setRecurrenceId(p.getStart());
                    vep.setDuration(DateTime.getMillisBetween(p.getStart(), p.getEnd()));
                    vevents.add(vep);
                }
            }
        }
        return vevents;
    }

    /**
     * Return a list of recurrent VTodo objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VTodo> getRecurrenceVtodos(final Period period) {
        List<VTodo> vtodos = new ArrayList<VTodo>();
        for (Entry<String, VTodo> e : vtodo.entrySet()) {
            VTodo vt = e.getValue();
            List<Period> periods = vt.getPeriods(period);
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    VTodo vtp = new VTodo();
                    vtp.setSummary(vt.getSummary());
                    vtp.setDTStamp(vt.getDTStamp());
                    vtp.setUid(vt.getUid());
                    vtp.setDTStart(p.getStart());
                    vtp.setRecurrenceId(p.getStart());
                    vtp.setDuration(DateTime.getMillisBetween(p.getStart(), p.getEnd()));
                    vtodos.add(vtp);
                }
            }
        }
        return vtodos;
    }

    /**
     * Return the VTimeZone object
     * 
     * @return
     */
    public VTimeZone getTimeZone() {
        return vtimezone;
    }

    /**
     * Return a specific VEvent object
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public VEvent getVevent(final String uid) throws VCalendarException {
        if (uid != null && vevent.containsKey(uid)) {
            return vevent.get(uid);
        }
        throw new VCalendarException("vevent not found");
    }

    /**
     * Return all VEvent objects
     * 
     * @return
     */
    public List<VEvent> getVevents() {
        List<VEvent> values = new ArrayList<VEvent>();
        values.addAll(vevent.values());
        return values;
    }

    /**
     * Return VEvent objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VEvent> getVevents(final Period period) {
        Map<String, VEvent> vevents = new HashMap<String, VEvent>();
        for (Entry<String, VEvent> e : vevent.entrySet()) {
            VEvent ve = e.getValue();
            List<Period> _dates = ve.getPeriods(period);
            if (_dates != null && !_dates.isEmpty()) {
                if (!vevents.containsKey(ve.getUid())) {
                    vevents.put(ve.getUid(), ve);
                }
            }
        }
        return new ArrayList<VEvent>(vevents.values());
    }

    /**
     * Return a VFreeBusy object with the availability.
     * 
     * @return VFreeBusy
     */
    public VFreeBusy getVFreeBusy() {
        return vfreebusy;
    }

    /**
     * Return a VFreeBusy object for a specific time period.
     * 
     * @param period
     * @return
     */
    public VFreeBusy getVFreeBusy(final Period period) {
        VFreeBusy vfb = new VFreeBusy(vtimezone);
        vfb.setDTStart(period.getStart());
        vfb.setDTEnd(period.getEnd());
        for (Entry<String, VEvent> e : vevent.entrySet()) {
            VEvent ve = e.getValue();
            List<Period> periods = ve.getPeriodsBetween(period.getStart(), period.getEnd());
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    vfb.addBusy(p);
                }
            }
        }
        /*
         * for(String id : vtodo.keySet()) { VTodo vt = vtodo.get(id); List<Period> periods =
         * vt.getPeriods(period); if(periods != null && !periods.isEmpty()) { for(Period p :
         * periods) { vfb.addBusy(p); } } }
         */
        return vfb;
    }

    /**
     * Return a specific VJournal object
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public VJournal getVjournal(final String uid) throws VCalendarException {
        if (uid != null && vjournal.containsKey(uid)) {
            return vjournal.get(uid);
        }
        throw new VCalendarException("vjournal not found");
    }

    /**
     * Return all VJournal objects
     * 
     * @return
     */
    public List<VJournal> getVjournals() {
        List<VJournal> _values = new ArrayList<VJournal>();
        _values.addAll(vjournal.values());
        return _values;
    }

    /**
     * Return VJournal objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VJournal> getVjournals(final Period period) {
        Map<String, VJournal> vevents = new HashMap<String, VJournal>();
        for (Entry<String, VJournal> e : vjournal.entrySet()) {
            VJournal vj = e.getValue();
            List<Period> _periods = vj.getPeriods(period);
            if (_periods != null && !_periods.isEmpty()) {
                if (!vevents.containsKey(vj.getUid())) {
                    vevents.put(vj.getUid(), vj);
                }
            }
        }
        return new ArrayList<VJournal>(vevents.values());
    }

    /**
     * Return a specific VTodo object
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public VTodo getVtodo(final String uid) throws VCalendarException {
        if (uid != null && vtodo.containsKey(uid)) {
            return vtodo.get(uid);
        }
        throw new VCalendarException("vtodo not found");
    }

    /**
     * Return all VTodo objects
     * 
     * @return
     */
    public List<VTodo> getVtodos() {
        List<VTodo> values = new ArrayList<VTodo>();
        values.addAll(vtodo.values());
        return values;
    }

    /**
     * Return VTodo objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VTodo> getVtodos(final Period period) {
        Map<String, VTodo> vevents = new HashMap<String, VTodo>();
        for (Entry<String, VTodo> e : vtodo.entrySet()) {
            VTodo vt = e.getValue();
            List<Period> periods = vt.getPeriods(period);
            if (periods != null && !periods.isEmpty()) {
                if (!vevents.containsKey(vt.getUid())) {
                    vevents.put(vt.getUid(), vt);
                }
            }
        }
        return new ArrayList<VTodo>(vevents.values());
    }

    /**
     * Return a list of <code>Integer</code> with days in a month that contains VEvent objects
     * 
     * @param date
     * @param events
     * @return
     */
    public static List<Integer> getVeventMonthDays(final Calendar date, final List<VEvent> events) {
        Calendar endDate = (java.util.Calendar) date.clone();
        date.set(java.util.Calendar.DAY_OF_MONTH, 1);
        date.set(java.util.Calendar.HOUR, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 1);
        date.set(java.util.Calendar.MILLISECOND, 0);
        endDate.set(java.util.Calendar.DAY_OF_MONTH, endDate.getMaximum(java.util.Calendar.DAY_OF_MONTH));
        endDate.set(java.util.Calendar.HOUR, 23);
        endDate.set(java.util.Calendar.MINUTE, 59);
        endDate.set(java.util.Calendar.SECOND, 59);
        endDate.set(java.util.Calendar.MILLISECOND, 0);
        List<Integer> days = new ArrayList<Integer>();
        for (VEvent ve : events) {
            List<Period> periods = ve.getPeriods(new Period(date, endDate));
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    Integer _day = new Integer(p.getStart().get(java.util.Calendar.DAY_OF_MONTH));
                    if (!days.contains(_day)) {
                        days.add(_day);
                    }
                }
            }
        }
        return days;
    }

    /**
     * Return VEvent objects related to Calendar object week.
     * 
     * @param date
     * @return
     */
    public List<VEvent> getWeekVevents(final Calendar date) {
        Calendar endDate = (java.util.Calendar) date.clone();
        date.set(java.util.Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        endDate.set(java.util.Calendar.WEEK_OF_MONTH, 1);

        return getVevents(new Period(date, endDate));
    }

    /**
     * Return a map of VEvent objects related to the calendar object day. The key of this map is an
     * <code>Integer</code> with the week day.
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VEvent>> getWeekVeventsMap(final Calendar date) {
        date.set(java.util.Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
        date.set(java.util.Calendar.HOUR_OF_DAY, 0);
        date.set(java.util.Calendar.MINUTE, 0);
        date.set(java.util.Calendar.SECOND, 0);
        date.set(java.util.Calendar.MILLISECOND, 0);
        Calendar endDate = (java.util.Calendar) date.clone();
        endDate.set(java.util.Calendar.WEEK_OF_MONTH, 1);

        Map<Integer, List<VEvent>> week_events = new HashMap<Integer, List<VEvent>>();
        for (VEvent ve : getVevents(new Period(date, endDate))) {
            List<Period> periods = ve.getPeriods(new Period(date, endDate));
            if (periods != null && !periods.isEmpty()) {
                for (Period p : periods) {
                    Calendar offset = (Calendar) p.getStart().clone();
                    for (; offset.before(p.getEnd()) && offset.before(endDate); offset.add(Calendar.DAY_OF_WEEK, 1)) {
                        Map<String, VEvent> vevents = new HashMap<String, VEvent>();
                        if (week_events.containsKey(offset.get(Calendar.DAY_OF_WEEK))) {
                            for (VEvent _tve : week_events.get(offset.get(Calendar.DAY_OF_WEEK))) {
                                vevents.put(_tve.getUid(), _tve);
                            }
                        }
                        if (!vevents.containsKey(ve.getUid())) {
                            vevents.put(ve.getUid(), ve);
                        }
                        week_events.put(offset.get(Calendar.DAY_OF_WEEK), new ArrayList<VEvent>(vevents.values()));
                    }
                }
            }
        }
        return week_events;
    }

    /**
     * Check if a specific VEvent object exists and return a boolean value.
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public boolean hasVevent(final String uid) throws VCalendarException {
        if (uid != null && vevent.containsKey(uid)) {
            return true;
        }
        return false;
    }

    /**
     * Check if a specific VTodo object exists and return a boolean value.
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public boolean hasVtodo(final String uid) throws VCalendarException {
        if (uid != null && vtodo.containsKey(uid)) {
            return true;
        }
        return false;
    }

    private boolean isActiveStatus(final String status) {
        if (status == null) {
            return true;
        }
        List<String> active_status = new ArrayList<String>(Arrays.asList(new String[] { "NEEDS-ACTION", "IN-PROCESS" }));
        return active_status.contains(status.toUpperCase());
    }

    private void nextLine() throws IOException {
        line = "";
        if (buffer != null && buffer.ready()) {
            line = buffer.readLine();
        }
    }

    private void parse() throws IOException, VCalendarException {
        vtimezone = new VTimeZone(null);
        for (nextLine(); line != null; nextLine()) {
            if (line.startsWith("METHOD")) {
                method = line.substring(line.indexOf(":") + 1);
            } else if (line.startsWith("BEGIN:VTIMEZONE")) {
                /**
                 * VTIMEZONE
                 */
                parseVTimeZone();
            } else if (line.startsWith("BEGIN:VFREEBUSY")) {
                /**
                 * VFREEBUSY
                 */
                parseVFreeBusy();
            } else if (line.startsWith("BEGIN:VEVENT")) {
                /**
                 * VEVENT
                 */
                parseVEvent();
            } else if (line.indexOf("BEGIN:VTODO") != -1) {
                /**
                 * VTODO
                 */
                parseVTodo();
            } else if (line.indexOf("BEGIN:VJOURNAL") != -1) {
                /**
                 * VJournal
                 */
                parseVJournal();
            }
        }
    }

    private void parseVAlarm(final VEvent ve) throws VCalendarException {
        VAlarm va = new VAlarm();
        try {
            for (nextLine(); line != null; nextLine()) {
                if (line.startsWith("END:VALARM")) {
                    ve.addAlarm(va);
                    break;
                } else {
                    if (line.startsWith("TRIGGER")) {
                        va.setTrigger(new Trigger(line));
                    } else if (line.startsWith("REPEAT")) {
                        line = line.substring(line.indexOf(":") + 1);
                        try {
                            va.setRepeat(Integer.parseInt(line));
                        } catch (NumberFormatException e) {
                        }
                    } else if (line.startsWith("DURATION")) {
                        line = line.substring(line.lastIndexOf(":") + 1);
                        va.setDuration(new Duration(line));
                    } else if (line.startsWith("DESCRIPTION")) {
                        line = line.substring(line.lastIndexOf(":") + 1);
                        va.setDescription(line);
                    } else if (line.startsWith("ACTION")) {
                        line = line.substring(line.lastIndexOf(":") + 1);
                        va.setAction(line);
                    } else if (line.startsWith("X-")) {
                        va.addExtended(line);
                    }
                }
            }
        } catch (Exception e) {
            throw new VCalendarException("VCALENDAR::VEVENT::VALARM::error::" + line);
        }
    }

    private void parseVFreeBusy() throws IOException, VCalendarException {
        VFreeBusy vfb = new VFreeBusy(vtimezone);
        for (nextLine(); line != null; nextLine()) {
            if (line.startsWith("END:VFREEBUSY")) {
                vfreebusy = vfb;
                break;
            } else if (line.startsWith("DTSTART")) {
                try {
                    line = line.substring(line.indexOf(":") + 1);
                    vfb.setDTStart(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                } catch (Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::DTSTART::error::" + line);
                }
            } else if (line.startsWith("DTEND")) {
                try {
                    line = line.substring(line.indexOf(":") + 1);
                    vfb.setDTEnd(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                } catch (Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::DTEND::error::" + line);
                }
            } else if (line.startsWith("ATTENDEE") && line.indexOf(":") > 0) {
                try {
                    Person p = new Person(line, Person.ATTENDEE);
                    vfb.setAttendee(p.getMailTo(), p);
                } catch (Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::ATTENDEE::error::" + line);
                }
            } else if (line.startsWith("ORGANIZER") && line.indexOf(":") > 0) {
                try {
                    Person p = new Person(line, Person.ORGANIZER);
                    vfb.setOrganizer(p.getMailTo(), p);
                } catch (Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::ORGANIZER::error::" + line);
                }
            } else if (line.startsWith("FREEBUSY")) {
                line = line.substring(line.indexOf(":") + 1);
                try {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    while (st.hasMoreTokens()) {
                        String t = st.nextToken();
                        if (t.contains("/")) {
                            try {
                                Calendar start = DateTime.getCalendarFromString(vtimezone.getTimeZone(),
                                        t.substring(0, t.indexOf("/")));
                                Calendar end = DateTime.getCalendarFromString(vtimezone.getTimeZone(),
                                        t.substring(t.indexOf("/") + 1));
                                vfb.addBusy(new Period(start, end));
                            } catch (Exception e) {
                                Calendar start = DateTime.getCalendarFromString(vtimezone.getTimeZone(),
                                        t.substring(0, t.indexOf("/")));
                                Duration d = new Duration(t.substring(t.indexOf("/") + 1));
                                Calendar end = Calendar.getInstance();
                                end.setTimeInMillis(start.getTimeInMillis() + d.getMilliseconds());
                                vfb.addBusy(new Period(start, end));
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::FREEBUSY::error::" + line);
                }
            }
        }
    }

    private void parseVEvent() throws IOException, VCalendarException {
        VEvent ve = new VEvent();
        for (nextLine(); line != null; nextLine()) {
            if (line.startsWith("END:VEVENT")) {
                if (vevent.containsKey(ve.getUid())) {
                    if (!vevent.get(ve.getUid()).hasRecurrence()) {
                        vevent.put(ve.getUid(), ve);
                    }
                } else {
                    vevent.put(ve.getUid(), ve);
                }
                break;
            } else if (line.startsWith("BEGIN:VALARM")) {
                /**
                 * VALARM
                 */
                parseVAlarm(ve);
            } else {
                if (line.startsWith("CATEGORIES")) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        StringTokenizer st = new StringTokenizer(line, ",");
                        if (st.countTokens() > 0) {
                            while (st.hasMoreTokens()) {
                                ve.addCategory(st.nextToken());
                            }
                        }
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::CATEGORIES::error::" + line);
                    }
                } else if (line.startsWith("SUMMARY") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setSummary(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::LOCATION::error::" + line);
                    }
                } else if (line.startsWith("LOCATION") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setLocation(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::LOCATION::error::" + line);
                    }
                } else if (line.startsWith("CREATED") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setCreated(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::CREATED::error::" + line);
                    }
                } else if (line.startsWith("LAST-MODIFIED") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setLastModified(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::LAST-MODIFIED::error::" + line);
                    }
                } else if (line.startsWith("DESCRIPTION") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setDescription(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DESCRIPTION::error::" + line);
                    }
                } else if (line.startsWith("DTSTAMP") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setDTStamp(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DTSTAMP::error::" + line);
                    }
                } else if (line.startsWith("UID") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setUid(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::UID::error::" + line);
                    }
                } else if (line.startsWith("DTSTART") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setDTStart(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DTSTART::error::" + line);
                    }
                } else if (line.startsWith("DTEND") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setDTEnd(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DTEND::error::" + line);
                    }
                } else if (line.startsWith("EXDATE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.addExDate(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::EXDATE::error::" + line);
                    }
                } else if (line.startsWith("STATUS") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setStatus(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::STATUS::error::" + line);
                    }
                } else if (line.startsWith("CLASS") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setClassType(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::CLASS::error::" + line);
                    }
                } else if (line.startsWith("ATTENDEE") && line.indexOf(":") > 0) {
                    try {
                        Person p = new Person(line, Person.ATTENDEE);
                        ve.setAttendee(p.getMailTo(), p);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::ATTENDEE::error::" + line);
                    }
                } else if (line.startsWith("ORGANIZER") && line.indexOf(":") > 0) {
                    try {
                        Person p = new Person(line, Person.ORGANIZER);
                        ve.setOrganizer(p.getMailTo(), p);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::ORGANIZER::error::" + line);
                    }
                } else if (line.startsWith("RRULE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        ve.setRRule(parseRRuleFromLine(line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::RRULE::error::" + line);
                    }
                } else if (line.startsWith("X-")) {
                    ve.addExtended(line);
                }
            }
        }
    }

    private void parseVJournal() throws IOException, VCalendarException {
        VJournal vj = new VJournal();
        for (nextLine(); line != null; nextLine()) {
            if (line.isEmpty()) {
                nextLine();
            }
            if (line.indexOf("END:VJOURNAL") != -1) {
                vjournal.put(vj.getUid(), vj);
                break;
            } else {
                if (line.startsWith("CATEGORIES")) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        StringTokenizer st = new StringTokenizer(line, ",");
                        if (st.countTokens() > 0) {
                            while (st.hasMoreTokens()) {
                                vj.addCategory(st.nextToken());
                            }
                        }
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::CATEGORIES::error::" + line);
                    }
                } else if (line.startsWith("SUMMARY") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setSummary(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::SUMMARY::error::" + line);
                    }
                } else if (line.startsWith("DESCRIPTION") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setDescription(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::DESCRIPTION::error::" + line);
                    }
                } else if (line.startsWith("CREATED") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setCreated(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::CREATED::error::" + line);
                    }
                } else if (line.startsWith("UID") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setUid(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::UID::error::" + line);
                    }
                } else if (line.startsWith("DTSTART") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setDTStart(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::DTSTART::error::" + line);
                    }
                } else if (line.startsWith("STATUS") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setStatus(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::STATUS::error::" + line);
                    }
                } else if (line.startsWith("CLASS") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setClassType(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::CLASS::error::" + line);
                    }
                } else if (line.startsWith("ATTENDEE") && line.indexOf(":") > 0) {
                    try {
                        Person p = new Person(line, Person.ATTENDEE);
                        vj.setAttendee(p.getMailTo(), p);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::ATTENDEE::error::" + line);
                    }
                } else if (line.startsWith("ORGANIZER") && line.indexOf(":") > 0) {
                    try {
                        Person p = new Person(line, Person.ORGANIZER);
                        vj.setOrganizer(p.getMailTo(), p);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::ORGANIZER::error::" + line);
                    }
                } else if (line.startsWith("RRULE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vj.setRRule(parseRRuleFromLine(line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::RRULE::error::" + line);
                    }
                } else if (line.startsWith("X-")) {
                    vj.addExtended(line);
                }
            }
        }
    }

    private void parseVTimeZone() throws IOException, VCalendarException {
        VTimeZone vtz = new VTimeZone(null);
        for (nextLine(); line != null; nextLine()) {
            if (line.startsWith("END:VTIMEZONE")) {
                vtimezone = vtz;
                break;
            } else if (line.startsWith("TZID")) {
                line = line.substring(line.indexOf(":") + 1);
                vtz.setTZID(line);
            } else if (line.startsWith("BEGIN:STANDARD")) {
                for (nextLine(); line != null; nextLine()) {
                    if (line.startsWith("END:STANDARD")) {
                        break;
                    } else if (line.startsWith("RRULE")) {
                        line = line.substring(line.indexOf(":") + 1);
                        vtz.setStandardRRule(parseRRuleFromLine(line));
                    }
                }
            } else if (line.startsWith("BEGIN:DAYLIGHT")) {
                for (nextLine(); line != null; nextLine()) {
                    if (line.startsWith("END:DAYLIGHT")) {
                        break;
                    } else if (line.startsWith("RRULE")) {
                        line = line.substring(line.indexOf(":") + 1);
                        vtz.setDayLightRRule(parseRRuleFromLine(line));
                    }
                }
            }
        }
    }

    private void parseVTodo() throws IOException, VCalendarException {
        VTodo vt = new VTodo();
        for (nextLine(); line != null; nextLine()) {
            if (line.isEmpty()) {
                nextLine();
            }
            if (line.indexOf("END:VTODO") != -1) {
                vtodo.put(vt.getUid(), vt);
                break;
            } else if (line.startsWith("BEGIN:VALARM")) {
                VAlarm va = new VAlarm();
                try {
                    for (nextLine(); line != null; nextLine()) {
                        if (line.startsWith("END:VALARM")) {
                            vt.addAlarm(va);
                            break;
                        } else {
                            if (line.startsWith("TRIGGER")) {
                                va.setTrigger(new Trigger(line));
                            } else if (line.startsWith("REPEAT")) {
                                line = line.substring(line.indexOf(":") + 1);
                                try {
                                    va.setRepeat(Integer.parseInt(line));
                                } catch (NumberFormatException e) {
                                }
                            } else if (line.startsWith("DURATION")) {
                                line = line.substring(line.lastIndexOf(":") + 1);
                                va.setDuration(new Duration(line));
                            } else if (line.startsWith("DESCRIPTION")) {
                                line = line.substring(line.lastIndexOf(":") + 1);
                                va.setDescription(line);
                            } else if (line.startsWith("ACTION")) {
                                line = line.substring(line.lastIndexOf(":") + 1);
                                va.setAction(line);
                            } else if (line.startsWith("X-")) {
                                va.addExtended(line);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new VCalendarException("VCALENDAR::VTODO::VALARM::error::" + line);
                }
            } else {
                if (line.startsWith("CATEGORIES")) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        StringTokenizer _st = new StringTokenizer(line, ",");
                        if (_st.countTokens() > 0) {
                            while (_st.hasMoreTokens()) {
                                vt.addCategory(_st.nextToken());
                            }
                        }
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::CATEGORIES::error::" + line);
                    }
                } else if (line.startsWith("SUMMARY") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setSummary(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::SUMMARY::error::" + line);
                    }
                } else if (line.startsWith("LOCATION") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setLocation(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::LOCATION::error::" + line);
                    }
                } else if (line.startsWith("CREATED") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setCreated(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::CREATED::error::" + line);
                    }
                } else if (line.startsWith("LAST-MODIFIED") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setLastModified(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::LAST-MODIFIED::error::" + line);
                    }
                } else if (line.startsWith("DESCRIPTION") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setDescription(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::DESCRIPTION::error::" + line);
                    }
                } else if (line.startsWith("DTSTAMP") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setDTStamp(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::LAST-MODIFIED::error::" + line);
                    }
                } else if (line.startsWith("DUE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setDue(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::DUE::error::" + line);
                    }
                } else if (line.startsWith("UID") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setUid(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::UID::error::" + line);
                    }
                } else if (line.startsWith("DTSTART") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setDTStart(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::DTSTART::error::" + line);
                    }
                } else if (line.startsWith("EXDATE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.addExDate(DateTime.getCalendarFromString(vtimezone.getTimeZone(), line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::EXDATE::error::" + line);
                    }
                } else if (line.startsWith("STATUS") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setStatus(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::STATUS::error::" + line);
                    }
                } else if (line.startsWith("PERCENT-COMPLETE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        try {
                            vt.setPercent(Integer.parseInt(line));
                        } catch (NumberFormatException e) {
                        }
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::PERCENT-COMPLETE::error::" + line);
                    }
                } else if (line.startsWith("CLASS") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setClassType(line);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::CLASS::error::" + line);
                    }
                } else if (line.startsWith("ATTENDEE") && line.indexOf(":") > 0) {
                    try {
                        Person p = new Person(line, Person.ATTENDEE);
                        vt.setAttendee(p.getMailTo(), p);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::ATTENDEE::error::" + line);
                    }
                } else if (line.startsWith("ORGANIZER") && line.indexOf(":") > 0) {
                    try {
                        Person p = new Person(line, Person.ORGANIZER);
                        vt.setOrganizer(p.getMailTo(), p);
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::ORGANIZER::error::" + line);
                    }
                } else if (line.startsWith("RRULE") && line.indexOf(":") > 0) {
                    try {
                        line = line.substring(line.indexOf(":") + 1);
                        vt.setRRule(parseRRuleFromLine(line));
                    } catch (Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::RRULE::error::" + line);
                    }
                } else if (line.startsWith("X-")) {
                    vt.addExtended(line);
                }
            }
        }
    }

    private RRule parseRRuleFromLine(final String line) throws VCalendarException {
        RRule rrule = new RRule();
        StringTokenizer st = new StringTokenizer(line, ";");
        while (st.hasMoreTokens()) {
            String part = st.nextToken();
            if (part.startsWith("FREQ=")) {
                part = part.substring(part.indexOf("=") + 1);
                rrule.setFrequency(part);
            } else if (part.startsWith("INTERVAL=")) {
                part = part.substring(part.indexOf("=") + 1);
                try {
                    rrule.setInterval(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                }
            } else if (part.startsWith("COUNT=")) {
                part = part.substring(part.indexOf("=") + 1);
                try {
                    rrule.setCount(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                }
            } else if (part.startsWith("UNTIL=")) {
                part = part.substring(part.indexOf("=") + 1);
                if (vtimezone != null) {
                    rrule.setUntil(DateTime.getCalendarFromString(vtimezone.getTimeZone(), part));
                } else {
                    rrule.setUntil(DateTime.getCalendarFromString(null, part));
                }
            } else if (part.startsWith("WKST=")) {
                part = part.substring(part.indexOf("=") + 1);
                rrule.setWeekStart(part);
            } else if (part.startsWith("BYMINUTE=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (NumberFormatException e) {
                    }
                }
                rrule.setByMinute(_values);
            } else if (part.startsWith("BYHOUR=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (NumberFormatException e) {
                    }
                }
                rrule.setByHour(_values);
            } else if (part.startsWith("BYDAY=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<String> _values = new ArrayList<String>();
                while (_stt.hasMoreTokens()) {
                    _values.add(_stt.nextToken().toUpperCase());
                }
                rrule.setByDay(_values);
            } else if (part.startsWith("BYMONTH=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (NumberFormatException e) {
                    }
                }
                rrule.setByMonth(_values);
            } else if (part.startsWith("BYMONTHDAY=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (NumberFormatException e) {
                    }
                }
                rrule.setByMonthDay(_values);
            } else if (part.startsWith("BYYEARDAY=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (NumberFormatException e) {
                    }
                }
                rrule.setByYearDay(_values);
            } else if (part.startsWith("BYWEEKNO=")) {
                part = part.substring(part.indexOf("=") + 1);
                StringTokenizer _stt = new StringTokenizer(part, ",");
                List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (NumberFormatException e) {
                    }
                }
                rrule.setByWeekNo(_values);
            }
        }
        return rrule;
    }

    private static final byte[] readBytes(final InputStream is) throws IOException {
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bufferInput = new BufferedInputStream(is);
        try {
            for (int i = bufferInput.read(buffer); i >= 0; i = bufferInput.read(buffer)) {
                baos.write(buffer, 0, i);
            }
        } finally {
            bufferInput.close();
        }
        return baos.toByteArray();
    }

    /**
     * Remove an VEvent object
     * 
     * @param uid
     * @return
     */
    public boolean removeVevent(final String uid) {
        if (vevent.remove(uid) != null) {
            return true;
        }
        return false;
    }

    /**
     * Remove an VJournal object
     * 
     * @param uid
     * @return
     */
    public boolean removeVJournal(final String uid) {
        if (vjournal.remove(uid) != null) {
            return true;
        }
        return false;
    }

    /**
     * Remove an VTodo object
     * 
     * @param uid
     * @return
     */
    public boolean removeVtodo(final String uid) {
        if (vtodo.remove(uid) != null) {
            return true;
        }
        return false;
    }

    /**
     * Set icalendar file
     * 
     * @param icalendar
     */
    public void setFile(final File icalendar) {
        ical_file = icalendar;
    }

    /**
     * Set method
     * 
     * @param method
     */
    public void setMethod(final String method) {
        this.method = method;
    }

    /**
     * Set the VTimeZone object
     * 
     * @param timezone
     */
    public void setTimeZone(final VTimeZone timezone) {
        vtimezone = timezone;
    }

    @Override
    public String toString() {
        final String CRLF = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR");
        sb.append(CRLF);
        sb.append("VERSION:" + version);
        sb.append(CRLF);
        sb.append("PRODID:" + prodid);
        sb.append(CRLF);
        if (method != null) {
            sb.append("METHOD:" + method);
            sb.append(CRLF);
        }

        if (vtimezone != null) {
            sb.append(vtimezone.toString());
        }

        for (VEvent ve : getVevents()) {
            sb.append(ve.toString(vtimezone));
        }

        for (VTodo vt : getVtodos()) {
            sb.append(vt.toString(vtimezone));
        }

        for (VJournal vj : getVjournals()) {
            sb.append(vj.toString(vtimezone));
        }

        sb.append("END:VCALENDAR");
        sb.append(CRLF);

        return sb.toString();
    }

    /**
     * Update an VEvent object
     * 
     * @param ve
     * @throws VCalendarException
     */
    public void updateVevent(final VEvent ve) throws VCalendarException {
        if (!vevent.containsKey(ve.getUid())) {
            throw new VCalendarException("VEvent not found");
        }
        vevent.put(ve.getUid(), ve);
    }

    /**
     * Update an VJournal object
     * 
     * @param vj
     * @throws VCalendarException
     */
    public void updateVjournal(final VJournal vj) throws VCalendarException {
        if (!vjournal.containsKey(vj.getUid())) {
            throw new VCalendarException("VJournal not found");
        }
        vjournal.put(vj.getUid(), vj);
    }

    /**
     * Update an VTodo object
     * 
     * @param vt
     * @throws VCalendarException
     */
    public void updateVtodo(final VTodo vt) throws VCalendarException {
        if (!vtodo.containsKey(vt.getUid())) {
            throw new VCalendarException("VTodo not found");
        }
        vtodo.put(vt.getUid(), vt);
    }

    /**
     * Writes the icalendar file
     * 
     * @throws VCalendarException
     */
    public void write() throws VCalendarException {
        if (ical_file != null && ical_file.canWrite()) {
            try {
                FileUtils.writeFile(ical_file, toString());
            } catch (FileLockException e) {
                throw new VCalendarException(e);
            } catch (IOException e) {
                throw new VCalendarException(e);
            }
        }
    }
}
