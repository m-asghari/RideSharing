package edu.usc.infolab.ridesharing;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;

import javax.activity.InvalidActivityException;
import java.text.ParseException;
import java.util.HashMap;

public class Request implements Comparable<Request> {
	public static int reqCtr = 0;

	public class AssignmentStat {
		public AssignmentStat() {
			this.mostProfitable = 0;
			this.looseMoney = 0;
			this.profitDiff = 0;
			this.failureReasons = new HashMap<FailureReason, Integer>() {
				{
					put(FailureReason.CantCopy, 0);
					put(FailureReason.MaxWaitTime, 0);
					put(FailureReason.ServiceConstraint, 0);
				}
			};
		}

		private AssignmentStat(AssignmentStat other) {
			this.assigned = other.assigned;
			this.schedulingTime = other.schedulingTime;
			this.assignmentTime = other.assignmentTime;
			this.potentialDrivers = other.potentialDrivers;
			this.acceptableBids = other.acceptableBids;
			this.mostProfitable = other.mostProfitable;
			this.looseMoney = other.looseMoney;
			this.profitDiff = other.profitDiff;
			this.failureReasons = new HashMap<FailureReason, Integer>(
					other.failureReasons);
		}

		public void AddFailureReason(FailureReason reason) {
			failureReasons.put(reason, failureReasons.get(reason) + 1);
		}

		public int assigned;
		public int schedulingTime;
		public int assignmentTime;
		public int potentialDrivers;
		public int acceptableBids;
		public int mostProfitable;
		public int looseMoney;
		public double profitDiff;
		public HashMap<FailureReason, Integer> failureReasons;

		@Override
		public AssignmentStat clone() {
			return new AssignmentStat(this);
		}
	}

	public int id;
	public AssignmentStat stats;

	public GPSNode source;
	public GPSNode destination;

	public Time requestTime;
	public int maxWaitTime;
	public Time latestPickUpTime;

	// optTime and optDistance based on the shortest path between src and dest
	public double maxRelDetour;
	public int optTime;
	public double optDistance;

	public Time pickUpTime;
	public double pickUpDistance;

	public Time dropOffTime;
	public double dropOffDistance;

	public double detour;
	public int actualTime;
	public double actualDistance;

	public double defaultFare;
	public double finalFare;
	
	public double perDistanceFare;

	public Request(GPSPoint source, GPSPoint dest, Time requestTime,
                   int maxWaitTime) {
		this.id = reqCtr++;
		this.stats = new AssignmentStat();
		this.source = new GPSNode(source, Type.source, this);
		this.destination = new GPSNode(dest, Type.destination, this);
		this.requestTime = requestTime.clone();
		this.maxWaitTime = maxWaitTime;
		this.latestPickUpTime = requestTime.clone();
		this.latestPickUpTime.AddMinutes(maxWaitTime);
		TimeDistancePair shortestPath = ShortestPathInMilesAndMinutes(
				this.source.point, this.destination.point);
		this.optDistance = shortestPath.distance;
		this.optTime = shortestPath.time.intValue();
		this.maxRelDetour = Utils.MaxDetourRelative;
		this.pickUpTime = new Time();
		this.pickUpDistance = -1;
		this.dropOffTime = new Time();
		this.dropOffDistance = -1;
		this.detour = -1;
		this.actualTime = -1;
		this.actualDistance = -1;
		this.defaultFare = Utils.PRICING_MODEL.DefaultFare(optDistance, optTime);
		this.finalFare = -1;
		this.perDistanceFare = 0;
	}

	public Request(String[] args) {
		try {
			if (args.length < 22) {
				throw new InvalidActivityException(
						"Not enough arguments for Request.");
			}

			this.id = Integer.parseInt(args[0]);
			this.stats = new AssignmentStat();
			this.stats.assigned = Integer.parseInt(args[1]);
			this.stats.schedulingTime = Integer.parseInt(args[2]);
			this.stats.assignmentTime = Integer.parseInt(args[3]);
			this.stats.potentialDrivers = Integer.parseInt(args[4]);
			this.stats.acceptableBids = Integer.parseInt(args[5]);
			this.source = new GPSNode(); // args[6] source
			this.destination = new GPSNode(); // args[7] destination
			this.requestTime = new Time(Time.sdf.parse(args[8]));
			this.maxWaitTime = Integer.parseInt(args[9]);
			this.latestPickUpTime = new Time(Time.sdf.parse(args[10]));
			this.optTime = Integer.parseInt(args[11]);
			this.optDistance = Double.parseDouble(args[12]);
			this.maxRelDetour = Utils.MaxDetourRelative;
			this.pickUpTime = new Time(Time.sdf.parse(args[13]));
			this.pickUpDistance = Double.parseDouble(args[14]);
			this.dropOffTime = new Time(Time.sdf.parse(args[15]));
			this.dropOffDistance = Double.parseDouble(args[16]);
			this.detour = Double.parseDouble(args[17]);
			this.actualTime = Integer.parseInt(args[18]);
			this.actualDistance = Double.parseDouble(args[19]);
			this.defaultFare = Double.parseDouble(args[20]);
			this.finalFare = Double.parseDouble(args[21]);
			//this.otherFare
		} catch (ParseException pe) {
			pe.printStackTrace();
		} catch (InvalidActivityException iae) {
			iae.printStackTrace();
		}
	}

