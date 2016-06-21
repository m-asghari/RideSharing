package edu.usc.infolab.ridesharing.auction;

import javax.activity.InvalidActivityException;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;

public class AuctionRequest extends Request {
    public double serverProfit;
    
    public AuctionRequest(GPSPoint source, GPSPoint dest, Time requestTime,
            int maxWaitTime) {
        super(source, dest, requestTime, maxWaitTime);
        serverProfit = 0;
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
        return results.toString();
    }
    
    @Override
    public String PrintLongResults() {
        StringBuilder results = new StringBuilder();
        results.append(super.PrintLongResults());
        results.append(String.format("Server Profit: %.2f",
                serverProfit));
        return results.toString();
    }
}