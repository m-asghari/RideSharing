package edu.usc.infolab.ridesharing.dynamicpricing.optimization.priceanalyzer;

/**
 * Created by Mohammad on 11/15/2017.
 */
public abstract class SupplyDemandChart{
    protected int m_demand;
    protected int m_supply;
    protected double m_optPrice;
    protected double m_equilibriumPrice;
    protected double m_currentPrice;
    private int m_location;

    public SupplyDemandChart(int demand, int supply, int location) {
        m_demand = demand;
        m_supply = supply;
        m_location = location;
        double optDemandPrice = getOptimalDemandPrice();
        m_equilibriumPrice = getEquilibriumPrice();
        m_optPrice = (optDemandPrice > m_equilibriumPrice) ? optDemandPrice : m_equilibriumPrice;
        m_currentPrice = m_optPrice;
    }

    public double getOptimalPrice() {
        return m_optPrice;
    }

    public int getID() {return m_location; }

    public double getCurrentPrice() { return m_currentPrice; }

    public double getAdjustedPrice(double trips) { return adjustedDemand_inverse(trips); }

    public double getCurrentTrips() { return getNumberOfTrips(m_currentPrice); }

    public void addSupply(int count) {
        m_supply += count;
        double optDemandPrice = getOptimalDemandPrice();
        m_equilibriumPrice = getEquilibriumPrice();
        m_optPrice = (optDemandPrice > m_equilibriumPrice) ? optDemandPrice : m_equilibriumPrice;
        m_currentPrice = m_optPrice;
    }

    public void setCurrentPrice(double price) { m_currentPrice = price; }

    public double getNumberOfTrips(double price) {
        return Math.min(adjustedDemand(price), adjustedSupply(price));
    }

    public double getOptimalNumberOfTrips() { return getNumberOfTrips(m_optPrice);}

    public double getMaxNumberOfTrips() {return getNumberOfTrips(m_equilibriumPrice);}

    public double getRevenuee(double price) {
        return getNumberOfTrips(price) * price;
    }

    public double getRevenue() { return getRevenuee(m_currentPrice); }

    //public int getUnusedSupply(double price) {
    //    return m_supply - (int)getNumberOfTrips(price);
    //}

    public int getUnusedSupply() { return m_supply - (int)getNumberOfTrips(m_currentPrice); }

    public double revDec(double deltaTrips) {
        double trips = getNumberOfTrips(m_currentPrice);
        double newPrice = adjustedDemand_inverse(trips + deltaTrips);
        if (newPrice < m_equilibriumPrice) return Double.MAX_VALUE;
        return (m_currentPrice * trips) - ((trips + deltaTrips) * newPrice);
    }

    public double revInc(double deltaTrips) {
        double trips = getNumberOfTrips(m_currentPrice);
        double newOptPrice = getPotentialOptimalPrice(deltaTrips);
        double newSupply = m_supply + deltaTrips;
        double newAdjustedSupply = newSupply * F_w(newOptPrice);
        return (newAdjustedSupply * newOptPrice) - (m_currentPrice * trips);
    }

    protected abstract double getEquilibriumPrice();

    protected abstract double getOptimalDemandPrice();

    protected abstract double F_r(double price);

    //protected abstract double F_r_inverse(double prob);

    protected abstract double F_w(double price);

    //protected abstract double F_w_inverse(double prob);

    protected double adjustedDemand(double price) {
        return m_demand * F_r(price);
    }

    protected abstract double adjustedDemand_inverse(double trips);

    protected double adjustedSupply(double price) {
        return m_supply * F_w(price);
    }

    protected abstract double getPotentialOptimalPrice(double deltaTrips);
}