	protected Request(Request other) {
		this.id = other.id;
		this.stats = other.stats.clone();
		this.source = other.source.clone();
		this.destination = other.destination.clone();
		this.requestTime = other.requestTime.clone();
		this.maxWaitTime = other.maxWaitTime;
		this.latestPickUpTime = other.latestPickUpTime.clone();
		this.optTime = other.optTime;
		this.optDistance = other.optDistance;
		this.maxRelDetour = other.maxRelDetour;
		this.pickUpTime = other.pickUpTime.clone();
		this.pickUpDistance = other.pickUpDistance;
		this.dropOffTime = other.dropOffTime.clone();
		this.dropOffDistance = other.dropOffDistance;
		this.actualTime = other.actualTime;
		this.actualDistance = other.actualDistance;
		this.perDistanceFare = other.perDistanceFare;
	}

	public void PickUp(double distance, Time time) {
		this.pickUpTime.SetTime(time);
		this.pickUpDistance = distance;
	}

	public void DropOff(double distance, Time time) {
		this.dropOffTime.SetTime(time);
		this.dropOffDistance = distance;
		this.actualDistance = distance - this.pickUpDistance;
		this.detour = this.actualDistance - this.optDistance;
		this.actualTime = time.SubtractInMinutes(this.pickUpTime);
		this.finalFare = this.profile(this.detour) * this.defaultFare;
	}

	public Double profile(double detour) {
		if (Utils.IsAcceptableDetour(detour, this.optDistance)) {
			return 1. - (0.00025 * Math.pow(detour, 2));
		}
		return 0.1;
	}

	/*
	 * Depending on the type of P it can either be the Euclidean distance or the
	 * shortest path on road network.
	 */
	private TimeDistancePair ShortestPathInMilesAndMinutes(GPSPoint s,
                                                               GPSPoint d) {
		TimeDistancePair milesAndMillis = s.DistanceInMilesAndMillis(d);
		return new TimeDistancePair(milesAndMillis.distance,
				milesAndMillis.time / Time.MillisInMinute);
	}

	@Override
	public Request clone() {
		return new Request(this);
	}

	@Override
	public int compareTo(Request o) {
		return requestTime.compareTo(o.requestTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Request) {
			Request otherRequest = (Request) obj;
			return (this.id == otherRequest.id);
		}
		return false;
	}

	// id,stats.assigned,stats.scheduleTime,stats.assignTime,stats.potentialDrivers,stats.acceptableBids,source,destination,rTime,maxW,latestPickUpTime,optTime,OptDist,pickUpTime,pickUpDistance,dropOffTime,dropOffDistance,detour,actualTime,actualDistance,default fare, final fare, other fare
	public String PrintShortResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("%d,"
		    + "%d,%d,%d,%d,%d,"
		    + "%s,%s,"
			+ "%s,%d,%s," 
		    + "%d,%.2f," 
			+ "%s,%.2f," 
		    + "%s,%.2f,"
			+ "%.2f,%d,%.2f," 
		    + "%.2f,%.2f,%.2f,", 
		    id, 
		    stats.assigned,	stats.schedulingTime, stats.assignmentTime, stats.potentialDrivers, stats.acceptableBids,
			source.toString(), destination.toString(),
			Time.sdf.format(requestTime.GetTime()), maxWaitTime, Time.sdf.format(latestPickUpTime.GetTime()),
			optTime, optDistance, 
			Time.sdf.format(pickUpTime.GetTime()), pickUpDistance, 
			Time.sdf.format(dropOffTime.GetTime()),	dropOffDistance, 
			detour, actualTime, actualDistance,
			defaultFare, finalFare, perDistanceFare));
		return results.toString();
	}

	public String PrintLongResults() {
		StringBuilder results = new StringBuilder();
		results.append(String
				.format("id:%d\n"
						+ "Assigned: %d, Scheduling Time: %d, Assignment Time: %d, #Potential Drivers: %d, #Acceptable Bids: %d\n"
						+ "Source: %s, Destination: %s\n"
						+ "Request Time: %s, Max Wait Time: %d, Latest PickUp Time: %s\n"
						+ "Opt Time: %d, Opt Distance: %.2f\n"
						+ "PickUp Time: %s, PickUp Distance: %.2f\n"
						+ "DropOff Time: %s, DropOff Distance: %.f\n"
						+ "Detour: %.2f, Actual Time: %d, Actual Distance%.2f\n"
						+ "Default Fare: %.2f, Final Fare: %.2f\n", id,
						stats.assigned, stats.schedulingTime,
						stats.assignmentTime, stats.potentialDrivers,
						stats.acceptableBids, source.toString(),
						destination.toString(), requestTime.toString(),
						maxWaitTime, latestPickUpTime.toString(), optTime,
						optDistance, pickUpTime.toString(), pickUpDistance,
						dropOffTime.toString(), dropOffDistance, detour,
						actualTime, actualDistance, defaultFare, finalFare));
		return results.toString();
	}

	@Override
	public String toString() {
		return String.format("%d\n", id);
	}
}
