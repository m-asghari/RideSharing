package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.TimeInstancePriceAnalyzer;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.TimeInstancePriceAnalyzer1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohammad on 11/15/2017.
 */
public class LocalOptimizer extends Optimizer {
    public LocalOptimizer(int[][] demands, int[] supplies, double[][][] transitions) {
        super(demands, supplies, transitions);
    }

    @Override
    public double Run() {
        double totalRevenue = 0;
        for (int t = 0; t < m_demands.length; t++) {
            double timeInstanceRevenue = 0;
            List<TimeInstancePriceAnalyzer> priceAnalyzers = new ArrayList<>();
            for (int i = 0; i < m_demands.length; i++) {
                TimeInstancePriceAnalyzer1 priceAnalyzer = new TimeInstancePriceAnalyzer1(m_demands[t][i], m_supplies[i]);
                timeInstanceRevenue += priceAnalyzer.getRevenue(priceAnalyzer.getOptimalPrice());
                priceAnalyzers.add(priceAnalyzer);
            }
            int[] futureSupplies = new int[m_supplies.length];
            for (int j = 0; j < futureSupplies.length; j++) {
                futureSupplies[j] = priceAnalyzers.get(j).getUnusedSupply(priceAnalyzers.get(j).getOptimalPrice());
                for (int i = 0; i < priceAnalyzers.size(); i++) {
                    TimeInstancePriceAnalyzer priceAnalyzer = priceAnalyzers.get(i);
                    futureSupplies[j] += (int)(priceAnalyzer.getNumberOfTrips(priceAnalyzer.getOptimalPrice()) * m_transitions[t][i][j]);
                }
            }
        }

        return totalRevenue;
    }
}
