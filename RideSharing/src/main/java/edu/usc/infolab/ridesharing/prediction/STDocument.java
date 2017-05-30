package edu.usc.infolab.ridesharing.prediction;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad on 5/26/2017.
 */
public class STDocument {
    private static int idCounter = 0;

    //public int id;
    public STDocID docID;
    public CellCoordinates spatialCoordinates;
    public int hour;
    int[] data;

    public STDocument(CellCoordinates spatialCoordinates, int hour, int dataSize) {
        //this.id = id;
        this.docID = new STDocID(spatialCoordinates, hour);
        this.spatialCoordinates = spatialCoordinates.clone();
        this.hour = hour;
        this.data = new int[dataSize];
    }

    public void SetWordCount(int locationID, int count) {
        this.data[locationID] = count;
    }

    public void IncrementCount(int locationID) {
        this.data[locationID]++;
    }

    public int GetWordCount(int locationID) {
        return data[locationID];
    }

    public int GetNumberOfWords() {
        return data.length;
    }


}
