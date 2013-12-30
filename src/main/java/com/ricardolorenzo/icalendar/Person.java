/*
 * Person class
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Ricardo_Lorenzo
 *
 */
public class Person implements Serializable {
    public static final long serialVersionUID = 89472947947291032L;

    public static final int ORGANIZER = 1;
    public static final int ATTENDEE = 2;
    private String mailto;
    private String cn;
    private String dir;
    private String role;
    private String partstat;
    private int type;

    public Person(final String value, final int type) throws VCalendarException {
        if (type != ORGANIZER && type != ATTENDEE) {
            throw new VCalendarException("invalid type");
        }
        this.type = type;
        StringTokenizer _st = new StringTokenizer(value, ";");
        while (_st.hasMoreTokens()) {
            String part = _st.nextToken();
            if (part.toUpperCase().startsWith("ROLE=")) {
                String subpart = part.substring(5);
                if (subpart.contains(":")) {
                    subpart = subpart.substring(0, subpart.indexOf(":"));
                }
                role = subpart;
            } else if (part.toUpperCase().startsWith("PARTSTAT=")) {
                String subpart = part.substring(9);
                if (subpart.contains(":")) {
                    subpart = subpart.substring(0, subpart.indexOf(":"));
                }
                partstat = subpart;
            } else if (part.toUpperCase().startsWith("CN=")) {
                String subpart = part.substring(3);
                if (subpart.contains(":")) {
                    subpart = subpart.substring(0, subpart.indexOf(":"));
                }
                cn = subpart;
            } else if (part.toUpperCase().startsWith("DIR=")) {
                String subpart = part.substring(3);
                if (subpart.contains(":")) {
                    subpart = subpart.substring(0, subpart.indexOf(":"));
                }
                dir = subpart;
            }
            if (part.toUpperCase().contains("MAILTO:")) {
                mailto = part.substring(part.toUpperCase().lastIndexOf("MAILTO:") + 7);
            }
        }
    }

    public Person() {
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return cn;
    }

    public String getMailTo() {
        return mailto;
    }

    public String getPartStat() {
        return partstat;
    }

    public String getDir() {
        return dir;
    }

    public String getRole() {
        return role;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public void setName(final String name) {
        cn = name;
    }

    public void setMailTo(final String mail) {
        mailto = mail;
    }

    public void setDir(final String uri) throws URISyntaxException {
        dir = new URI(uri).toString();
    }

    public void setPartStat(final String name) throws Exception {
        List<String> values = new ArrayList<String>(Arrays.asList(new String[] { "NEEDS-ACTION", "TENTATIVE",
                "ACCEPTED", "DECLINED", "DELEGATED", "IN-PROCESS", "COMPLETED" }));
        if (!values.contains(name.toUpperCase())) {
            throw new Exception("invalid partstat");
        }
        partstat = name.toUpperCase();
    }

    public void setRole(final String name) throws VCalendarException {
        List<String> values = new ArrayList<String>(Arrays.asList(new String[] { "CHAIR", "REQ-PARTICIPANT",
                "OPT-PARTICIPANT", "NON-PARTICIPANT" }));
        if (!values.contains(name.toUpperCase())) {
            throw new VCalendarException("invalid role");
        }
        role = name.toUpperCase();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case ORGANIZER:
                sb.append("ORGANIZER");
                break;
            case ATTENDEE:
                sb.append("ATTENDEE");
                break;
            default:
                break;
        }
        if (role != null) {
            sb.append(";ROLE=");
            sb.append(role);
        }
        if (partstat != null) {
            sb.append(";PARTSTAT=");
            sb.append(partstat);
        }
        if (cn != null) {
            sb.append(";CN=");
            sb.append(cn);
        }
        sb.append(":MAILTO:");
        if (mailto != null) {
            sb.append(mailto);
        }
        return sb.toString();
    }
}
