package edu.usc.infolab.ridesharing.prediction;

import cc.mallet.types.Dirichlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad on 5/31/2017.
 */
public class WeightedAverage {
    public static double[][] ComputeProbabilities(double[][] data) {
        double[][] probs = new double[data.length][];
        for (int i = 0; i < data.length; i++) {
            probs[i] = ComputeProbabilities(data[i]);
        }
        return probs;
    }

    private static double[] ComputeProbabilities(double[] data) {
        double sum = 0.f;
        for (int v = 0; v < data.length; v++) {
            sum += data[v];
        }
        double[] probs = new double[data.length];
        for (int v = 0; v < data.length; v++) {
            probs[v] = (sum != 0) ? data[v] / sum : 1.f / data.length;
        }
        return probs;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<String> dataList = new ArrayList<>();
        String dataFileTemplate = "../Data/NYCTaxiDataset/TripData/PredictionData/%s_%d_%d.csv";
        //int cellSize = 5000;
        //int hour = 1;

        for (int cellSize : new int[]{100, 250, 500, 1000, 2500, 5000}) {
            for (int hour : new int[]{1,2,3,4,5}) {
                FileReader fr = new FileReader(String.format(dataFileTemplate, DataType.TRAINING, cellSize, hour));
                BufferedReader br = new BufferedReader(fr);

                String line = "";
                ArrayList<STDocID> trainingDocIDs = new ArrayList<>();
                dataList = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    trainingDocIDs.add(new STDocID(new CellCoordinates(
                            Integer.parseInt(fields[0]), Integer.parseInt(fields[1]),
                            Integer.parseInt(fields[2]), Integer.parseInt(fields[3])),
                            Integer.parseInt(fields[4])));
                    dataList.add(fields[5]);
                }

                double[][] trainingData = new double[dataList.size()][];
                for (int i = 0; i < dataList.size(); i++) {
                    String[] counts = dataList.get(i).split(":");
                    trainingData[i] = new double[counts.length];
                    for (int v = 0; v < counts.length; v++) {
                        trainingData[i][v] = Double.parseDouble(counts[v]);
                    }
                }
                br.close();
                fr.close();
                dataList.clear();

                double[][] trainingProbs = ComputeProbabilities(trainingData);
                trainingData = null;

                fr = new FileReader(String.format(dataFileTemplate, DataType.TEST, cellSize, hour));
                br = new BufferedReader(fr);

                line = "";
                ArrayList<STDocID> testDocIDs = new ArrayList<>();
                dataList = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    testDocIDs.add(new STDocID(new CellCoordinates(
                            Integer.parseInt(fields[0]), Integer.parseInt(fields[1]),
                            Integer.parseInt(fields[2]), Integer.parseInt(fields[3])),
                            Integer.parseInt(fields[4])));
                    dataList.add(fields[5]);
                }

                double[][] testData = new double[dataList.size()][];
                for (int i = 0; i < dataList.size(); i++) {
                    String[] counts = dataList.get(i).split(":");
                    testData[i] = new double[counts.length];
                    for (int v = 0; v < counts.length; v++) {
                        testData[i][v] = Double.parseDouble(counts[v]);
                    }
                }
                br.close();
                fr.close();
                dataList.clear();

                double[][] testingProbs = ComputeProbabilities(testData);
                testData = null;

                double divergence = 0.f;
                for (STDocID docID : trainingDocIDs) {
                    double[] docTrainProbs = trainingProbs[trainingDocIDs.indexOf(docID)];
                    double[] docTestProbs = testingProbs[testDocIDs.indexOf(docID)];
                    divergence += Divergence.KLD(docTrainProbs, docTestProbs);
                }
                System.out.println(String.format("cellSize: %d, hour: %d -> %.6f", cellSize, hour, divergence/testingProbs.length));
            }
        }
    }
}
