package edu.usc.infolab.ridesharing.pricing;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.ProfitCostSchedule;

import java.util.ArrayList;

public abstract class PricingModel {
  public abstract <R extends Request, D extends Driver<R>> ProfitCostSchedule GetProfitAndCost(
          D driver, ArrayList<GPSNode> schedule, Time start, boolean currentSchedule);
  
  public abstract double ComputeFinalFare(Request request, double detour);
  
  public abstract <R extends Request, D extends Driver<R>> double ComputeDriverIncome(
      D driver, int onBoardPassengers, double distance, double time);
  
  public abstract double DefaultFare(double distance, int time);
}
