package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

import edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer.SupplyDemandChart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Mohammad on 11/15/2017.
 */
public abstract class Optimizer {
    protected int[][] m_demands;
    protected int[] m_supplies;
    protected double[][][] m_transitions;

    private FileWriter fw;
    private BufferedWriter bw;

    public Optimizer(int[][] demands, int[] supplies, double[][][] transitions) throws IOException{
        m_demands = demands;
        m_supplies = supplies;
        m_transitions = transitions;

        fw = new FileWriter(String.format("summary_%s_%s.csv", getType(), new SimpleDateFormat("dd-MMM-yy_HH-mm-ss").format(Calendar.getInstance().getTime())));
        bw = new BufferedWriter(fw);
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
