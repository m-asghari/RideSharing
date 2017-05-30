package edu.usc.infolab.ridesharing.prediction;

/**
 * Created by Mohammad on 5/30/2017.
 */
public class STDocID {
    public CellCoordinates cell;
    public int hour;

    public STDocID(CellCoordinates cell, int hour) {
        this.cell = cell.clone();
        this.hour = hour;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof STDocID) {
            STDocID other = (STDocID) obj;
            return (this.cell.equals(other.cell) && this.hour == other.hour);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (this.cell.hashCode()+this.hour)/113;
    }
}
