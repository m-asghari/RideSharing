package edu.usc.infolab.ridesharing;

/**
 * Created by Mohammad on 4/12/2017.
 */
public class TimeDistancePair {
    public Double time;
    public Double distance;

    public TimeDistancePair(double distance, double time) {
        this.time = time;
        this.distance = distance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + ((distance == null) ? 0 : distance.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        TimeDistancePair other = (TimeDistancePair) obj;
        if (distance == null) {
            if (other.distance != null)
                return false;
        } else if (!distance.equals(other.distance))
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        return true;
    }
}
