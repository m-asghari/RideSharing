package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.supplydemandanalyzer.SupplyDemandChart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Mohammad on 11/15/2017.
 */
public abstract class Optimizer {
    protected double[][] m_demands;
    protected double[] m_supplies;
    protected double[][][] m_transitions;

    private FileWriter fw;
    private BufferedWriter bw;

    public Optimizer(double[][] demands, double[] supplies, double[][][] transitions) throws IOException{
        m_demands = demands;
        m_supplies = supplies;
        m_transitions = transitions;

        for (int t = 0; t < transitions.length; t++) {
            for (int i = 0; i < transitions[t].length; i++) {
                double sum = 0;
                for (int j = 0; j < transitions[t][i].length; j++) {
                    if (Double.isNaN(transitions[t][i][j]))
                        System.out.println("Optimizer - Constructor - 1");
                    else
                        sum += transitions[t][i][j];
                }
            }
        }

        fw = new FileWriter(String.format("summary_%s_%s.csv", getType(), new SimpleDateFormat("dd-MMM-yy_HH-mm-ss").format(Calendar.getInstance().getTime())));
        bw = new BufferedWriter(fw);
    }

    protected double[] getFutureSupply(SupplyDemandChart[] sources, int currentTime) {
        SupplyDemandChart[] orderedSources = new SupplyDemandChart[sources.length];
        for (int i = 0; i < sources.length; i++) {
            orderedSources[sources[i].getID()] = sources[i];
        }

        double totalSup1 = 0;
        for (int i = 0; i < orderedSources.length; i++) totalSup1 += m_supplies[i];
        double[] srcTotalTrips = new double[orderedSources.length];
        double[] futureSupplies = new double[orderedSources.length];
        for (int j = 0; j < futureSupplies.length; j++) {
            for (int i = 0; i < orderedSources.length; i++) {
                double trips = orderedSources[i].getCurrentTrips() * m_transitions[currentTime][i][j];
                futureSupplies[j] += trips;
                srcTotalTrips[i] += trips;
            }
        }
        for (int i = 0; i < futureSupplies.length; i++) {
            futureSupplies[i] += m_supplies[i] - srcTotalTrips[i];
        }
        double totalSup2 = 0;
        for (int i = 0; i < orderedSources.length; i++) totalSup2 += futureSupplies[i];
        if (Math.abs(totalSup1 - totalSup2) > 0.1) {
            System.out.println("Optimizer - Get Future Supplies - 1");
        }
        return futureSupplies;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bw.close();
        fw.close();
    }

    public abstract double Run() throws IOException;

    protected void log(String message) throws IOException {
        bw.flush();
        bw.write(message);
        bw.write("\n");
    }

    protected abstract String getType();
}
