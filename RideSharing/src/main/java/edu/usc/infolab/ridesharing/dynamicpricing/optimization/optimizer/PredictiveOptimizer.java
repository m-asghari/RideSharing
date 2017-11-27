package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.Counter;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.TimeInstancePriceAnalyzer;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.TimeInstancePriceAnalyzer1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohammad on 11/20/17.
 */
public class PredictiveOptimizer extends Optimizer {
    public PredictiveOptimizer(int[][] demands, int[] supplies, double[][][] transitions) {
        super(demands, supplies, transitions);
    }

    @Override
    public double Run() {
        double totalRevenue = 0;
        for (int t = 0; t < m_demands.length - 1; t++) {
            List<TimeInstancePriceAnalyzer> sources = new ArrayList<>();
            for (int i = 0; i < m_demands.length; i++) {
                TimeInstancePriceAnalyzer1 priceAnalyzer = new TimeInstancePriceAnalyzer1(m_demands[t][i], m_supplies[i]);
                sources.add(priceAnalyzer);
            }

            double[] sourceTripCounts = new double[m_demands.length];
            double[] sourceMaxTrips = new double[m_demands.length];
            for (int i = 0; i < sources.size(); i++) {
                sourceTripCounts[i] = sources.get(i).getOptimalNumberOfTrips();
                sourceMaxTrips[i] = sources.get(i).getMaxNumberOfTrips();
            }

            Counter tripCounter = new Counter(sourceTripCounts, sourceMaxTrips);
            double bestRevInc = 0;
            double[] bestTripNumber = new double[m_demands.length];

            while (tripCounter.hasNext()) {

            }



            int[] futureSupplies = new int[m_supplies.length];
            for (int j = 0; j < futureSupplies.length; j++) {
                futureSupplies[j] = priceAnalyzers.get(j).getUnusedSupply(priceAnalyzers.get(j).getOptimalPrice());
                for (int i = 0; i < priceAnalyzers.size(); i++) {
                    TimeInstancePriceAnalyzer priceAnalyzer = priceAnalyzers.get(i);
                    futureSupplies[j] += (int)(priceAnalyzer.getNumberOfTrips(priceAnalyzer.getOptimalPrice()) * m_transitions[t][i][j]);
                }
            }
            totalRevenue += timeInstanceRevenue;
        }

        return totalRevenue;
    }
}
