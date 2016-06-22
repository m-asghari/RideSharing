package edu.usc.infolab.ridesharing.auction;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.ridesharing.Utils;

public class Bid implements Comparable<Bid>{
	public AuctionDriver driver;
	public ArrayList<GPSNode> schedule;
	public double profit;
	public double cost;
	public double distToPickup;
	
	public Bid(AuctionDriver driver, ArrayList<GPSNode> schedule, double profit, double cost) {
		this.driver = driver;
		this.schedule = new ArrayList<GPSNode>(schedule);
		this.profit = profit;
		this.cost = cost;
		this.distToPickup = Utils.Max_Double;
	}
	
	// with maximal cost, minus infinity cost
	public static Bid WorstBid() {
		return new Bid(null, new ArrayList<GPSNode>(), Utils.Min_Double, Utils.Max_Double);
	}
	
	@Override
	public int compareTo(Bid o) {
		if (this.profit == o.profit) {
			if (this.distToPickup > o.distToPickup) {
				return -1;
			} else if (this.distToPickup < o.distToPickup) {
				return 1;
			} else {
				return 0;
			}
		} else if (this.profit > o.profit) {
			return 1;
		} else {
			return -1;
		}
	}
}