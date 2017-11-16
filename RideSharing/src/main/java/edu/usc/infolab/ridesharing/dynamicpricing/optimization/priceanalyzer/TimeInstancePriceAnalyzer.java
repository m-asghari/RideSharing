package edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer;

/**
 * Created by Mohammad on 11/15/2017.
 */
public abstract class TimeInstancePriceAnalyzer {
    protected int m_demand;
    protected int m_supply;
    protected double m_optPrice;

    public TimeInstancePriceAnalyzer(int demand, int supply) {
        m_demand = demand;
        m_supply = supply;
        double optDemandPrice = getOptimalDemandPrice();
        double equilibriumPrice = getEquilibriumPrice();
        m_optPrice = (optDemandPrice > equilibriumPrice) ? optDemandPrice : equilibriumPrice;
    }

    public double getOptimalPrice() {
        return m_optPrice;
    }

    public double getNumberOfTrips(double price) {
        return Math.min(adjustedDemand(price), adjustedSupply(price));
    }

    public double getRevenue(double price) {
        return getNumberOfTrips(price) * price;
    }

    public int getUnusedSupply(double price) {
        return m_supply - (int)getNumberOfTrips(price);
    }

    protected abstract double getEquilibriumPrice();

    protected abstract double getOptimalDemandPrice();

    protected abstract double F_r(double price);

    //protected abstract double F_r_inverse(double prob);

    protected abstract double F_w(double price);

    //protected abstract double F_w_inverse(double prob);

    protected int adjustedDemand(double price) {
        return (int)(m_demand * F_r(price));
    }

    protected int adjustedSupply(double price) {
        return (int)(m_supply * F_w(price));
    }
}
