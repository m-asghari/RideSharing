package edu.usc.infolab.ridesharing.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;

public class ResultGenerator {
	
	public static <R extends Request, D extends Driver<R>> String ShortSummary(ArrayList<R> requests, ArrayList<D> drivers) {
		StringBuilder summary = new StringBuilder();
		HashMap<Integer, Integer> serviceCountMap = new HashMap<Integer, Integer>();
		for (D d : drivers) {
			Integer key = d.servicedRequests.size();
			Integer newValue = 1;
			if (serviceCountMap.containsKey(key)) {
				newValue += serviceCountMap.get(key);
			}
			serviceCountMap.put(key, newValue);
		}
		for (Entry<Integer, Integer> e : serviceCountMap.entrySet()) {
			summary.append(String.format("Serviced: %d,  Count: %d\n", e.getKey(), e.getValue()));
		}
		return summary.toString();
	}
	
	public static <R extends Request, D extends Driver<R>> void SaveData(String name, ArrayList<R> requests, ArrayList<D> drivers) {
		try {
			String now = Time.sdf.format(Calendar.getInstance().getTime());
			File driversFile = new File(GetResultsDirectory(), String.format("%s_%s_drivers.csv", name, now));
			FileWriter fw = new FileWriter(driversFile);
			BufferedWriter bw = new BufferedWriter(fw);
			for (D d : drivers) {
				bw.write(d.PrintShortResults());
				bw.write("\n");
			}
			bw.close();
			fw.close();
			
			File requestsFile = new File(GetResultsDirectory(), String.format("%s_%s_requests.csv", name, now));
			fw = new FileWriter(requestsFile);
			bw = new BufferedWriter(fw);
			for (R r : requests) {
				bw.write(r.PrintShortResults());
				bw.write("\n");
			}
			bw.close();
			fw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static Pair<ArrayList<AuctionRequest>, ArrayList<AuctionDriver>> ReadSavedData(String now) {
		ArrayList<AuctionRequest> requests = new ArrayList<AuctionRequest>();
		ArrayList<AuctionDriver> drivers = new ArrayList<AuctionDriver>();
		try {
			File driversFile = new File(GetResultsDirectory(), String.format("drivers_%s.csv", now));
			FileReader fr = new FileReader(driversFile);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				drivers.add(new AuctionDriver(line.split(",")));
			}
			br.close();
			fr.close();
			
			File requestsFile = new File(GetResultsDirectory(), String.format("requests_%s.csv", now));
			fr = new FileReader(requestsFile);
			br = new BufferedReader(fr);
			line = "";
			while ((line = br.readLine()) != null) {
				requests.add(new AuctionRequest(line.split(",")));
			}
			br.close();
			fr.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		return new Pair<ArrayList<AuctionRequest>, ArrayList<AuctionDriver>>(requests, drivers);
	}
	
	private static File GetResultsDirectory() {
		File resultsDir = new File("Results");
		if (!resultsDir.exists()) {
			resultsDir.mkdir();
		}
		return resultsDir;
	}

	public static void main(String[] args) {
		GetResultsDirectory();
	}

}
