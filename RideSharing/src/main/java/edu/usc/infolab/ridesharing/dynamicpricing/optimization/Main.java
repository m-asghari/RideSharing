package edu.usc.infolab.ridesharing.dynamicpricing.optimization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;

/**
 * Created by Mohammad on 11/15/2017.
 */
public class Main {
    private static final String demandFilePath = "";
    private static final String transitionFilePath = "";

    public static void main(String[] args) throws Exception, IOException{
        FileReader demandFileReader = new FileReader(demandFilePath);
        BufferedReader demandReader = new BufferedReader(demandFileReader);

        FileReader transitionFileReader = new FileReader(transitionFilePath);
        BufferedReader transitionReader = new BufferedReader(transitionFileReader);

        String demandLine = demandReader.readLine();
        String transitionLine = transitionReader.readLine();

        if (!demandLine.equals(transitionLine)) {
            throw new Exception("Incompatible demand and transition files");
        }

        String[] fields = demandLine.split(",");
        int timeIntervalSize = Integer.parseInt(fields[0]);
        int locationSize = Integer.parseInt(fields[1]);

        int currentTime = -1;
        int[] currentDemand = new int[locationSize];
        int[] futureDemand = new int[locationSize];
        double[][] currentTransition = new double[locationSize][];
        for (int i = 0; i < locationSize; i++) {
            currentTransition[i] = new double[locationSize];
        }

        demandLine = demandReader.readLine();
        fields = demandLine.split(",");
        currentTime = Integer.parseInt(fields[0]);
        for (int i = 0; i < locationSize; i++) {
            futureDemand[i] = Integer.parseInt(fields[i+1]);
        }

        while (currentTime < timeIntervalSize) {
            for (int i = 0; i < locationSize; i++) {
                currentDemand[i] = futureDemand[i];
            }
            demandLine = demandReader.readLine();
            int nextTime = -1;
            if (demandLine != null) {
                fields = demandLine.split(",");
                nextTime = Integer.parseInt(fields[0]);
                for (int i = 0; i < locationSize; i++) {
                    futureDemand[i] = Integer.parseInt(fields[i + 1]);
                }
            } else {
                futureDemand = null;
            }

            transitionLine = transitionReader.readLine(); // pass the line with time
            for (int i = 0; i < locationSize; i++) {
                transitionLine = transitionReader.readLine();
                fields = transitionLine.split(",");
                for (int j = 0; j < locationSize; j++) {
                    currentTransition[i][j] = Double.parseDouble(fields[j]);
                }
            }

            currentTime = nextTime;
        }
    }
}
