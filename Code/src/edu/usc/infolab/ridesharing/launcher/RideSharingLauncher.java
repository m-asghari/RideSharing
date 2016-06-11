package edu.usc.infolab.ridesharing.launcher;

import java.io.File;
import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.algorithms.KineticTreeAlgorithm;
import edu.usc.infolab.ridesharing.algorithms.NearestNeighborAlgorithm;
import edu.usc.infolab.ridesharing.algorithms.SecondPriceAuctionAlgorithm;
import edu.usc.infolab.ridesharing.algorithms.SecondPriceAuctionWithReservedValueAlgorithm;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.AuctionInput;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.KTInput;
import edu.usc.infolab.ridesharing.kinetictree.KTDriver;
import edu.usc.infolab.ridesharing.kinetictree.KTRequest;

public class RideSharingLauncher {

	public static void main(String[] args) {
		File requestsFile = new File("../Data/trips_2013_05_12.csv");
		
		ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
		ArrayList<AuctionDriver> auctionDrivers = new ArrayList<AuctionDriver>();
		Time startTime = auctionRequests.get(0).requestTime.clone();
		
		SecondPriceAuctionAlgorithm spaAlgo = new SecondPriceAuctionAlgorithm(startTime, 1);
		spaAlgo.Run(auctionRequests, auctionDrivers);

		auctionRequests = AuctionInput.GenerateRequests(requestsFile);
		auctionDrivers = new ArrayList<AuctionDriver>();
		startTime = auctionRequests.get(0).requestTime.clone();
		
		SecondPriceAuctionWithReservedValueAlgorithm sparvAlgo = new SecondPriceAuctionWithReservedValueAlgorithm(startTime, 1);
		sparvAlgo.Run(auctionRequests, auctionDrivers);
		
		auctionRequests = AuctionInput.GenerateRequests(requestsFile);
		auctionDrivers = new ArrayList<AuctionDriver>();
		startTime = auctionRequests.get(0).requestTime.clone();
		
		NearestNeighborAlgorithm nnAlgo = new NearestNeighborAlgorithm(startTime, 1);
		nnAlgo.Run(auctionRequests, auctionDrivers);
		
		ArrayList<KTRequest> ktRequests = KTInput.GenerateRequests(requestsFile);
		ArrayList<KTDriver> ktDrivers = new ArrayList<KTDriver>();
		startTime = ktRequests.get(0).requestTime.clone();
		
		KineticTreeAlgorithm ktAlgo = new KineticTreeAlgorithm(startTime, 1);
		ktAlgo.Run(ktRequests, ktDrivers);
	}

}
