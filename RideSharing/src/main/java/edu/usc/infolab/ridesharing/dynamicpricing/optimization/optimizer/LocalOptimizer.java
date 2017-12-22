package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.supplydemandanalyzer.SupplyDemandChart;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.supplydemandanalyzer.SupplyDemandChart1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohammad on 11/15/2017.
 */
public class LocalOptimizer extends Optimizer {
    public LocalOptimizer(double[][] demands, double[] supplies, double[][][] transitions) throws IOException {
        super(demands, supplies, transitions);
    }

    @Override
    public double Run() throws IOException{
        double totalRevenue = 0;
        for (int t = 0; t < m_demands.length; t++) {
            double timeInstanceRevenue = 0;
            List<SupplyDemandChart> sources = new ArrayList<>();
            for (int i = 0; i < m_demands[t].length; i++) {
                SupplyDemandChart1 sdc = new SupplyDemandChart1(m_demands[t][i], m_supplies[i], i);
                log(String.format("%d,%s", t, sdc.getSummary()));
                timeInstanceRevenue += sdc.getRevenue();
                sources.add(sdc);
            }
            double[] futureSupplies = getFutureSupply(sources.toArray(new SupplyDemandChart[0]), t);
            m_supplies = futureSupplies;

            totalRevenue += timeInstanceRevenue;
        }

        return totalRevenue;
    }

    @Override
    protected String getType() {
        return "local";
    }
}
