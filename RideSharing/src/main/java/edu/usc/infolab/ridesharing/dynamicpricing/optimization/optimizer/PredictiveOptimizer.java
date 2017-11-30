package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.Counter;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart;
import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart1;

import java.util.*;

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
        int timeIntervalsSize = m_demands.length;
        int locationsSize = m_supplies.length;
        for (int t = 0; t < timeIntervalsSize - 1; t++) {
            double timeInstanceRevenue = 0;

            // Construct Source Supply/Demand Charts
            PriorityQueue<SupplyDemandChart> sourcePQ = new PriorityQueue<>(new Comparator<SupplyDemandChart>() {
                @Override
                public int compare(SupplyDemandChart o1, SupplyDemandChart o2) {
                    Double revRed1 = o1.revDec(1);
                    Double revRed2 = o2.revDec(1);
                    return revRed1.compareTo(revRed2);
                }
            });
            for (int i = 0; i < locationsSize; i++) {
                SupplyDemandChart1 priceAnalyzer = new SupplyDemandChart1(m_demands[t][i], m_supplies[i], i);
                sourcePQ.add(priceAnalyzer);
                //timeInstanceRevenue += priceAnalyzer.getRevenue(priceAnalyzer.getCurrentPrice());
            }

            // Construct Destination SupplyDemand charts
            PriorityQueue<SupplyDemandChart> destinationPQ = new PriorityQueue<>(new Comparator<SupplyDemandChart>() {
                @Override
                public int compare(SupplyDemandChart o1, SupplyDemandChart o2) {
                    Double revInc1 = o1.revInc(1);
                    Double revInc2 = o2.revInc(1);
                    return -1 * revInc1.compareTo(revInc2);
                }
            });

            int[] futureSupplies = getFutureSupply(sourcePQ.toArray(new SupplyDemandChart[0]), t);
            for (int i = 0; i < locationsSize; i++) {
                SupplyDemandChart1 priceAnalyzer = new SupplyDemandChart1(m_demands[t+1][i], futureSupplies[i], i);
                destinationPQ.add(priceAnalyzer);
            }


            // Compute current and max number of trips
            int[][] maxTrips = new int[locationsSize][locationsSize];
            int[][] currentTrips = new int[locationsSize][locationsSize];
            for (Iterator<SupplyDemandChart> it = sourcePQ.iterator(); it.hasNext();) {
                SupplyDemandChart source = it.next();
                for (int j = 0; j < maxTrips.length; j++) {
                    maxTrips[source.getID()][j] = (int)Math.round(m_demands[t][source.getID()] * m_transitions[t][source.getID()][j]);
                    currentTrips[source.getID()][j] = (int)Math.round(source.getCurrentPrice() * m_transitions[t][source.getID()][j]);
                }
            }


            double extraRev = 0;
            while (!destinationPQ.isEmpty() && destinationPQ.peek().revInc(1) > sourcePQ.peek().revDec(1)) {
                SupplyDemandChart destinationTop = destinationPQ.poll();
                double revInc = destinationTop.revInc(1);
                int destID = destinationTop.getID();

                List<SupplyDemandChart> incompatibleSources = new ArrayList<>();
                SupplyDemandChart sourceTop = null;
                int sourceID = -1;
                boolean foundCompatible = false;
                while (!sourcePQ.isEmpty()) {
                    sourceTop = sourcePQ.poll();
                    double revDec = sourceTop.revDec(1);
                    if (revDec > revInc) {
                        incompatibleSources.add(sourceTop);
                        break;
                    }

                    sourceID = sourceTop.getID();
                    if (currentTrips[sourceID][destID] + 1 <= maxTrips[sourceID][destID]) {
                        foundCompatible = true;
                        break;
                    }
                    incompatibleSources.add(sourceTop);
                }
                if (foundCompatible) {
                    //timeInstanceRevenue += (destinationTop.revInc(1) - sourceTop.revDec(1));
                    currentTrips[sourceID][destID]++;

                    double newSourcePrice = sourceTop.getAdjustedPrice(sourceTop.getCurrentTrips() + 1);
                    sourceTop.setCurrentPrice(newSourcePrice);
                    sourcePQ.add(sourceTop);

                    destinationTop.addSupply(1);
                    destinationPQ.add(destinationTop);
                }
                for (SupplyDemandChart chart : incompatibleSources) {
                    sourcePQ.add(chart);
                }
            }

            for (Iterator<SupplyDemandChart> it = sourcePQ.iterator(); it.hasNext();) {
                timeInstanceRevenue += it.next().getRevenue();
            }

            // (TODO): adjust future supplies
            futureSupplies = getFutureSupply(sourcePQ.toArray(new SupplyDemandChart[0]), t);
            m_supplies = futureSupplies;

            totalRevenue += timeInstanceRevenue;
        }

        // (TODO): last time instance
        double lastInstanceRevenue = 0;
        for (int i = 0; i < locationsSize; i++) {
            SupplyDemandChart1 priceAnalyzer = new SupplyDemandChart1(m_demands[timeIntervalsSize-1][i], m_supplies[i], i);
            lastInstanceRevenue += priceAnalyzer.getRevenue();
        }
        totalRevenue += lastInstanceRevenue;

        return totalRevenue;
    }
}
