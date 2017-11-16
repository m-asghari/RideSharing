package edu.usc.infolab.ridesharing.dynamicpricing.optimization.optimizer;

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

    public abstract double Run();
}
