package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;

import javax.activity.InvalidActivityException;

public class AuctionRequest extends Request {
    public double serverProfit;
    public double fpaProfit;
    public double spaProfit;
    public double sparvProfit;

    public AuctionRequest(GPSPoint source, GPSPoint dest, Time requestTime,
                          int maxWaitTime) {
        super(source, dest, requestTime, maxWaitTime);
        serverProfit = 0;
        fpaProfit = 0;
        spaProfit = 0;
        sparvProfit = 0;
    }
    
    public AuctionRequest(String[] args) {
        super(args);
        try {
            if (args.length < 23) {
                throw new InvalidActivityException("Not enough arguments for AuctionRequest.");
            }
            this.serverProfit = Double.parseDouble(args[22]);
            
        } catch (InvalidActivityException iae) {
            iae.printStackTrace();
        }
    }
    
    @Override
    public String PrintShortResults() {
        StringBuilder results = new StringBuilder();
        results.append(super.PrintShortResults());
        results.append(String.format("%.2f,", serverProfit));
        results.append(String.format("%.2f,", fpaProfit));
        results.append(String.format("%.2f,", spaProfit));
        results.append(String.format("%.2f,", sparvProfit));
        return results.toString();
    }
    
    @Override
    public String PrintLongResults() {
        StringBuilder results = new StringBuilder();
        results.append(super.PrintLongResults());
        results.append(String.format("Server Profit: %.2f",
                serverProfit));
        results.append(String.format("FPA Profit: %.2f",
                fpaProfit));
        results.append(String.format("SPA Profit: %.2f",
                spaProfit));
        results.append(String.format("SPARV Profit: %.2f",
                sparvProfit));
        return results.toString();
    }
}