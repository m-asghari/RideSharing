package edu.usc.infolab.ridesharing.auction;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.ridesharing.Utils;

public class ProfitCostSchedule implements Comparable<ProfitCostSchedule> {
  public double profit;
  public double cost;
  public ArrayList<GPSNode> schedule;

  public ProfitCostSchedule(double profit, double cost, ArrayList<GPSNode> schedule) {
    this.profit = profit;
    this.cost = cost;
    this.schedule = new ArrayList<GPSNode>(schedule);
  }

  public static ProfitCostSchedule WorstPCS() {
    return new ProfitCostSchedule(Utils.Min_Double, Utils.Max_Double, new ArrayList<GPSNode>());
  }

  @Override
  public int compareTo(ProfitCostSchedule o) {
    if (this.profit == o.profit) {
      if (this.schedule.size() == o.schedule.size()) {
        return 0;
      } else if (this.schedule.size() > o.schedule.size()) {
        return 1;
      } else {
        return -1;
      }
    } else if (this.profit > o.profit) {
      return 1;
    } else {
      return -1;
    }
  }
}
