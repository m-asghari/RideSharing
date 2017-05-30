package edu.usc.infolab.ridesharing.launcher;

import edu.usc.infolab.ridesharing.prediction.CellCoordinates;
import edu.usc.infolab.ridesharing.prediction.DataType;
import edu.usc.infolab.ridesharing.prediction.STDocument;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mohammad on 5/25/17.
 */
public class SpatialDocumentGenerator {
    private static final int LNG_TOTAL_DIFF = 128000;
    private static final int LAT_TOTAL_DIFF = 111000;
    private static final int HOUR_START = 6;
    private static final int HOUR_END = 24;

    private static int CELL_SIZE = 1000;
    private static int HOUR_SKIP = 3;

    // Following are some statistics for the grid (lng/lat deltas) in the month of January
    private static final int MIN_PICKUP_LNG_DELTA = 1842;
    private static final int MAX_PICKUP_LNG_DELTA = 110544;
    private static final int MIN_PICKUP_LAT_DELTA = 0;
    private static final int MAX_PICKUP_LAT_DELTA = 125562;
    private static final int MIN_DROPOFF_LNG_DELTA = 2529;
    private static final int MAX_DROPOFF_LNG_DELTA = 118395;
    private static final int MIN_DROPOFF_LAT_DELTA = 153;
    private static final int MAX_DROPOFF_LAT_DELTA = 125601;

    private static String dbDriver = "oracle.jdbc.driver.OracleDriver";
    private static String dbConnectionString = "jdbc:oracle:thin:@localhost:1521:xe";
    private static String dbUser = "masghari";
    private static String dbPassword = "rth@usc";
    private static String dbDateFormat = "yyyy-mm-dd hh24:mi:ss";

    private static Connection dbConnection = null;
    private static Statement statement = null;
    private static ResultSet rs = null;

    private static final int VIABILITY_THRESHOLD = 1000;
    private static HashMap<CellCoordinates, Integer> GetViableLocations(String filename) throws IOException{
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);

        HashMap<CellCoordinates, Integer> eventCounts = new HashMap<>();
        for (int startLng = 0; startLng < LNG_TOTAL_DIFF; startLng += CELL_SIZE) {
            for (int startLat = 0; startLat < LAT_TOTAL_DIFF; startLat += CELL_SIZE) {
                eventCounts.put(new CellCoordinates(-1, startLng, startLat, CELL_SIZE), 0);
            }
        }

        String line = "";
        int recordCounter = 0;
        while((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            int pickUpLngDelta = ((Double)Double.parseDouble(fields[15])).intValue();
            int pickUpLatDelta = ((Double)Double.parseDouble(fields[14])).intValue();
            int dropOffLngDelta = ((Double)Double.parseDouble(fields[19])).intValue();
            int dropOffLatDelta = ((Double)Double.parseDouble(fields[18])).intValue();

            int pickUpStartLng = pickUpLngDelta - (pickUpLngDelta % CELL_SIZE);
            int pickUpStartLat = pickUpLatDelta - (pickUpLatDelta % CELL_SIZE);
            CellCoordinates pickUpKey = new CellCoordinates(-1, pickUpStartLng, pickUpStartLat, CELL_SIZE);
            int dropOffStartLng = dropOffLngDelta - (dropOffLngDelta % CELL_SIZE);
            int dropOffStartLat = dropOffLatDelta - (dropOffLatDelta % CELL_SIZE);
            CellCoordinates dropOffKey = new CellCoordinates(-1, dropOffStartLng, dropOffStartLat, CELL_SIZE);

            if (!eventCounts.containsKey(pickUpKey) || !eventCounts.containsKey(dropOffKey)) {
                continue;
            }
            eventCounts.put(pickUpKey, eventCounts.get(pickUpKey) + 1);
            eventCounts.put(dropOffKey, eventCounts.get(dropOffKey) + 1);
            recordCounter++;
            if (recordCounter % 1000000 == 0) {
                System.out.println(String.format("SpatialDocGen-GetViableLocations: Processed %d records", recordCounter));
            }
        }
        br.close();
        fr.close();

        HashMap<CellCoordinates, Integer> viableLocations = new HashMap<>();
        int locationID = 0;
        for (Map.Entry<CellCoordinates, Integer> e : eventCounts.entrySet()) {
            if (e.getValue() > VIABILITY_THRESHOLD) {
                viableLocations.put(
                        new CellCoordinates(locationID, e.getKey().startLng, e.getKey().startLat, e.getKey().cellSize),
                        locationID);
                locationID++;
            };
        }
        //SaveViableLocations(viableLocations);
        return viableLocations;
    }

