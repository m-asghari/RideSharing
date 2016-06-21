package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;

public class TShareModelAuctionDriver extends AuctionDriver {
  private static final double INFLATE_PARAM = 0.5;

  /**
   * @param initialLoc
   * @param start
   * @param end
   */
  public TShareModelAuctionDriver(GPSPoint initialLoc, Time start, Time end) {
    super(initialLoc,start,end);
  }
    
  @Override
  protected void UpdateTravelledDistance(double length) {
    super.UpdateTravelledDistance(length);
    this.income += GetCurrentCostPerLength(length);
  }
  
  private double GetCurrentCostPerLength(double length) {
    return GetCurrentCostPerLength(length, this.onBoardRequests.size());
  }
  
  private double GetCurrentCostPerLength(double length, int onBoardPassengers) {
    if (onBoardPassengers == 0) {
      return 0;
    } else if (onBoardPassengers == 1) {
      return INCOME_PER_MILE * length;
    } else {
      return (1 + INFLATE_PARAM) * INCOME_PER_MILE * length;
    }
  }
  
}

