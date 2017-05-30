package edu.usc.infolab.ridesharing.prediction;

/**
 * Created by Mohammad on 5/26/2017.
 */
public class CellCoordinates {
    public int locationID;
    public int startLng;
    public int startLat;
    public int cellSize;

    public CellCoordinates(int locationID, int startLng, int startLat, int size) {
        this.locationID = locationID;
        this.startLng = startLng;
        this.startLat = startLat;
        this.cellSize = size;
    }

    public CellCoordinates(String[] args) {
        if (args.length < 4) {
            System.out.println("CellCoordinates-CellCoordinates: Not Enought Arguments.");
        }
        this.locationID = Integer.parseInt(args[0]);
        this.startLng = Integer.parseInt(args[1]);
        this.startLat = Integer.parseInt(args[2]);
        this.cellSize = Integer.parseInt(args[3]);
    }

    private CellCoordinates(CellCoordinates other) {
        this.locationID = other.locationID;
        this.startLng = other.startLng;
        this.startLat = other.startLat;
        this.cellSize = other.cellSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CellCoordinates) {
            CellCoordinates other = (CellCoordinates) obj;
            return (this.startLng == other.startLng && this.startLat == other.startLat && this.cellSize == other.cellSize);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (int)((this.startLng+this.startLat+this.cellSize)/113);
    }

    @Override
    public CellCoordinates clone() {
        return new CellCoordinates(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(locationID);sb.append(',');
        sb.append(startLng);sb.append(',');
        sb.append(startLat);sb.append(',');
        sb.append(cellSize);
        return sb.toString();
    }
}
