package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart1;

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
            List<SupplyDemandChart> sources = new ArrayList<>();
            for (int i = 0; i < m_demands.length; i++) {
                SupplyDemandChart1 priceAnalyzer = new SupplyDemandChart1(m_demands[t][i], m_supplies[i], i);
                timeInstanceRevenue += priceAnalyzer.getRevenue();
                sources.add(priceAnalyzer);
            }
            int[] futureSupplies = getFutureSupply(sources.toArray(new SupplyDemandChart[0]), t);
            m_supplies = futureSupplies;

            totalRevenue += timeInstanceRevenue;
        }

        return totalRevenue;
    }
}
