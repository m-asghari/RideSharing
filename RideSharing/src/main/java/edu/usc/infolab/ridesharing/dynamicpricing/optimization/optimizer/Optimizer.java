package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart;

import java.util.List;

/**
 * Created by Mohammad on 11/15/2017.
 */
public abstract class Optimizer {
    protected int[][] m_demands;
    protected int[] m_supplies;
    protected double[][][] m_transitions;

    public Optimizer(int[][] demands, int[] supplies, double[][][] transitions) {
        m_demands = demands;
        m_supplies = supplies;
        m_transitions = transitions;
    }

    protected int[] getFutureSupply(SupplyDemandChart[] sources, int currentTime) {
        int[] futureSupplies = new int[sources.length];
        for (int j = 0; j < futureSupplies.length; j++) {
            futureSupplies[j] = sources[j].getUnusedSupply();
            for (int i = 0; i < sources.length; i++) {
                SupplyDemandChart priceAnalyzer = sources[i];
                futureSupplies[j] += (int)(priceAnalyzer.getCurrentTrips() * m_transitions[currentTime][i][j]);
            }
        }
        return futureSupplies;
    }

    public abstract double Run();
}
