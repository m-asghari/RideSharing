package edu.usc.infolab.ridesharing.launcher;

import java.io.File;
import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.algorithms.SecondPriceAuctionAlgorithm;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.AuctionInput;

public class RideSharingLauncher {

	public static void main(String[] args) {
		File requestsFile = new File("../Data/trips_2013_05_12.csv");
		ArrayList<AuctionRequest> requests = AuctionInput.GenerateRequests(requestsFile);
		//ArrayList<AuctionDriver> drivers = AuctionInput.GenerateDrivers(20);
		ArrayList<AuctionDriver> drivers = new ArrayList<AuctionDriver>();
		Time startTime = requests.get(0).requestTime.clone();
		
		SecondPriceAuctionAlgorithm spAlgo = new SecondPriceAuctionAlgorithm(startTime, 1);
		spAlgo.Run(requests, drivers);		
	}

}
