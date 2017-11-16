package edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer;

/**
 * Created by Mohammad on 11/15/2017.
 */
public class TimeInstancePriceAnalyzer1 extends TimeInstancePriceAnalyzer {
    private static final double maxP = 5;

    public TimeInstancePriceAnalyzer1(int demand, int supply) {
        super(demand, supply);
    }

    @Override
    protected double getEquilibriumPrice() {
        return maxP * Math.sqrt(m_demand/(m_demand + m_supply));
    }

    @Override
    protected double getOptimalDemandPrice() {
        return maxP/Math.sqrt(3);
    }

    @Override
    protected double F_r(double price) {
        if (price < 0) return 1;
        if (price > maxP) return 0;
        return 1 - (Math.pow(price, 2) / Math.pow(maxP,2));
    }

    @Override
    protected double F_w(double price) {
        if (price < 0) return 0;
        if (price > maxP) return 1;
        return Math.pow(price, 2) / Math.pow(maxP,2);
    }
}
