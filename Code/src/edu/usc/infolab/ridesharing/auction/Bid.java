package edu.usc.infolab.ridesharing.auction;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.ridesharing.Utils;

public class Bid implements Comparable<Bid>{
	public AuctionDriver driver;
	public ArrayList<GPSNode> schedule;
	public double value;
	public double cost;
	public double distToPickup;
	
	public Bid(AuctionDriver driver, ArrayList<GPSNode> schedule, Double value, Double cost) {
		this.driver = driver;
		this.schedule = new ArrayList<GPSNode>(schedule);
		this.value = value;
		this.cost = cost;
		this.distToPickup = Utils.Max_Double;
	}
	
	// with maximal cost, minus infinity cost
	public static Bid WorstBid() {
		return new Bid(null, new ArrayList<GPSNode>(), Utils.Min_Double, Utils.Max_Double);
	}
	
	@Override
	public int compareTo(Bid o) {
		if (this.value == o.value) {
			if (this.distToPickup > o.distToPickup) {
				return -1;
			} else if (this.distToPickup < o.distToPickup) {
				return 1;
			} else {
				return 0;
			}
		} else if (this.value > o.value) {
			return 1;
		} else {
			return -1;
		}
	}
}