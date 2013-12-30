/*
 * Duration class
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

/**
 * @author Ricardo_Lorenzo
 *
 */
public class Duration implements Serializable {
    public static final long serialVersionUID = 89472947947292854L;

    private int weeks;
    private int days;
    private int hours;
    private int minutes;
    private int seconds;
    private boolean negative;

    /**
     * Duration constructor from a string representation.
     * @param value
     */
    public Duration(final String value) {
        weeks = 0;
        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        negative = false;

        String tok = "";
        for (char c : value.toCharArray()) {
            switch (c) {
                case '+':
                    tok = "";
                    break;
                case '-':
                    tok = "";
                    negative = true;
                    break;
                case 'P':
                    tok = "";
                    break;
                case 'T':
                    tok = "";
                    break;
                case 'W':
                    try {
                        weeks = Integer.parseInt(tok);
                    } catch (NumberFormatException _ex) {
                    }
                    tok = "";
                    break;
                case 'D':
                    try {
                        days = Integer.parseInt(tok);
                    } catch (NumberFormatException _ex) {
                    }
                    tok = "";
                    break;
                case 'H':
                    try {
                        hours = Integer.parseInt(tok);
                    } catch (NumberFormatException _ex) {
                    }
                    tok = "";
                    break;
                case 'M':
                    try {
                        minutes = Integer.parseInt(tok);
                    } catch (NumberFormatException _ex) {
                    }
                    tok = "";
                    break;
                case 'S':
                    try {
                        minutes = Integer.parseInt(tok);
                    } catch (NumberFormatException _ex) {
                    }
                    tok = "";
                    break;
                default:
                    tok += c;
                    break;
            }
        }
    }

    /**
     * Duration constructor.
     * @param value
     */
    public Duration() {
        weeks = 0;
        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        negative = false;
    }

    private void compute() {
        if (seconds > 0) {
            if (seconds > 60) {
                if (minutes > 0) {
                    minutes = minutes + seconds / 60;
                    seconds = seconds % 60;
                } else {
                    minutes = seconds / 60;
                    seconds = seconds % 60;
                }
            }
        }
        if (minutes > 0) {
            if (minutes > 60) {
                if (hours > 0) {
                    hours = hours + minutes / 60;
                    minutes = minutes % 60;
                } else {
                    hours = minutes / 60;
                    minutes = minutes % 60;
                }
            }
        }
        if (hours > 0) {
            if (hours > 24) {
                if (days > 0) {
                    days = days + hours / 24;
                    hours = hours % 24;
                } else {
                    days = hours / 24;
                    hours = hours % 24;
                }
            }
        }
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        compute();
        if (negative) {
            sb.append("-");
        }
        sb.append("P");
        if (weeks > 0) {
            sb.append(weeks);
            sb.append("W");
        } else {
            if (days > 0) {
                sb.append(days);
                sb.append("D");
            }
            if (hours > 0 || minutes > 0 || seconds > 0) {
                sb.append("T");
                if (hours > 0) {
                    sb.append(hours);
                    sb.append("H");
                }
                if (minutes > 0) {
                    sb.append(minutes);
                    sb.append("M");
                }
                if (seconds > 0) {
                    sb.append(seconds);
                    sb.append("S");
                }
            }
            if ((hours + minutes + seconds + days + weeks) == 0) {
                sb.append("T0S");
            }
        }
        return sb.toString();
    }

    /**
     * Returns the duration of the Trigger in milliseconds.
     * @param arg0
     * @return
     */
    public long getMilliseconds() {
        long millis = 0;
        if (weeks > 0) {
            millis += weeks * 7 * 24 * 60 * 60 * 1000L;
        } else {
            if (days > 0) {
                millis += days * 24 * 60 * 60 * 1000L;
            }
            if (hours > 0) {
                millis += hours * 60 * 60 * 1000L;
            }
            if (minutes > 0) {
                millis += minutes * 60 * 1000L;
            }
            if (seconds > 0) {
                millis += seconds * 1000L;
            }
        }
        if (negative) {
            millis *= -1;
        }
        return millis;
    }

    /**
     * Returns the number of days.
     * @return int
     */
    public int getDays() {
        if (days == 0) {
            return getHours() / 24;
        }
        return days;
    }

    /**
     * Returns the number of hours.
     * @return int
     */
    public int getHours() {
        if (hours == 0) {
            return getMinutes() / 60;
        }
        return hours;
    }

    /**
     * Returns the number of minutes.
     * @return int
     */
    public int getMinutes() {
        if (minutes == 0) {
            return getSeconds() / 60;
        }
        return minutes;
    }

    /**
     * Returns the number of seconds.
     * @return int
     */
    public int getSeconds() {
        if (seconds == 0) {
            return new Long(getMilliseconds() / 1000L).intValue();
        }
        return seconds;
    }

    /**
     * Returns the number of weeks.
     * @return int
     */
    public int getWeeks() {
        if (days == 0) {
            return getHours() / 7;
        }
        return weeks;
    }

    public boolean isBefore() {
        return negative;
    }

    /**
     * Set the number of days.
     * @return int
     */
    public void setDays(final int days) {
        this.days = days;
    }

    /**
     * Set the number of hours.
     * @return int
     */
    public void setHours(final int hours) {
        this.hours = hours;
    }

    /**
     * Set the number of minutes.
     * @return int
     */
    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }

    /**
     * Set the number of seconds.
     * @return int
     */
    public void setSeconds(final int seconds) {
        this.seconds = seconds;
    }

    /**
     * Set the number of weeks.
     * @return int
     */
    public void setWeeks(final int weeks) {
        this.weeks = weeks;
    }

    public void setBefore(final boolean value) {
        negative = value;
    }
}
