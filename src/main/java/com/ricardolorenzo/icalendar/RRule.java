/*
 * RRule class
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
public class RRule implements Serializable {
    public static final long serialVersionUID = 89472947947291231L;

    private Calendar until;
    private String freq;
    private int count;
    private int interval;
    private String wkst;
    private List<Integer> byminute;
    private List<Integer> byhour;
    private List<String> byday;
    private List<Integer> bymonth;
    private List<Integer> bymonthday;
    private List<Integer> byyearday;
    private List<Integer> byweekno;

    public RRule() {
        count = 0;
        interval = 0;
    }

    public String getFrequency() {
        return freq;
    }

    public void setFrequency(String freq) throws VCalendarException {
        freq = freq.toUpperCase();
        if (!Arrays.asList(new String[] { "MINUTELY", "HOURLY", "DAILY", "WEEKLY", "MONTHLY", "YEARLY" })
                .contains(freq)) {
            throw new VCalendarException("invlaid frequency");
        }
        this.freq = freq;
    }

    public Calendar getUntil() {
        return until;
    }

    public boolean hasUntil() {
        if (until != null) {
            return true;
        }
        return false;
    }

    public void setUntil(final Calendar until) {
        until.set(Calendar.MILLISECOND, 0);
        this.until = until;
    }

    public int getCount() {
        return count;
    }

    public boolean hasCount() {
        if (count != 0) {
            return true;
        }
        return false;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public int getInterval() {
        return interval;
    }

    public boolean hasInterval() {
        if (interval != 0) {
            return true;
        }
        return false;
    }

    public void setInterval(final int interval) {
        this.interval = interval;
    }

    public String getWeekStart() {
        return wkst;
    }

    public boolean hasWeekStart() {
        if (wkst != null) {
            return true;
        }
        return false;
    }

    public void setWeekStart(final String wkst) throws VCalendarException {
        if (!Arrays.asList(new String[] { "SU", "MO", "TU", "WE", "TH", "FR", "SA" }).contains(wkst)) {
            throw new VCalendarException("invalid weekend start");
        }
        this.wkst = wkst;
    }

    public List<Integer> getByMinute() {
        return byminute;
    }

    public boolean hasByMinute() {
        if (byminute != null) {
            return true;
        }
        return false;
    }

    public void setByMinute(final List<Integer> byminute) {
        this.byminute = byminute;
    }

    public List<Integer> getByHour() {
        return byhour;
    }

    public boolean hasByHour() {
        if (byhour != null) {
            return true;
        }
        return false;
    }

    public void setByHour(final List<Integer> byhour) {
        this.byhour = byhour;
    }

    public List<String> getByDay() {
        return byday;
    }

    public boolean hasByDay() {
        if (byday != null) {
            return true;
        }
        return false;
    }

    public void setByDay(final List<String> byday) throws VCalendarException {
        List<String> weekDays = new ArrayList<String>(Arrays.asList(new String[] { "SU", "MO", "TU", "WE", "TH", "FR",
                "SA" }));

        for (int i = byday.size(); --i >= 0;) {
            String day = byday.get(i);
            if (day.length() == 2) {
                if (!weekDays.contains(day)) {
                    throw new VCalendarException("invalid day list");
                }
            } else if (day.length() < 2) {
                throw new VCalendarException("invalid day list");
            } else if (day.length() > 2) {
                String nday = day.substring(day.length() - 2);
                if (!weekDays.contains(nday)) {
                    throw new VCalendarException("invalid day list");
                }
                nday = day.substring(0, day.lastIndexOf(nday));
                try {
                    Integer.parseInt(nday);
                } catch (NumberFormatException _ex) {
                    throw new VCalendarException("invalid day list");
                }
            }
        }
        this.byday = byday;
    }

    public List<Integer> getByMonth() {
        return bymonth;
    }

    public boolean hasByMonth() {
        if (bymonth != null) {
            return true;
        }
        return false;
    }

    public void setByMonth(final List<Integer> bymonth) {
        this.bymonth = bymonth;
    }

    public List<Integer> getByMonthDay() {
        return bymonthday;
    }

    public boolean hasByMonthDay() {
        if (bymonthday != null) {
            return true;
        }
        return false;
    }

    public void setByMonthDay(final List<Integer> bymonthday) {
        this.bymonthday = bymonthday;
    }

    public List<Integer> getByYearDay() {
        return byyearday;
    }

    public boolean hasByYearDay() {
        if (byyearday != null) {
            return true;
        }
        return false;
    }

    public void setByYearDay(final List<Integer> byyearday) {
        this.byyearday = byyearday;
    }

    public List<Integer> getByWeekNo() {
        return byweekno;
    }

    public boolean hasByWeekNo() {
        if (byweekno != null) {
            return true;
        }
        return false;
    }

    public void setByWeekNo(final List<Integer> byweekno) {
        this.byweekno = byweekno;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FREQ=");
        sb.append(freq);
        if (hasUntil()) {
            sb.append(";");
            sb.append("UNTIL=");
            sb.append(DateTime.getUTCTime(until));
        }
        if (hasCount()) {
            sb.append(";");
            sb.append("COUNT=");
            sb.append(count);
        }
        if (hasInterval()) {
            sb.append(";");
            sb.append("INTERVAL=");
            sb.append(interval);
        }
        if (hasByMinute()) {
            sb.append(";");
            sb.append("BYMINUTE=");
            for (int i = 0; i < byminute.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(byminute.get(i).toString());
            }
        }
        if (hasByHour()) {
            sb.append(";");
            sb.append("BYHOUR=");
            for (int i = 0; i < byhour.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(byhour.get(i).toString());
            }
        }
        if (hasByDay()) {
            sb.append(";");
            sb.append("BYDAY=");
            for (int i = 0; i < byday.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(byday.get(i));
            }
        }
        if (hasByMonth()) {
            sb.append(";");
            sb.append("BYMONTH=");
            for (int i = 0; i < bymonth.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(bymonth.get(i).toString());
            }
        }
        if (hasByMonthDay()) {
            sb.append(";");
            sb.append("BYMONTHDAY=");
            for (int i = 0; i < bymonthday.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(bymonthday.get(i).toString());
            }
        }
        if (hasByWeekNo()) {
            sb.append(";");
            sb.append("BYWEEKNO=");
            for (int i = 0; i < byweekno.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(byweekno.get(i).toString());
            }
        }
        if (hasByYearDay()) {
            sb.append(";");
            sb.append("BYYEARDAY=");
            for (int i = 0; i < byyearday.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(byyearday.get(i).toString());
            }
        }
        return sb.toString();
    }
}
