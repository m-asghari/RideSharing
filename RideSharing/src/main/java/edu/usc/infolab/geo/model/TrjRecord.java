/**
 *
 */
package edu.usc.infolab.geo.model;

import com.vividsolutions.jts.geom.Coordinate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents an Immutable point in a trajectory.
 *
 * @author Yaguang
 */
public class TrjRecord implements Comparable<TrjRecord> {
    /**
     * Time in milliseconds.
     */
    private long time;
    private Coordinate location;
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static void setTimezone(TimeZone timezone) {
        formatter.setTimeZone(timezone);
    }

    public TrjRecord(long time, Coordinate location) {
        this.time = time;
        this.location = location;
    }

    public long getTime() {
        return time;
    }

    public String getTimeString() {
        Date date = new Date(this.time);
        return formatter.format(date);
    }

    private void setTime(long time) {
        this.time = time;
    }

    private void setTime(Date time) {
        this.time = time.getTime();
    }

    public Coordinate getLocation() {
        return location;
    }

    private void setLocation(Coordinate location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getTimeString(), getLocation());
    }

    @Override
    public int compareTo(TrjRecord o) {
        int result = new Long(this.getTime()).compareTo(o.getTime());
        if (result == 0) {
            result = this.getLocation().compareTo(o.getLocation());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrjRecord))
            return false;
        TrjRecord trjObj = (TrjRecord) obj;
        return trjObj.getTime() == this.time && trjObj.getLocation().equals(this.getLocation());
    }
}
