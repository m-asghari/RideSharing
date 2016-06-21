package edu.usc.infolab.ridesharing.algorithms;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;
import edu.usc.infolab.ridesharing.auction.ShortestPathDriver;

import java.util.ArrayList;

public class ShortestPathAlgorithm extends AuctionAlgorithm<ShortestPathDriver> {
  /**
   * @param startTime
   * @param ati
   */
  public ShortestPathAlgorithm(Time startTime, int ati) {
    super(startTime, ati);
  }

  @Override
  public String GetName() {
    return "SP";
  }

  @Override
  public AuctionDriver SelectWinner(AuctionRequest request, ArrayList<Bid> bids) {
    if (bids.isEmpty()) return null;
    Bid smallestBid = null;
    double smallestCost = Utils.Max_Double;
    for (Bid bid : bids) {
      if (bid.cost < smallestCost) {
        smallestBid = bid;
        smallestCost = bid.cost;
      }
    }
    if (smallestBid == null || smallestBid.cost < 0) {
      return null;
    }
    ShortestPathDriver winner = (ShortestPathDriver)smallestBid.driver;
    return winner;
  }
}
