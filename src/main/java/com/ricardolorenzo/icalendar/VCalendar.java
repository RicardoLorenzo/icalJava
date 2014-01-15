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

    /**
     * Return a list of <code>Integer</code> with days in a month that contains VEvent objects
     * 
     * @param date
     * @param events
     * @return
     */
    public static List<Integer> getVeventMonthDays(final Calendar date, final List<VEvent> events) {
        final Calendar endDate = (java.util.Calendar) date.clone();
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
        final List<Integer> days = new ArrayList<Integer>();
        for (final VEvent ve : events) {
            final List<Period> periods = ve.getPeriods(new Period(date, endDate));
            if ((periods != null) && !periods.isEmpty()) {
                for (final Period p : periods) {
                    final Integer _day = new Integer(p.getStart().get(java.util.Calendar.DAY_OF_MONTH));
                    if (!days.contains(_day)) {
                        days.add(_day);
                    }
                }
            }
        }
        return days;
    }

    private static final byte[] readBytes(final InputStream is) throws IOException {
        final byte[] buffer = new byte[2048];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BufferedInputStream bufferInput = new BufferedInputStream(is);
        try {
            for (int i = bufferInput.read(buffer); i >= 0; i = bufferInput.read(buffer)) {
                baos.write(buffer, 0, i);
            }
        } finally {
            bufferInput.close();
        }
        return baos.toByteArray();
    }

    private VTimeZone vtimezone;
    private VFreeBusy vfreebusy;
    private final Map<String, VEvent> vevent;
    private final Map<String, VTodo> vtodo;
    private final Map<String, VJournal> vjournal;
    private String method;
    transient private File ical_file;

    transient private String line;

    transient private BufferedReader buffer;

    public VCalendar() throws VCalendarException {
        this.vtimezone = new VTimeZone(null);
        this.vevent = new HashMap<String, VEvent>();
        this.vtodo = new HashMap<String, VTodo>();
        this.vjournal = new HashMap<String, VJournal>();
    }

    public VCalendar(final File icalendar) throws VCalendarException {
        this();
        this.ical_file = icalendar;

        if (this.ical_file.exists()) {
            try {
                final InputStream is = new BufferedInputStream(new ByteArrayInputStream(readBytes(new FileInputStream(
                        this.ical_file))));
                this.buffer = new BufferedReader(new InputStreamReader(is));
                try {
                    parse();
                } finally {
                    this.buffer.close();
                }
            } catch (final IOException e) {
                throw new VCalendarException(e);
            }
        }
    }

    public VCalendar(final InputStream is) throws VCalendarException {
        this();
        try {
            this.buffer = new BufferedReader(new InputStreamReader(is));
            try {
                parse();
            } finally {
                this.buffer.close();
            }
        } catch (final IOException e) {
            throw new VCalendarException(e);
        }
    }

    public VCalendar(final String content) throws VCalendarException {
        this();
        try {
            this.buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.trim().getBytes())));
            try {
                parse();
            } finally {
                this.buffer.close();
            }
        } catch (final IOException e) {
            throw new VCalendarException(e);
        }
    }

    /**
     * Add VEvent object
     * 
     * @param ve
     */
    public void addVevent(final VEvent ve) {
        this.vevent.put(ve.getUid(), ve);
    }

    /**
     * Add VJournal object
     * 
     * @param vj
     */
    public void addVjournal(final VJournal vj) {
        this.vjournal.put(vj.getUid(), vj);
    }

    /**
     * Add VTodo object
     * 
     * @param vt
     */
    public void addVtodo(final VTodo vt) {
        this.vtodo.put(vt.getUid(), vt);
    }

    /**
     * Return all active VTodo objects
     * 
     * @return
     */
    public List<VTodo> getActiveVtodos() {
        final List<VTodo> vtodos = new ArrayList<VTodo>();
        for (final Entry<String, VTodo> e : this.vtodo.entrySet()) {
            final VTodo _vt = e.getValue();
            if (isActiveStatus(_vt.getStatus())) {
                vtodos.add(_vt);
            }
        }
        return vtodos;
    }

    /**
     * Return the method
     * 
     * @return
     */
    public String getMethod() {
        if (this.method == null) {
            return "";
        }
        return this.method;
    }

    /**
     * Return a list of recurrent VEvent objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VEvent> getRecurrentVevents(final Period period) {
        final List<VEvent> vevents = new ArrayList<VEvent>();
        for (final Entry<String, VEvent> e : this.vevent.entrySet()) {
            final VEvent ve = e.getValue();
            final List<Period> periods = ve.getPeriods(period);
            if ((periods != null) && !periods.isEmpty()) {
                for (final Period p : periods) {
                    final VEvent vep = new VEvent();
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
    public List<VTodo> getRecurrentVtodos(final Period period) {
        final List<VTodo> vtodos = new ArrayList<VTodo>();
        for (final Entry<String, VTodo> e : this.vtodo.entrySet()) {
            final VTodo vt = e.getValue();
            final List<Period> periods = vt.getPeriods(period);
            if ((periods != null) && !periods.isEmpty()) {
                for (final Period p : periods) {
                    final VTodo vtp = new VTodo();
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
        return this.vtimezone;
    }

    /**
     * Return a specific VEvent object
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public VEvent getVevent(final String uid) throws VCalendarException {
        if ((uid != null) && this.vevent.containsKey(uid)) {
            return this.vevent.get(uid);
        }
        throw new VCalendarException("vevent not found");
    }

    /**
     * Return all VEvent objects
     * 
     * @return
     */
    public List<VEvent> getVevents() {
        final List<VEvent> values = new ArrayList<VEvent>();
        values.addAll(this.vevent.values());
        return values;
    }

    /**
     * Return VEvent objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VEvent> getVevents(final Period period) {
        final Map<String, VEvent> vevents = new HashMap<String, VEvent>();
        for (final Entry<String, VEvent> e : this.vevent.entrySet()) {
            final VEvent ve = e.getValue();
            final List<Period> _dates = ve.getPeriods(period);
            if ((_dates != null) && !_dates.isEmpty()) {
                if (!vevents.containsKey(ve.getUid())) {
                    vevents.put(ve.getUid(), ve);
                }
            }
        }
        return new ArrayList<VEvent>(vevents.values());
    }

    /**
     * Return all VEvent objects related to the calendar object day.
     * 
     * @param date
     * @return
     */
    public List<VEvent> getVeventsForDay(final Calendar date) {
        return getVevents(Period.getDayPeriod(date));
    }

    /**
     * Return all VEvent objects related to the calendar object month.
     * 
     * @param date
     * @return
     */
    public List<VEvent> getVeventsForMonth(final Calendar date) {
        return getVevents(Period.getMonthPeriod(date));
    }

    /**
     * Return VEvent objects related to Calendar object week.
     * 
     * @param date
     * @return
     */
    public List<VEvent> getVeventsForWeek(final Calendar date) {
        return getVevents(Period.getWeekPeriod(date));
    }

    /**
     * Return a map of VEvent objects related to the calendar object day. The key of this map is an
     * <code>Integer</code> with the day hour (24h format).
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VEvent>> getVeventsMapForDay(final Calendar date) {
        final Period pdate = Period.getDayPeriod(date);
        final Map<Integer, List<VEvent>> day_events = new HashMap<Integer, List<VEvent>>();
        for (final VEvent ve : getVevents()) {
            final List<Period> periods = ve.getPeriods(pdate);
            if (periods != null) {
                for (final Period p : periods) {
                    final Map<String, VEvent> vevents = new HashMap<String, VEvent>();
                    final Integer day = Integer.valueOf(p.getStart().get(Calendar.DAY_OF_MONTH));
                    if (day_events.containsKey(day)) {
                        for (final VEvent existant_ve : day_events.get(day)) {
                            vevents.put(existant_ve.getUid(), existant_ve);
                        }
                    }
                    if (!vevents.containsKey(ve.getUid())) {
                        vevents.put(ve.getUid(), ve);
                    }
                    day_events.put(day, new ArrayList<VEvent>(vevents.values()));
                }
            }
        }
        return day_events;
    }

    /**
     * Return a map of VEvent objects related to the calendar object month. The key of this map is
     * an <code>Integer</code> with the month day hour.
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VEvent>> getVeventsMapForMonth(final Calendar date) {
        final Period pdate = Period.getMonthPeriod(date);
        final Map<Integer, List<VEvent>> month_events = new HashMap<Integer, List<VEvent>>();
        for (final VEvent ve : getVevents(pdate)) {
            final List<Period> periods = ve.getPeriods(pdate);
            if (periods != null) {
                for (final Period p : periods) {
                    final Map<String, VEvent> vevents = new HashMap<String, VEvent>();
                    for (final Integer day : p.getMonthDays(date)) {
                        if (month_events.containsKey(day)) {
                            for (final VEvent existant_ve : month_events.get(day)) {
                                vevents.put(existant_ve.getUid(), existant_ve);
                            }
                        }
                        if (!vevents.containsKey(ve.getUid())) {
                            vevents.put(ve.getUid(), ve);
                        }
                        month_events.put(day, new ArrayList<VEvent>(vevents.values()));
                    }
                }
            }
        }
        return month_events;
    }

    /**
     * Return a map of VEvent objects related to the calendar object day. The key of this map is an
     * <code>Integer</code> with the week day.
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VEvent>> getVeventsMapForWeek(final Calendar date) {
        final Period pdate = Period.getWeekPeriod(date);
        final Map<Integer, List<VEvent>> week_events = new HashMap<Integer, List<VEvent>>();
        for (final VEvent ve : getVevents(pdate)) {
            final List<Period> periods = ve.getPeriods(pdate);
            if ((periods != null) && !periods.isEmpty()) {
                for (final Period p : periods) {
                    final Map<String, VEvent> vevents = new HashMap<String, VEvent>();
                    for (final Integer day : p.getWeekDays(date)) {
                        if (week_events.containsKey(day)) {
                            for (final VEvent existant_ve : week_events.get(day)) {
                                vevents.put(existant_ve.getUid(), existant_ve);
                            }
                        }
                        if (!vevents.containsKey(ve.getUid())) {
                            vevents.put(ve.getUid(), ve);
                        }
                        week_events.put(day, new ArrayList<VEvent>(vevents.values()));
                    }
                }
            }
        }
        return week_events;
    }

    /**
     * Return a VFreeBusy object with the availability.
     * 
     * @return VFreeBusy
     */
    public VFreeBusy getVFreeBusy() {
        return this.vfreebusy;
    }

    /**
     * Return a VFreeBusy object for a specific time period.
     * 
     * @param period
     * @return
     */
    public VFreeBusy getVFreeBusy(final Period period) {
        final VFreeBusy vfb = new VFreeBusy(this.vtimezone);
        vfb.setDTStart(period.getStart());
        vfb.setDTEnd(period.getEnd());
        for (final Entry<String, VEvent> e : this.vevent.entrySet()) {
            final VEvent ve = e.getValue();
            final List<Period> periods = ve.getPeriodsBetween(period.getStart(), period.getEnd());
            if ((periods != null) && !periods.isEmpty()) {
                for (final Period p : periods) {
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
        if ((uid != null) && this.vjournal.containsKey(uid)) {
            return this.vjournal.get(uid);
        }
        throw new VCalendarException("vjournal not found");
    }

    /**
     * Return all VJournal objects
     * 
     * @return
     */
    public List<VJournal> getVjournals() {
        final List<VJournal> _values = new ArrayList<VJournal>();
        _values.addAll(this.vjournal.values());
        return _values;
    }

    /**
     * Return VJournal objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VJournal> getVjournals(final Period period) {
        final Map<String, VJournal> vevents = new HashMap<String, VJournal>();
        for (final Entry<String, VJournal> e : this.vjournal.entrySet()) {
            final VJournal vj = e.getValue();
            final List<Period> _periods = vj.getPeriods(period);
            if ((_periods != null) && !_periods.isEmpty()) {
                if (!vevents.containsKey(vj.getUid())) {
                    vevents.put(vj.getUid(), vj);
                }
            }
        }
        return new ArrayList<VJournal>(vevents.values());
    }

    /**
     * Return all VJournal objects related to the calendar object day.
     * 
     * @param date
     * @return
     */
    public List<VJournal> getVjournalsForDay(final Calendar date) {
        return getVjournals(Period.getDayPeriod(date));
    }

    /**
     * Return all VJournal objects related to the calendar object month.
     * 
     * @param date
     * @return
     */
    public List<VJournal> getVjournalsForMonth(final Calendar date) {
        return getVjournals(Period.getMonthPeriod(date));
    }

    /**
     * Return a VJournal map related to the calendar object month. The key of the map is an
     * <code>Integer</code> with the month day.
     * 
     * @param date
     * @return
     */
    public Map<Integer, List<VJournal>> getVjournalsMapForMonth(final Calendar date) {
        final Period pdate = Period.getMonthPeriod(date);
        final List<VJournal> journals = getVjournals(pdate);
        final Map<Integer, List<VJournal>> month_journals = new HashMap<Integer, List<VJournal>>();
        for (final VJournal vj : journals) {
            final List<Period> periods = vj.getPeriods(pdate);
            if ((periods != null) && !periods.isEmpty()) {
                for (final Period p : periods) {
                    final Map<String, VJournal> vjournals = new HashMap<String, VJournal>();
                    for (final Integer day : p.getMonthDays(date)) {
                        if (month_journals.containsKey(day)) {
                            for (final VJournal existant_vj : month_journals.get(day)) {
                                vjournals.put(existant_vj.getUid(), existant_vj);
                            }
                        }
                        if (!vjournals.containsKey(vj.getUid())) {
                            vjournals.put(vj.getUid(), vj);
                        }
                        month_journals.put(day, new ArrayList<VJournal>(vjournals.values()));
                    }
                }
            }
        }
        return month_journals;
    }

    /**
     * Return a specific VTodo object
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public VTodo getVtodo(final String uid) throws VCalendarException {
        if ((uid != null) && this.vtodo.containsKey(uid)) {
            return this.vtodo.get(uid);
        }
        throw new VCalendarException("vtodo not found");
    }

    /**
     * Return all VTodo objects
     * 
     * @return
     */
    public List<VTodo> getVtodos() {
        final List<VTodo> values = new ArrayList<VTodo>();
        values.addAll(this.vtodo.values());
        return values;
    }

    /**
     * Return VTodo objects for a specific time period.
     * 
     * @param period
     * @return
     */
    public List<VTodo> getVtodos(final Period period) {
        final Map<String, VTodo> vevents = new HashMap<String, VTodo>();
        for (final Entry<String, VTodo> e : this.vtodo.entrySet()) {
            final VTodo vt = e.getValue();
            final List<Period> periods = vt.getPeriods(period);
            if ((periods != null) && !periods.isEmpty()) {
                if (!vevents.containsKey(vt.getUid())) {
                    vevents.put(vt.getUid(), vt);
                }
            }
        }
        return new ArrayList<VTodo>(vevents.values());
    }

    /**
     * Return all VTodo objects related to the calendar object day.
     * 
     * @param date
     * @return
     */
    public List<VTodo> getVtodosForDay(final Calendar date) {
        return getVtodos(Period.getDayPeriod(date));
    }

    /**
     * Check if a specific VEvent object exists and return a boolean value.
     * 
     * @param uid
     * @return
     * @throws VCalendarException
     */
    public boolean hasVevent(final String uid) throws VCalendarException {
        if ((uid != null) && this.vevent.containsKey(uid)) {
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
        if ((uid != null) && this.vtodo.containsKey(uid)) {
            return true;
        }
        return false;
    }

    private boolean isActiveStatus(final String status) {
        if (status == null) {
            return true;
        }
        final List<String> active_status = new ArrayList<String>(Arrays.asList(new String[] { "NEEDS-ACTION",
                "IN-PROCESS" }));
        return active_status.contains(status.toUpperCase());
    }

    private void nextLine() throws IOException {
        this.line = "";
        if ((this.buffer != null) && this.buffer.ready()) {
            this.line = this.buffer.readLine();
        }
    }

    private void parse() throws IOException, VCalendarException {
        this.vtimezone = new VTimeZone(null);
        for (nextLine(); this.line != null; nextLine()) {
            if (this.line.startsWith("METHOD")) {
                this.method = this.line.substring(this.line.indexOf(":") + 1);
            } else if (this.line.startsWith("BEGIN:VTIMEZONE")) {
                /**
                 * VTIMEZONE
                 */
                parseVTimeZone();
            } else if (this.line.startsWith("BEGIN:VFREEBUSY")) {
                /**
                 * VFREEBUSY
                 */
                parseVFreeBusy();
            } else if (this.line.startsWith("BEGIN:VEVENT")) {
                /**
                 * VEVENT
                 */
                parseVEvent();
            } else if (this.line.indexOf("BEGIN:VTODO") != -1) {
                /**
                 * VTODO
                 */
                parseVTodo();
            } else if (this.line.indexOf("BEGIN:VJOURNAL") != -1) {
                /**
                 * VJournal
                 */
                parseVJournal();
            }
        }
    }

    private RRule parseRRuleFromLine(final String line) throws VCalendarException {
        final RRule rrule = new RRule();
        final StringTokenizer st = new StringTokenizer(line, ";");
        while (st.hasMoreTokens()) {
            String part = st.nextToken();
            if (part.startsWith("FREQ=")) {
                part = part.substring(part.indexOf("=") + 1);
                rrule.setFrequency(part);
            } else if (part.startsWith("INTERVAL=")) {
                part = part.substring(part.indexOf("=") + 1);
                try {
                    rrule.setInterval(Integer.parseInt(part));
                } catch (final NumberFormatException e) {
                }
            } else if (part.startsWith("COUNT=")) {
                part = part.substring(part.indexOf("=") + 1);
                try {
                    rrule.setCount(Integer.parseInt(part));
                } catch (final NumberFormatException e) {
                }
            } else if (part.startsWith("UNTIL=")) {
                part = part.substring(part.indexOf("=") + 1);
                if (this.vtimezone != null) {
                    rrule.setUntil(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), part));
                } else {
                    rrule.setUntil(DateTime.getCalendarFromString(null, part));
                }
            } else if (part.startsWith("WKST=")) {
                part = part.substring(part.indexOf("=") + 1);
                rrule.setWeekStart(part);
            } else if (part.startsWith("BYMINUTE=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (final NumberFormatException e) {
                    }
                }
                rrule.setByMinute(_values);
            } else if (part.startsWith("BYHOUR=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (final NumberFormatException e) {
                    }
                }
                rrule.setByHour(_values);
            } else if (part.startsWith("BYDAY=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<String> _values = new ArrayList<String>();
                while (_stt.hasMoreTokens()) {
                    _values.add(_stt.nextToken().toUpperCase());
                }
                rrule.setByDay(_values);
            } else if (part.startsWith("BYMONTH=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (final NumberFormatException e) {
                    }
                }
                rrule.setByMonth(_values);
            } else if (part.startsWith("BYMONTHDAY=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (final NumberFormatException e) {
                    }
                }
                rrule.setByMonthDay(_values);
            } else if (part.startsWith("BYYEARDAY=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (final NumberFormatException e) {
                    }
                }
                rrule.setByYearDay(_values);
            } else if (part.startsWith("BYWEEKNO=")) {
                part = part.substring(part.indexOf("=") + 1);
                final StringTokenizer _stt = new StringTokenizer(part, ",");
                final List<Integer> _values = new ArrayList<Integer>();
                while (_stt.hasMoreTokens()) {
                    try {
                        _values.add(Integer.parseInt(_stt.nextToken()));
                    } catch (final NumberFormatException e) {
                    }
                }
                rrule.setByWeekNo(_values);
            }
        }
        return rrule;
    }

    private void parseVAlarm(final VEvent ve) throws VCalendarException {
        final VAlarm va = new VAlarm();
        try {
            for (nextLine(); this.line != null; nextLine()) {
                if (this.line.startsWith("END:VALARM")) {
                    ve.addAlarm(va);
                    break;
                } else {
                    if (this.line.startsWith("TRIGGER")) {
                        va.setTrigger(new Trigger(this.line));
                    } else if (this.line.startsWith("REPEAT")) {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        try {
                            va.setRepeat(Integer.parseInt(this.line));
                        } catch (final NumberFormatException e) {
                        }
                    } else if (this.line.startsWith("DURATION")) {
                        this.line = this.line.substring(this.line.lastIndexOf(":") + 1);
                        va.setDuration(new Duration(this.line));
                    } else if (this.line.startsWith("DESCRIPTION")) {
                        this.line = this.line.substring(this.line.lastIndexOf(":") + 1);
                        va.setDescription(this.line);
                    } else if (this.line.startsWith("ACTION")) {
                        this.line = this.line.substring(this.line.lastIndexOf(":") + 1);
                        va.setAction(this.line);
                    } else if (this.line.startsWith("X-")) {
                        va.addExtended(this.line);
                    }
                }
            }
        } catch (final Exception e) {
            throw new VCalendarException("VCALENDAR::VEVENT::VALARM::error::" + this.line);
        }
    }

    private void parseVEvent() throws IOException, VCalendarException {
        final VEvent ve = new VEvent();
        for (nextLine(); this.line != null; nextLine()) {
            if (this.line.startsWith("END:VEVENT")) {
                if (this.vevent.containsKey(ve.getUid())) {
                    if (!this.vevent.get(ve.getUid()).hasRecurrence()) {
                        this.vevent.put(ve.getUid(), ve);
                    }
                } else {
                    this.vevent.put(ve.getUid(), ve);
                }
                break;
            } else if (this.line.startsWith("BEGIN:VALARM")) {
                /**
                 * VALARM
                 */
                parseVAlarm(ve);
            } else {
                if (this.line.startsWith("CATEGORIES")) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        final StringTokenizer st = new StringTokenizer(this.line, ",");
                        if (st.countTokens() > 0) {
                            while (st.hasMoreTokens()) {
                                ve.addCategory(st.nextToken());
                            }
                        }
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::CATEGORIES::error::" + this.line);
                    }
                } else if (this.line.startsWith("SUMMARY") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setSummary(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::LOCATION::error::" + this.line);
                    }
                } else if (this.line.startsWith("LOCATION") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setLocation(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::LOCATION::error::" + this.line);
                    }
                } else if (this.line.startsWith("CREATED") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setCreated(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::CREATED::error::" + this.line);
                    }
                } else if (this.line.startsWith("LAST-MODIFIED") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setLastModified(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::LAST-MODIFIED::error::" + this.line);
                    }
                } else if (this.line.startsWith("DESCRIPTION") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setDescription(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DESCRIPTION::error::" + this.line);
                    }
                } else if (this.line.startsWith("DTSTAMP") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setDTStamp(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DTSTAMP::error::" + this.line);
                    }
                } else if (this.line.startsWith("UID") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setUid(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::UID::error::" + this.line);
                    }
                } else if (this.line.startsWith("DTSTART") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setDTStart(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DTSTART::error::" + this.line);
                    }
                } else if (this.line.startsWith("DTEND") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setDTEnd(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::DTEND::error::" + this.line);
                    }
                } else if (this.line.startsWith("EXDATE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.addExDate(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::EXDATE::error::" + this.line);
                    }
                } else if (this.line.startsWith("STATUS") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setStatus(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::STATUS::error::" + this.line);
                    }
                } else if (this.line.startsWith("CLASS") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setClassType(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::CLASS::error::" + this.line);
                    }
                } else if (this.line.startsWith("ATTENDEE") && (this.line.indexOf(":") > 0)) {
                    try {
                        final Person p = new Person(this.line, Person.ATTENDEE);
                        ve.setAttendee(p.getMailTo(), p);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::ATTENDEE::error::" + this.line);
                    }
                } else if (this.line.startsWith("ORGANIZER") && (this.line.indexOf(":") > 0)) {
                    try {
                        final Person p = new Person(this.line, Person.ORGANIZER);
                        ve.setOrganizer(p.getMailTo(), p);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::ORGANIZER::error::" + this.line);
                    }
                } else if (this.line.startsWith("RRULE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        ve.setRRule(parseRRuleFromLine(this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VEVENT::RRULE::error::" + this.line);
                    }
                } else if (this.line.startsWith("X-")) {
                    ve.addExtended(this.line);
                }
            }
        }
    }

    private void parseVFreeBusy() throws IOException, VCalendarException {
        final VFreeBusy vfb = new VFreeBusy(this.vtimezone);
        for (nextLine(); this.line != null; nextLine()) {
            if (this.line.startsWith("END:VFREEBUSY")) {
                this.vfreebusy = vfb;
                break;
            } else if (this.line.startsWith("DTSTART")) {
                try {
                    this.line = this.line.substring(this.line.indexOf(":") + 1);
                    vfb.setDTStart(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                } catch (final Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::DTSTART::error::" + this.line);
                }
            } else if (this.line.startsWith("DTEND")) {
                try {
                    this.line = this.line.substring(this.line.indexOf(":") + 1);
                    vfb.setDTEnd(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                } catch (final Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::DTEND::error::" + this.line);
                }
            } else if (this.line.startsWith("ATTENDEE") && (this.line.indexOf(":") > 0)) {
                try {
                    final Person p = new Person(this.line, Person.ATTENDEE);
                    vfb.setAttendee(p.getMailTo(), p);
                } catch (final Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::ATTENDEE::error::" + this.line);
                }
            } else if (this.line.startsWith("ORGANIZER") && (this.line.indexOf(":") > 0)) {
                try {
                    final Person p = new Person(this.line, Person.ORGANIZER);
                    vfb.setOrganizer(p.getMailTo(), p);
                } catch (final Exception e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::ORGANIZER::error::" + this.line);
                }
            } else if (this.line.startsWith("FREEBUSY")) {
                this.line = this.line.substring(this.line.indexOf(":") + 1);
                try {
                    final StringTokenizer st = new StringTokenizer(this.line, ",");
                    while (st.hasMoreTokens()) {
                        final String t = st.nextToken();
                        if (t.contains("/")) {
                            try {
                                final Calendar start = DateTime.getCalendarFromString(this.vtimezone.getTimeZone(),
                                        t.substring(0, t.indexOf("/")));
                                final Calendar end = DateTime.getCalendarFromString(this.vtimezone.getTimeZone(),
                                        t.substring(t.indexOf("/") + 1));
                                vfb.addBusy(new Period(start, end));
                            } catch (final Exception e) {
                                final Calendar start = DateTime.getCalendarFromString(this.vtimezone.getTimeZone(),
                                        t.substring(0, t.indexOf("/")));
                                final Duration d = new Duration(t.substring(t.indexOf("/") + 1));
                                final Calendar end = Calendar.getInstance();
                                end.setTimeInMillis(start.getTimeInMillis() + d.getMilliseconds());
                                vfb.addBusy(new Period(start, end));
                            }
                        }
                    }
                } catch (final NullPointerException e) {
                    throw new VCalendarException("VCALENDAR::VFREEBUSY::FREEBUSY::error::" + this.line);
                }
            }
        }
    }

    private void parseVJournal() throws IOException, VCalendarException {
        final VJournal vj = new VJournal();
        for (nextLine(); this.line != null; nextLine()) {
            if (this.line.isEmpty()) {
                nextLine();
            }
            if (this.line.indexOf("END:VJOURNAL") != -1) {
                this.vjournal.put(vj.getUid(), vj);
                break;
            } else {
                if (this.line.startsWith("CATEGORIES")) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        final StringTokenizer st = new StringTokenizer(this.line, ",");
                        if (st.countTokens() > 0) {
                            while (st.hasMoreTokens()) {
                                vj.addCategory(st.nextToken());
                            }
                        }
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::CATEGORIES::error::" + this.line);
                    }
                } else if (this.line.startsWith("SUMMARY") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setSummary(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::SUMMARY::error::" + this.line);
                    }
                } else if (this.line.startsWith("DESCRIPTION") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setDescription(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::DESCRIPTION::error::" + this.line);
                    }
                } else if (this.line.startsWith("CREATED") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setCreated(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::CREATED::error::" + this.line);
                    }
                } else if (this.line.startsWith("UID") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setUid(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::UID::error::" + this.line);
                    }
                } else if (this.line.startsWith("DTSTART") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setDTStart(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::DTSTART::error::" + this.line);
                    }
                } else if (this.line.startsWith("STATUS") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setStatus(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::STATUS::error::" + this.line);
                    }
                } else if (this.line.startsWith("CLASS") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setClassType(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::CLASS::error::" + this.line);
                    }
                } else if (this.line.startsWith("ATTENDEE") && (this.line.indexOf(":") > 0)) {
                    try {
                        final Person p = new Person(this.line, Person.ATTENDEE);
                        vj.setAttendee(p.getMailTo(), p);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::ATTENDEE::error::" + this.line);
                    }
                } else if (this.line.startsWith("ORGANIZER") && (this.line.indexOf(":") > 0)) {
                    try {
                        final Person p = new Person(this.line, Person.ORGANIZER);
                        vj.setOrganizer(p.getMailTo(), p);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::ORGANIZER::error::" + this.line);
                    }
                } else if (this.line.startsWith("RRULE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vj.setRRule(parseRRuleFromLine(this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VJOURNAL::RRULE::error::" + this.line);
                    }
                } else if (this.line.startsWith("X-")) {
                    vj.addExtended(this.line);
                }
            }
        }
    }

    private void parseVTimeZone() throws IOException, VCalendarException {
        final VTimeZone vtz = new VTimeZone(null);
        for (nextLine(); this.line != null; nextLine()) {
            if (this.line.startsWith("END:VTIMEZONE")) {
                this.vtimezone = vtz;
                break;
            } else if (this.line.startsWith("TZID")) {
                this.line = this.line.substring(this.line.indexOf(":") + 1);
                vtz.setTZID(this.line);
            } else if (this.line.startsWith("BEGIN:STANDARD")) {
                for (nextLine(); this.line != null; nextLine()) {
                    if (this.line.startsWith("END:STANDARD")) {
                        break;
                    } else if (this.line.startsWith("RRULE")) {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vtz.setStandardRRule(parseRRuleFromLine(this.line));
                    }
                }
            } else if (this.line.startsWith("BEGIN:DAYLIGHT")) {
                for (nextLine(); this.line != null; nextLine()) {
                    if (this.line.startsWith("END:DAYLIGHT")) {
                        break;
                    } else if (this.line.startsWith("RRULE")) {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vtz.setDayLightRRule(parseRRuleFromLine(this.line));
                    }
                }
            }
        }
    }

    private void parseVTodo() throws IOException, VCalendarException {
        final VTodo vt = new VTodo();
        for (nextLine(); this.line != null; nextLine()) {
            if (this.line.isEmpty()) {
                nextLine();
            }
            if (this.line.indexOf("END:VTODO") != -1) {
                this.vtodo.put(vt.getUid(), vt);
                break;
            } else if (this.line.startsWith("BEGIN:VALARM")) {
                final VAlarm va = new VAlarm();
                try {
                    for (nextLine(); this.line != null; nextLine()) {
                        if (this.line.startsWith("END:VALARM")) {
                            vt.addAlarm(va);
                            break;
                        } else {
                            if (this.line.startsWith("TRIGGER")) {
                                va.setTrigger(new Trigger(this.line));
                            } else if (this.line.startsWith("REPEAT")) {
                                this.line = this.line.substring(this.line.indexOf(":") + 1);
                                try {
                                    va.setRepeat(Integer.parseInt(this.line));
                                } catch (final NumberFormatException e) {
                                }
                            } else if (this.line.startsWith("DURATION")) {
                                this.line = this.line.substring(this.line.lastIndexOf(":") + 1);
                                va.setDuration(new Duration(this.line));
                            } else if (this.line.startsWith("DESCRIPTION")) {
                                this.line = this.line.substring(this.line.lastIndexOf(":") + 1);
                                va.setDescription(this.line);
                            } else if (this.line.startsWith("ACTION")) {
                                this.line = this.line.substring(this.line.lastIndexOf(":") + 1);
                                va.setAction(this.line);
                            } else if (this.line.startsWith("X-")) {
                                va.addExtended(this.line);
                            }
                        }
                    }
                } catch (final Exception e) {
                    throw new VCalendarException("VCALENDAR::VTODO::VALARM::error::" + this.line);
                }
            } else {
                if (this.line.startsWith("CATEGORIES")) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        final StringTokenizer _st = new StringTokenizer(this.line, ",");
                        if (_st.countTokens() > 0) {
                            while (_st.hasMoreTokens()) {
                                vt.addCategory(_st.nextToken());
                            }
                        }
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::CATEGORIES::error::" + this.line);
                    }
                } else if (this.line.startsWith("SUMMARY") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setSummary(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::SUMMARY::error::" + this.line);
                    }
                } else if (this.line.startsWith("LOCATION") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setLocation(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::LOCATION::error::" + this.line);
                    }
                } else if (this.line.startsWith("CREATED") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setCreated(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::CREATED::error::" + this.line);
                    }
                } else if (this.line.startsWith("LAST-MODIFIED") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setLastModified(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::LAST-MODIFIED::error::" + this.line);
                    }
                } else if (this.line.startsWith("DESCRIPTION") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setDescription(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::DESCRIPTION::error::" + this.line);
                    }
                } else if (this.line.startsWith("DTSTAMP") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setDTStamp(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::LAST-MODIFIED::error::" + this.line);
                    }
                } else if (this.line.startsWith("DUE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setDue(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::DUE::error::" + this.line);
                    }
                } else if (this.line.startsWith("UID") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setUid(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::UID::error::" + this.line);
                    }
                } else if (this.line.startsWith("DTSTART") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setDTStart(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::DTSTART::error::" + this.line);
                    }
                } else if (this.line.startsWith("EXDATE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.addExDate(DateTime.getCalendarFromString(this.vtimezone.getTimeZone(), this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::EXDATE::error::" + this.line);
                    }
                } else if (this.line.startsWith("STATUS") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setStatus(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::STATUS::error::" + this.line);
                    }
                } else if (this.line.startsWith("PERCENT-COMPLETE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        try {
                            vt.setPercent(Integer.parseInt(this.line));
                        } catch (final NumberFormatException e) {
                        }
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::PERCENT-COMPLETE::error::" + this.line);
                    }
                } else if (this.line.startsWith("CLASS") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setClassType(this.line);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::CLASS::error::" + this.line);
                    }
                } else if (this.line.startsWith("ATTENDEE") && (this.line.indexOf(":") > 0)) {
                    try {
                        final Person p = new Person(this.line, Person.ATTENDEE);
                        vt.setAttendee(p.getMailTo(), p);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::ATTENDEE::error::" + this.line);
                    }
                } else if (this.line.startsWith("ORGANIZER") && (this.line.indexOf(":") > 0)) {
                    try {
                        final Person p = new Person(this.line, Person.ORGANIZER);
                        vt.setOrganizer(p.getMailTo(), p);
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::ORGANIZER::error::" + this.line);
                    }
                } else if (this.line.startsWith("RRULE") && (this.line.indexOf(":") > 0)) {
                    try {
                        this.line = this.line.substring(this.line.indexOf(":") + 1);
                        vt.setRRule(parseRRuleFromLine(this.line));
                    } catch (final Exception e) {
                        throw new VCalendarException("VCALENDAR::VTODO::RRULE::error::" + this.line);
                    }
                } else if (this.line.startsWith("X-")) {
                    vt.addExtended(this.line);
                }
            }
        }
    }

    /**
     * Remove an VEvent object
     * 
     * @param uid
     * @return
     */
    public boolean removeVevent(final String uid) {
        if (this.vevent.remove(uid) != null) {
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
        if (this.vjournal.remove(uid) != null) {
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
        if (this.vtodo.remove(uid) != null) {
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
        this.ical_file = icalendar;
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
        this.vtimezone = timezone;
    }

    @Override
    public String toString() {
        final String CRLF = "\r\n";
        final StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR");
        sb.append(CRLF);
        sb.append("VERSION:" + version);
        sb.append(CRLF);
        sb.append("PRODID:" + prodid);
        sb.append(CRLF);
        if (this.method != null) {
            sb.append("METHOD:" + this.method);
            sb.append(CRLF);
        }

        if (this.vtimezone != null) {
            sb.append(this.vtimezone.toString());
        }

        for (final VEvent ve : getVevents()) {
            sb.append(ve.toString(this.vtimezone));
        }

        for (final VTodo vt : getVtodos()) {
            sb.append(vt.toString(this.vtimezone));
        }

        for (final VJournal vj : getVjournals()) {
            sb.append(vj.toString(this.vtimezone));
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
        if (!this.vevent.containsKey(ve.getUid())) {
            throw new VCalendarException("VEvent not found");
        }
        this.vevent.put(ve.getUid(), ve);
    }

    /**
     * Update an VJournal object
     * 
     * @param vj
     * @throws VCalendarException
     */
    public void updateVjournal(final VJournal vj) throws VCalendarException {
        if (!this.vjournal.containsKey(vj.getUid())) {
            throw new VCalendarException("VJournal not found");
        }
        this.vjournal.put(vj.getUid(), vj);
    }

    /**
     * Update an VTodo object
     * 
     * @param vt
     * @throws VCalendarException
     */
    public void updateVtodo(final VTodo vt) throws VCalendarException {
        if (!this.vtodo.containsKey(vt.getUid())) {
            throw new VCalendarException("VTodo not found");
        }
        this.vtodo.put(vt.getUid(), vt);
    }

    /**
     * Writes the icalendar file
     * 
     * @throws VCalendarException
     */
    public void write() throws VCalendarException {
        if ((this.ical_file != null) && this.ical_file.canWrite()) {
            try {
                FileUtils.writeFile(this.ical_file, toString());
            } catch (final FileLockException e) {
                throw new VCalendarException(e);
            } catch (final IOException e) {
                throw new VCalendarException(e);
            }
        }
    }
}
