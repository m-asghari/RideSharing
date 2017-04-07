package edu.usc.infolab.geo.model;

import com.vividsolutions.jts.geom.Coordinate;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A trajectory consists of an immutable list of ordered trajectory records.
 * <p>
 * Created by yaguang on 3/2/16.
 */
public class Trajectory {
    private List<TrjRecord> records = new ArrayList<>();
    private List<Long> times = new ArrayList<>();
    private String moid = "N/A";


    public Trajectory(Collection<TrjRecord> records) {
        this(records, "N/A");
    }

    public Trajectory(Collection<TrjRecord> records, String moid) {
        this.moid = moid;
        this.records = new ArrayList<>(records);
        Collections.sort(this.records);
        times.addAll(this.records.stream().map(TrjRecord::getTime).collect(Collectors.toList()));
    }

    public String getMoid() {
        return moid;
    }

    /**
     * Retrieves the trajectory record at a certain time, interpolate if necessary.
     *
     * @param time
     * @return
     */
    public TrjRecord at(long time) {
        if (time < times.get(0) || time > times.get(times.size() - 1))
            return null;

        int position = Collections.binarySearch(times, time);
        TrjRecord result = null;
        if (position >= 0) {
            result = records.get(position);
        } else {
            position = -(position + 1);
            // Interpolates.
            TrjRecord prev = records.get(Math.max(0, position - 1)),
                    next = records.get(Math.min(times.size(), position));
            result = interpolate(prev, next, time);
        }
        return result;
    }

    /**
     * Calculates the TrjRecord at time by interpolation.
     *
     * @param prev
     * @param next
     * @param time
     * @return
     */
    private static TrjRecord interpolate(TrjRecord prev, TrjRecord next, long time) {
        if (time < prev.getTime() || time > next.getTime())
            throw new IllegalArgumentException(
                    String.format("Time should between %d and %d, given %d", prev.getTime(), next.getTime(), time));
        double ratio = (double) (time - prev.getTime()) / (next.getTime() - prev.getTime());
        double x = prev.getLocation().x + ratio * (next.getLocation().x - prev.getLocation().x);
        double y = prev.getLocation().y + ratio * (next.getLocation().y - prev.getLocation().y);
        TrjRecord result = new TrjRecord(time, new Coordinate(x, y));
        return result;
    }

    /**
     * Retrieves the trajectory record at a certain time, interpolate if necessary.
     *
     * @param time
     * @return
     */
    public TrjRecord at(DateTime time) {
        return at(time.getMillis());
    }

    /**
     * Gets the duration of this trajectory.
     *
     * @return number of seconds.
     */
    public long getDuration() {
        long elapsedMills = records.get(records.size() - 1).getTime() - records.get(0).getTime();
        return elapsedMills / 1000;
    }

    /**
     * Calculates a temporal uniformly sampled trajectory by interpolation.
     *
     * @return uniformly sampled trajectory.
     */
    public Trajectory getUniformlySampledTrajectory(long interval) {
        List<TrjRecord> newRecords = new ArrayList<>();
        int size = times.size(), ind = 0;
        long endTime = times.get(size - 1);
        long currentTime = times.get(0);
        while (currentTime <= endTime) {
            TrjRecord prev = records.get(ind);
            TrjRecord next = records.get(Math.min(ind + 1, size - 1));

            // Interpolates
            TrjRecord interpolation = interpolate(prev, next, currentTime);
            newRecords.add(interpolation);

            // Increases currentTime.
            currentTime += interval;
            // Increases ind.
            while (ind < size - 1 && times.get(ind + 1) < currentTime)
                ++ind;
        }
        return new Trajectory(newRecords);
    }

    public List<Coordinate> toCoordinateList() {
        return records.stream().map(TrjRecord::getLocation).collect(Collectors.toList());
    }

    public int getRecordsNum() {
        return this.records.size();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Trajectory && this.records.equals(((Trajectory) obj).records);
    }

    @Override
    public String toString() {
        return this.records.toString();
    }

    public List<TrjRecord> getRecords() {
        return new ArrayList<>(this.records);
    }

    public Trajectory slice(DateTime start, DateTime end) {
        int startInd = Collections.binarySearch(times, start.getMillis());
        int endInd = Collections.binarySearch(times, end.getMillis());
        if (startInd < 0)
            startInd = -(startInd + 1);
        if (endInd < 0)
            endInd = -(endInd + 1);
        else
            endInd += 1;
        return new Trajectory(records.subList(startInd, endInd), this.moid);
    }
}
