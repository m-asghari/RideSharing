package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.KTInput;
import edu.usc.infolab.ridesharing.kinetictree.KTDriver;
import edu.usc.infolab.ridesharing.kinetictree.KTRequest;

public class KineticTreeAlgorithm extends Algorithm<KTRequest, KTDriver> {

	public KineticTreeAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
	}

	@Override
	public Status ProcessRequest(KTRequest r, Time time) {
		ArrayList<KTDriver> potentialDrivers = GetPotentialDrivers(r);
		r.stats.potentialDrivers = potentialDrivers.size();
		
		HashMap<KTDriver, Double> insertCosts = new HashMap<KTDriver, Double>();
		Time start = new Time();
		for (KTDriver d : potentialDrivers) {
			insertCosts.put(d, d.InsertRequest(r));
		}
		
		KTDriver selectedDriver = null;
		double minCost = Utils.Max_Double;
		for (Entry<KTDriver, Double> e : insertCosts.entrySet()) {
			if (e.getValue() != null && e.getValue() < minCost) {
				minCost = e.getValue();
				selectedDriver = e.getKey();
			}
		}
		if (selectedDriver == null) {
			return Status.NOT_ASSIGNED;
		}
		selectedDriver.AddRequest(r, time);
		Time end = new Time();
		r.stats.schedulingTime = end.SubtractInMillis(start);
		return Status.ASSIGNED;
	}

	@Override
	protected KTDriver GetNewDriver() {
		return KTInput.GetNewDriver();
	}
	
	@Override
	public String GetName() {
		return "KTA";
	}

}
