package edu.usc.infolab.ridesharing.prediction;

/**
 * Created by Mohammad on 5/26/2017.
 */
public class Cell {
    public CellCoordinates spatialCoordinates;
    public int hour;

    public Cell(CellCoordinates spatialCoordinates, int hour) {
        this.spatialCoordinates = spatialCoordinates.clone();
        this.hour = hour;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cell) {
            Cell other = (Cell) obj;
            boolean isEqual = this.spatialCoordinates.locationID == other.spatialCoordinates.locationID
                    && this.hour == other.hour;
            return isEqual;
        }
        return super.equals(obj);
    }
}
