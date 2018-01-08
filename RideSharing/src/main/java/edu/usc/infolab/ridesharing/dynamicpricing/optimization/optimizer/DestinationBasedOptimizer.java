package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.supplydemandanalyzer.SupplyDemandChart;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.supplydemandanalyzer.SupplyDemandChart1;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by mohammad on 1/5/18.
 */
public class DestinationBasedOptimizer extends Optimizer {

    public DestinationBasedOptimizer(double[][] demands, double[] supplies, double[][][] transitions) throws IOException {
        super(demands, supplies, transitions);
    }

    @Override
    public double Run() throws IOException {
        double totalRevenue = 0;
        double trips[][][] = new double[m_demands.length][m_supplies.length][m_supplies.length];
        for (int t = 0; t < m_demands.length; t++) {
            double timeInstanceRevenue = 0;

            for (int i = 0; i < m_demands[t].length; i++) {
                PriorityQueue<SupplyDemandChart> maxPQ = new PriorityQueue<>(new Comparator<SupplyDemandChart>() {
                    @Override
                    public int compare(SupplyDemandChart o1, SupplyDemandChart o2) {
                        Double revInc1 = o1.revAfterSupplyInc(1);
                        Double revInc2 = o2.revAfterSupplyInc(1);
                        return -1 * revInc1.compareTo(revInc2);
                    }
                });

                PriorityQueue<SupplyDemandChart> minPQ = new PriorityQueue<>(new Comparator<SupplyDemandChart>() {
                    @Override
                    public int compare(SupplyDemandChart o1, SupplyDemandChart o2) {
                        Double revDec1 = o1.revAfterSupplyDec(1);
                        Double revDec2 = o2.revAfterSupplyDec(1);
                        return revDec1.compareTo(revDec2);
                    }
                });

                for (int j = 0; j < 0; j++) {
                    SupplyDemandChart1 sdc = new SupplyDemandChart1(
                            m_demands[t][i] * m_transitions[t][i][j],
                            m_supplies[i] * m_transitions[t][i][j], j);
                    maxPQ.add(sdc);
                    minPQ.add(sdc);
                }

                while (maxPQ.peek().revAfterSupplyInc(1) > minPQ.peek().revAfterSupplyDec(1)) {
                    SupplyDemandChart addSDC = maxPQ.poll();
                    SupplyDemandChart removeSDC = minPQ.poll();
                    minPQ.remove(addSDC);
                    maxPQ.remove(removeSDC);

                    addSDC.addSupply(1);
                    removeSDC.removeSupply(1);

                    maxPQ.add(addSDC);
                    minPQ.add(addSDC);
                    maxPQ.add(removeSDC);
                    minPQ.add(removeSDC);
                }

                for (SupplyDemandChart sdc : maxPQ) {
                    timeInstanceRevenue += sdc.getRevenue();
                    trips[t][i][sdc.getID()] = sdc.getCurrentTrips();
                }

            }
            double[] futureSupplies = getFutureSupply(trips[t], m_supplies);
            m_supplies = futureSupplies;

            totalRevenue += timeInstanceRevenue;
        }

        return totalRevenue;
    }

    private double[] getFutureSupply(double[][] trips, double[] priorSupply) {
        double[] remainingSupply = new double[trips.length];
        for (int j = 0; j < trips.length; j++) {
            remainingSupply[j] = priorSupply[j];
        }
        double[] futureSupply = new double[trips.length];
        for (int j = 0; j < trips.length; j++) {
            for (int i = 0; i < trips.length; i++) {
                futureSupply[j] += trips[i][j];
                remainingSupply[i] -= trips[i][j];
            }
        }
        for (int j = 0; j < trips.length; j++) {
            futureSupply[j] += remainingSupply[j];
        }
        return futureSupply;
    }

    @Override
    protected String getType() {
        return "DB";
    }
}