    private static void SaveViableLocations(HashMap<CellCoordinates, Integer> viableLocations) throws IOException {
        FileWriter fw = new FileWriter(
                String.format("../Data/NYCTaxiDataset/TripData/PredictionData/locations_%d_%d.csv",
                        CELL_SIZE, HOUR_SKIP));
        BufferedWriter bw = new BufferedWriter(fw);

        for (CellCoordinates location : viableLocations.keySet()) {
            bw.write(location.toString());
            bw.newLine();
        }
        bw.close();
        fw.close();
    }

    private static HashMap<CellCoordinates, Integer> LoadViableLocations() throws IOException{
        FileReader fr = new FileReader(
                String.format("../Data/NYCTaxiDataset/TripData/PredictionData/locations_%d_%d.csv",
                        CELL_SIZE, HOUR_SKIP));
        BufferedReader br = new BufferedReader(fr);

        HashMap<CellCoordinates, Integer> viableLocations = new HashMap<>();
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            viableLocations.put(new CellCoordinates(fields), Integer.parseInt(fields[0]));
        }
        br.close();
        fr.close();
        return viableLocations;
    }

    private static final int TRAINING_DATA_CUTTOFF_DAY = 21;
    private static ArrayList<STDocument> GetData(String filename, HashMap<CellCoordinates, Integer> locations, DataType type)
            throws IOException{
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);

        ArrayList<STDocument> documents = new ArrayList<>();
        int idCounter = 0;

        HashMap<Integer, HashMap<CellCoordinates, Integer>> documentFinder = new HashMap<>();
        for (int hour = HOUR_START; hour < HOUR_END; hour += HOUR_SKIP) {
            HashMap<CellCoordinates, Integer> perHourMap = new HashMap<>();
            for (CellCoordinates pickUp : locations.keySet()) {
                documents.add(new STDocument(pickUp, hour, locations.size()));
                perHourMap.put(pickUp, idCounter);
                idCounter++;
            }
            documentFinder.put(hour, perHourMap);
        }

        int startDay = (type == DataType.TRAINING) ? 0 : TRAINING_DATA_CUTTOFF_DAY;
        int endDay = (type == DataType.TRAINING) ? TRAINING_DATA_CUTTOFF_DAY : 31;

        String line = "";
        int recordCounter = 0;
        while((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            int pickUpLngDelta = ((Double)Double.parseDouble(fields[15])).intValue();
            int pickUpLatDelta = ((Double)Double.parseDouble(fields[14])).intValue();
            int dropOffLngDelta = ((Double)Double.parseDouble(fields[19])).intValue();
            int dropOffLatDelta = ((Double)Double.parseDouble(fields[18])).intValue();
            int dayOfMonth = Integer.parseInt(fields[3]);
            int hourOfDay = Integer.parseInt(fields[5]);

            int pickUpStartLng = pickUpLngDelta - (pickUpLngDelta % CELL_SIZE);
            int pickUpStartLat = pickUpLatDelta - (pickUpLatDelta % CELL_SIZE);
            CellCoordinates pickUpCell = new CellCoordinates(-1, pickUpStartLng, pickUpStartLat, CELL_SIZE);
            int dropOffStartLng = dropOffLngDelta - (dropOffLngDelta % CELL_SIZE);
            int dropOffStartLat = dropOffLatDelta - (dropOffLatDelta % CELL_SIZE);
            CellCoordinates dropOffCell = new CellCoordinates(-1, dropOffStartLng, dropOffStartLat, CELL_SIZE);
            int hour = hourOfDay - ((hourOfDay -HOUR_START) % HOUR_SKIP);

            if (locations.containsKey(pickUpCell) && locations.containsKey(dropOffCell)) {
                if (dayOfMonth >= startDay && dayOfMonth < endDay) {
                    if (hour >= HOUR_START && hour < HOUR_END) {
                        documents.get(documentFinder.get(hour).get(pickUpCell)).IncrementCount(
                                locations.get(dropOffCell));
                    }
                }
            }

            recordCounter++;
            if (recordCounter % 1000000 == 0) {
                System.out.println(String.format("SpatialDocGen-GetData: Processed %d records", recordCounter));
            }
        }
        br.close();
        fr.close();
        SaveData(documents, type);
        return documents;
    }

    public static void SaveData(ArrayList<STDocument> documents, DataType type) throws IOException {
        FileWriter fw = new FileWriter(
                String.format("../Data/NYCTaxiDataset/TripData/PredictionData/%s_%d_%d.csv",
                        type.toString(), CELL_SIZE, HOUR_SKIP));
        BufferedWriter bw = new BufferedWriter(fw);

        for (STDocument document : documents) {
            StringBuilder sb = new StringBuilder();
            sb.append(document.spatialCoordinates.locationID);
            sb.append(',');
            sb.append(document.hour);
            sb.append(',');
            sb.append(document.GetWordCount(0));
            for (int locationID = 1; locationID < document.GetNumberOfWords(); locationID++) {
                sb.append(':');
                sb.append(document.GetWordCount(locationID));
            }
            sb.append("\n");
            bw.append(sb.toString());
        }

        bw.close();
        fw.close();
    }

    public static void GenerateTrainTestData(String dataFile, int cellSize, int hourSkip)
            throws IOException, SQLException {
        CELL_SIZE = cellSize;
        HOUR_SKIP = hourSkip;
        System.out.println(String.format("Starting Cell Size %d and Hour Skip %d",
                cellSize, hourSkip));
        //HashMap<CellCoordinates, Integer> viableLocations = GetViableLocations(dataFile);
        HashMap<CellCoordinates, Integer> viableLocations = LoadViableLocations();
        ArrayList<STDocument> trainingData = GetData(dataFile, viableLocations, DataType.TRAINING);
        ArrayList<STDocument> testData = GetData(dataFile, viableLocations, DataType.TEST);
    }


    public static void main(String[] args) throws SQLException, IOException {
        String dataFile = "../Data/NYCTaxiDataset/TripData/ProcessedData/trips_procesed_0.csv";
        for (int cellSize : new int[]{/*100,*/ 250, 500, 1000, 2500, 5000}) {
            CELL_SIZE = cellSize;
            HashMap<CellCoordinates, Integer> viableLocations = GetViableLocations(dataFile);
            //HashMap<CellCoordinates, Integer> viableLocations = LoadViableLocations();
            for (int hourSkip : new int[]{1, 2, 3, 4, 5}){
                HOUR_SKIP = hourSkip;
                System.out.println(String.format("Starting Cell Size %d and Hour Skip %d",
                        cellSize, hourSkip));
                SaveViableLocations(viableLocations);
                ArrayList<STDocument> trainingData = GetData(dataFile, viableLocations, DataType.TRAINING);
                ArrayList<STDocument> testData = GetData(dataFile, viableLocations, DataType.TEST);
            }
        }
    }

    /*public static HashMap<Integer, CellCoordinates> GetViableLocations() throws SQLException, IOException {
        HashMap<Integer, CellCoordinates> viableLocations = new HashMap<>();
        int idCounter = 0;
        try {
            Class.forName(dbDriver);
            dbConnection = DriverManager.getConnection(dbConnectionString, dbUser, dbPassword);

            for (int startLng = 0; startLng < LNG_TOTAL_DIFF; startLng += CELL_SIZE) {
                for (int startLat = 0; startLat < LAT_TOTAL_DIFF; startLat += CELL_SIZE) {
                    // Number of pickup points in cell
                    int pickUpCount = 0;
                    String pickUpQueryString = String.format("SELECT COUNT(*) as CNT FROM TRIPS " +
                                    "WHERE PICKUPLNGDELTA >= %d AND PICKUPLNGDELTA < %d " +
                                    "AND PICKUPLATDELTA >= %d AND PICKUPLATDELTA < %d",
                            startLng, startLng + CELL_SIZE, startLat, startLat + CELL_SIZE);
                    statement = dbConnection.createStatement();
                    rs = statement.executeQuery(pickUpQueryString);
                    if (rs.next()) {
                        pickUpCount = rs.getInt("CNT");
                    }
                    rs.close();
                    statement.close();

                    // Number of dropoff points in cell
                    int dropOffCount = 0;
                    String dropOffQueryString = String.format("SELECT COUNT(*) as CNT FROM TRIPS " +
                                    "WHERE DROPOFFLNGDELTA >= %d AND DROPOFFLNGDELTA < %d " +
                                    "AND DROPOFFLATDELTA >= %d AND DROPOFFLATDELTA < %d",
                            startLng, startLng + CELL_SIZE, startLat, startLat + CELL_SIZE);

                    statement = dbConnection.createStatement();
                    rs = statement.executeQuery(dropOffQueryString);
                    if (rs.next()) {
                        dropOffCount = rs.getInt("CNT");
                    }
                    rs.close();
                    statement.close();

                    System.out.print("STDocumentGenerator-GetViabaleCellCoordinates: ");
                    if (pickUpCount + dropOffCount > VIABILITY_THRESHOLD) {
                        viableLocations.put(idCounter, new CellCoordinates(idCounter, startLng, startLat, CELL_SIZE));
                        idCounter++;
                        System.out.println(String.format("CellCoordinate %d,%d is viable", startLng, startLat));
                    } else {
                        System.out.println(String.format("CellCoordinate %d,%d is not viable", startLng, startLat));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        System.out.print("STDocumentGenerator-GetViabaleCellCoordinates: ");
        System.out.println(String.format("viable locations size: %d", viableLocations.size()));
        SaveViableLocations(viableLocations);
        return viableLocations;
    }*/

    /*public static ArrayList<STDocument> GetData(
            HashMap<Integer, CellCoordinates> locations, DataType type) throws SQLException {
        ArrayList<STDocument> documents = new ArrayList<>();
        int idCounter = 0;

        int startDay = (type == DataType.TRAINING) ? 0 : TRAINING_DATA_CUTTOFF_DAY;
        int endDay = (type == DataType.TRAINING) ? TRAINING_DATA_CUTTOFF_DAY : 31;

        try {
            Class.forName(dbDriver);
            dbConnection = DriverManager.getConnection(dbConnectionString, dbUser, dbPassword);

            for (int hour = HOUR_START; hour < HOUR_END; hour += HOUR_SKIP) {
                System.out.println(String.format("SpatialDocumentGen-GetData: Starting Hour: %d", hour));
                for (CellCoordinates pickUp : locations.values()) {
                    STDocument document = new STDocument(idCounter, pickUp, hour, locations.size());
                    idCounter++;

                    for (CellCoordinates dropOff : locations.values()) {
                        int wordCount = 0;
                        String queryString = String.format("SELECT COUNT(*) as CNT FROM TRIPS " +
                                "WHERE PICKUPLNGDELTA >= %d AND PICKUPLNGDELTA < %d " +
                                "AND PICKUPLATDELTA >= %d AND PICKUPLATDELTA < %d " +
                                "AND DROPOFFLNGDELTA >= %d AND DROPOFFLNGDELTA < %d " +
                                "AND DROPOFFLATDELTA >= %d AND DROPOFFLATDELTA < %d " +
                                "AND REQDAYOFMONTH >= %d AND REQDAYOFMONTH < %d " +
                                "AND REQHOUR >= %d AND REQHOUR < %d",
                                pickUp.startLng, pickUp.startLng + pickUp.cellSize,
                                pickUp.startLat, pickUp.startLat + pickUp.cellSize,
                                dropOff.startLng, dropOff.startLng + dropOff.cellSize,
                                dropOff.startLat, dropOff.startLat + dropOff.cellSize,
                                startDay, endDay, hour, hour + HOUR_SKIP);

                        statement = dbConnection.createStatement();
                        rs = statement.executeQuery(queryString);
                        if (rs.next()) {
                            wordCount = rs.getInt("CNT");
                        }
                        document.SetWordCount(new Cell(dropOff, hour), wordCount);
                        rs.close();
                        statement.close();
                    }
                    System.out.println(String.format("SpatialDocumentGen-GetData: document %d finished", idCounter));
                    documents.add(document);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return documents;
    }*/
}
