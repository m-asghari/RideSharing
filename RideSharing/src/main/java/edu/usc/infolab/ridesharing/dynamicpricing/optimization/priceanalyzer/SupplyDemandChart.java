package edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer;

/**
 * Created by Mohammad on 11/15/2017.
 */
public abstract class TimeInstancePriceAnalyzer {
    protected int m_demand;
    protected int m_supply;
    protected double m_optPrice;
    protected double m_equilibriumPrice;

    public TimeInstancePriceAnalyzer(int demand, int supply) {
        m_demand = demand;
        m_supply = supply;
        double optDemandPrice = getOptimalDemandPrice();
        m_equilibriumPrice = getEquilibriumPrice();
        m_optPrice = (optDemandPrice > m_equilibriumPrice) ? optDemandPrice : m_equilibriumPrice;
    }

    public double getOptimalPrice() {
        return m_optPrice;
    }

    public double getNumberOfTrips(double price) {
        return Math.min(adjustedDemand(price), adjustedSupply(price));
    }

    public double getOptimalNumberOfTrips() { return getNumberOfTrips(m_optPrice);}

    public double getMaxNumberOfTrips() {return getNumberOfTrips(m_equilibriumPrice);}

    public double getRevenue(double price) {
        return getNumberOfTrips(price) * price;
    }

    public int getUnusedSupply(double price) {
        return m_supply - (int)getNumberOfTrips(price);
    }

    public double RevRed(double deltaTrips) {
        double optTrips = getNumberOfTrips(m_optPrice);
        double demandInverse = adjustedDemand_inverse(optTrips + deltaTrips);
        return (m_optPrice * optTrips) - (optTrips * demandInverse) - deltaTrips * demandInverse;
    }

    public double RevInc(double deltaTrips) {
        double optTrips = getNumberOfTrips(m_optPrice);
        double newOptPrice = getPotentialOptimalPrice(deltaTrips);
        double newSupply = m_supply + deltaTrips;
        double newAdjustedSupply = newSupply * F_w(newOptPrice);
        return (newAdjustedSupply * newOptPrice) - (m_optPrice * optTrips);
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

    protected abstract double adjustedDemand_inverse(double trips);

    protected int adjustedSupply(double price) {
        return (int)(m_supply * F_w(price));
    }

    protected abstract double getPotentialOptimalPrice(double deltaTrips);
}
