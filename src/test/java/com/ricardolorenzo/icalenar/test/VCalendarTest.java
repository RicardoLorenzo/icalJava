/*
 * VCalendarTest class
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
package com.ricardolorenzo.icalenar.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import com.ricardolorenzo.icalendar.VCalendar;
import com.ricardolorenzo.icalendar.VCalendarException;
import com.ricardolorenzo.icalendar.VEvent;

/**
 * @author Ricardo Lorenzo
 * 
 */
public class VCalendarTest {

    @Test
    public void testFile() {
        assertNotNull("iCal file is missing", getClass().getResource("/calendar.ical"));
    }

    @Test
    public void testCalendar() {
        VCalendar vcal;
        try {
            vcal = new VCalendar(new File(getClass().getResource("/calendar.ical").toURI()));
            List<VEvent> events = vcal.getVevents();
            assertFalse(events.isEmpty());
            VEvent e = vcal.getVevent("1285935469767a7c7c1a9b3f0df8003a@yoursever.com");
            assertNotNull(e);
            assertEquals(e.getDTStart().get(Calendar.MONTH), 6);
        } catch (VCalendarException e) {
            assertTrue(false);
        } catch (URISyntaxException e) {
            assertTrue(false);
        }
    }
}
