package edu.usc.infolab.ridesharing.dynamicpricing;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer.LocalOptimizer;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer.Optimizer;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer.PredictiveOptimizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Mohammad on 11/27/2017.
 */
public class DynamicPricingMain {
    private static final String DEMAND_FILE = "../Data/NYCTaxiDataset/TripData/ReformattedData/05-May/demands/demand_5_1_tract.csv";
    private static final String TRANSITION_FILE = "../Data/NYCTaxiDataset/TripData/ReformattedData/05-May/transitions/transition_5_1_tract.csv";
    private static final int TOTAL_DRIVERS = 10000;

    public static void main(String[] args) throws IOException {
        int[][] demands = getDemands();
        double[][][] transitions = getTransitions();
        int[] supplies = generateInitialSupply(demands[0].length, TOTAL_DRIVERS);

        Optimizer locOptimizer = new LocalOptimizer(demands, supplies, transitions);
        double localOptRev = locOptimizer.Run();

        Optimizer predOptimizer = new PredictiveOptimizer(demands, supplies, transitions);
        double predOptRev = predOptimizer.Run();

        System.out.printf("Local: %.2f, Predictive: %.2f, Ratio: %.2f\n", localOptRev, predOptRev, (predOptRev - localOptRev)/localOptRev);

        /*for (double error = 0.; error <= 1.; error += 0.05) {
            System.out.printf("Prediction Error: %.2f\n", error);
            Optimizer predOptimizer = new PredictiveOptimizer(demands, supplies, transitions, error);
            double predOptRev = predOptimizer.Run();
            System.out.printf("Local: %.2f, Predictive: %.2f, Ratio: %.2f\n", localOptRev, predOptRev, (predOptRev - localOptRev)/localOptRev);
        }*/

    }

    private static int[][] getDemands() throws IOException {
        FileReader fr = new FileReader(DEMAND_FILE);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        String[] fields = line.split(",");
        int timeInstanceSize = Integer.parseInt(fields[0]);
        int locationsSize = Integer.parseInt(fields[1]);

        int[][] demands = new int[timeInstanceSize][locationsSize];
        for (int t = 0; t < timeInstanceSize; t++) {
            line = br.readLine();
            fields = line.split(",");
            for (int i = 0; i < locationsSize; i++) {
                demands[t][i] = Integer.parseInt(fields[i+1]);
            }
        }
        br.close();
        fr.close();
        return demands;
    }

    private static double[][][] getTransitions() throws IOException{
        FileReader fr = new FileReader(TRANSITION_FILE);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        String[] fields = line.split(",");
        int timeInstanceSize = Integer.parseInt(fields[0]);
        int locationsSize = Integer.parseInt(fields[1]);

        double[][][] transitions = new double[timeInstanceSize][locationsSize][locationsSize];
        for (int t = 0; t < timeInstanceSize; t++) {
            br.readLine();
            for (int i = 0; i < locationsSize; i++) {
                line = br.readLine();
                fields = line.split(",");
                for (int j = 1; j < locationsSize; j++) {
                    transitions[t][i][j] = Double.parseDouble(fields[j]);
                }
            }
        }

        br.close();
        fr.close();

        return transitions;
    }

    private static int[] generateInitialSupply(int locationSize, int totalDrivers) {
        int[] supplies = new int[locationSize];
        Random rand = new Random();
        for (int i = 0; i < totalDrivers; i++) {
            supplies[rand.nextInt(locationSize)]++;
        }
        return supplies;
    }

}
