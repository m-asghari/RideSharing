package edu.usc.infolab.ridesharing.launcher;

import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.TimeDistancePair;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.algorithms.*;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionDriverType;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.ShortestPathDriver;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.AuctionInput;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.KTInput;
import edu.usc.infolab.ridesharing.kinetictree.KTDriver;
import edu.usc.infolab.ridesharing.kinetictree.KTRequest;
import edu.usc.infolab.shortestpath.ShortestPathUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class RideSharingLauncher {

    public static void main(String[] args) throws Exception {
        //ShortestPathUtil.InitializeNYCGraph();

        File requestsFile = new File("../Data/trips_2013_05_12.csv");
        File driversFile = new File("../Data/drivers_from_reqs_2013_05_12.csv");
        StringBuilder summaries = new StringBuilder();

        // Create Results Directory
        if (!Utils.resultsDir.exists()) {
            Utils.resultsDir.mkdir();
        }

        Utils.MaxWaitTime = 6;
        Utils.NumberOfVehicles = 500;
        Utils.MaxPassengers = 4;
        Utils.MaxDetourRelative = 0.5;
        for (double cheatingPortion : new double[]{0.0, 0.25, 0.5, 0.75, 1.}) {
            Utils.CheatingPortion = cheatingPortion;
            System.out.println(String.format("Starting: "
                            + "MaxWaitTime: %d, Number of Vehicles: %d, Max Passenger: %d, Max Detour(Relative): %.2f, Cheating Portion: %.2f\n",
                    Utils.MaxWaitTime, Utils.NumberOfVehicles, Utils.MaxPassengers, Utils.MaxDetourRelative, Utils.CheatingPortion));
            summaries.append(RunAlgorithms(requestsFile, driversFile));
        }

        /*System.out.println(String.format("Starting: "
                            + "MaxWaitTime: %d, Number of Vehicles: %d, Max Passenger: %d, Max Detour(Relative): %.2f\n",
                    Utils.MaxWaitTime, Utils.NumberOfVehicles, Utils.MaxPassengers, Utils.MaxDetourRelative));
        RunAlgorithms(requestsFile, driversFile);


        int[] maxWaitTimes = new int[]{3, 6, 9, 12, 15, 20};
        int[] numOfVehicles = new int[]{250, 500, 1000, 2000, 5000};
        int[] numOfPassengers = new int[]{2, 3, 4, 5, 6};
        double[] maxDetourRelatives = new double[]{0.25, 0.5, 0.75, 1.f};

        Utils.NumberOfVehicles = 500;
        Utils.MaxPassengers = 4;
        Utils.MaxDetourRelative = 0.5;
        Utils.CheatingPortion = 1.;
        for (int maxWaitTime : maxWaitTimes) {
            Utils.MaxWaitTime = maxWaitTime;
            System.out.println(String.format("Starting: "
                            + "MaxWaitTime: %d, Number of Vehicles: %d, Max Passenger: %d, Max Detour(Relative): %.2f, Cheating Portion: %.2f\n",
                    Utils.MaxWaitTime, Utils.NumberOfVehicles, Utils.MaxPassengers, Utils.MaxDetourRelative, Utils.CheatingPortion));
            summaries.append(RunAlgorithms(requestsFile, driversFile));
        }

        Utils.MaxWaitTime = 6;
        Utils.MaxPassengers = 4;
        Utils.MaxDetourRelative = 0.5;
        Utils.CheatingPortion = 1.;
        for (int numOfVehicle : numOfVehicles) {
            Utils.NumberOfVehicles = numOfVehicle;
            System.out.println(String.format("Starting: "
                            + "MaxWaitTime: %d, Number of Vehicles: %d, Max Passenger: %d, Max Detour(Relative): %.2f, Cheating Portion: %.2f\n",
                    Utils.MaxWaitTime, Utils.NumberOfVehicles, Utils.MaxPassengers, Utils.MaxDetourRelative, Utils.CheatingPortion));
            summaries.append(RunAlgorithms(requestsFile, driversFile));
        }

        Utils.MaxWaitTime = 6;
        Utils.NumberOfVehicles = 500;
        Utils.MaxDetourRelative = 0.5;
        Utils.CheatingPortion = 1.;
        for (int numOfPassenger : numOfPassengers) {
            Utils.MaxPassengers = numOfPassenger;
            System.out.println(String.format("Starting: "
                            + "MaxWaitTime: %d, Number of Vehicles: %d, Max Passenger: %d, Max Detour(Relative): %.2f, Cheating Portion: %.2f\n",
                    Utils.MaxWaitTime, Utils.NumberOfVehicles, Utils.MaxPassengers, Utils.MaxDetourRelative, Utils.CheatingPortion));
            summaries.append(RunAlgorithms(requestsFile, driversFile));
        }

        Utils.MaxWaitTime = 3;
        Utils.NumberOfVehicles = 500;
        Utils.MaxPassengers = 4;
        Utils.CheatingPortion = 1.;
        for (double maxDetourRelative : maxDetourRelatives) {
            Utils.MaxDetourRelative = maxDetourRelative;
            System.out.println(String.format("Starting: "
                            + "MaxWaitTime: %d, Number of Vehicles: %d, Max Passenger: %d, Max Detour(Relative): %.2f, Cheating Portion: %.2f\n",
                    Utils.MaxWaitTime, Utils.NumberOfVehicles, Utils.MaxPassengers, Utils.MaxDetourRelative, Utils.CheatingPortion));
            summaries.append(RunAlgorithms(requestsFile, driversFile));
        }*/

        String finalSummary = summaries.toString();
        System.out.println(finalSummary);
        try {
            File oFile = new File(String.format("Summaries_%s.csv",
                    Utils.FILE_SYSTEM_SDF.format(Calendar.getInstance().getTime())));
            FileWriter fw = new FileWriter(oFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(finalSummary);
            bw.close();
            fw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void RunShortestPath(File requestsFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        Calendar start = Calendar.getInstance();
        for (int i = 0; i < 1000; i++) {
            if (i % 50 == 0) {
                System.out.println(String.format("i = %d", i));
            }
            AuctionRequest req = auctionRequests.get(i);
            TimeDistancePair trip = req.source.DistanceInMilesAndMillis(req.destination);
        }
        Calendar end = Calendar.getInstance();
        System.out.println((int) (end.getTimeInMillis() - start.getTimeInMillis()));
    }

    @SuppressWarnings("unused")
    private static String RunAlgorithms(File requestsFile, File driversFile) {
        StringBuilder summaries = new StringBuilder();
        //summaries.append(RunSecondPriceAuction(requestsFile, driversFile));
        //summaries.append(RunFirstPriceAuction(requestsFile, driversFile));
        //summaries.append(RunSecondPriceAuction(requestsFile, driversFile));
        //summaries.append(RunSecondPriceAuctionWithReservedValue(requestsFile, driversFile));
        //summaries.append(RunNearestNeighbor(requestsFile, driversFile));
        //summaries.append(RunShortestPath(requestsFile, driversFile));
        //summaries.append(RunKineticTree(requestsFile, driversFile));
        summaries.append(RunAuction(requestsFile, driversFile));
        return summaries.toString();
    }

    private static String RunAuction(File requestsFile, File driversFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        ArrayList<AuctionDriver> auctionDrivers = AuctionInput.GenerateDrivers(driversFile, Utils.NumberOfVehicles, AuctionDriverType.EXHAUSTIVE_SEARCH);
        Time startTime = auctionRequests.get(0).requestTime.clone();

        AuctionAlgorithm<AuctionDriver> aucAlgo = new AuctionAlgorithm<AuctionDriver>(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                aucAlgo.GetName(),
                aucAlgo.Run(auctionRequests, auctionDrivers));
    }

    private static String RunFirstPriceAuction(File requestsFile, File driversFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        ArrayList<AuctionDriver> auctionDrivers = AuctionInput.GenerateDrivers(driversFile, Utils.NumberOfVehicles, AuctionDriverType.EXHAUSTIVE_SEARCH);
        Time startTime = auctionRequests.get(0).requestTime.clone();

        FirstPriceAuctionAlgorithm<AuctionDriver> fpaAlgo = new FirstPriceAuctionAlgorithm<AuctionDriver>(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                fpaAlgo.GetName(),
                fpaAlgo.Run(auctionRequests, auctionDrivers));
    }

    @SuppressWarnings("unused")
    private static String RunSecondPriceAuction(File requestsFile, File driversFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        ArrayList<AuctionDriver> auctionDrivers = AuctionInput.GenerateDrivers(driversFile, Utils.NumberOfVehicles, AuctionDriverType.EXHAUSTIVE_SEARCH);
        Time startTime = auctionRequests.get(0).requestTime.clone();

        SecondPriceAuctionAlgorithm<AuctionDriver> spaAlgo = new SecondPriceAuctionAlgorithm<AuctionDriver>(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                spaAlgo.GetName(),
                spaAlgo.Run(auctionRequests, auctionDrivers));
    }

    private static String RunSecondPriceAuctionWithReservedValue(File requestsFile, File driversFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        ArrayList<AuctionDriver> auctionDrivers = AuctionInput.GenerateDrivers(driversFile, Utils.NumberOfVehicles, AuctionDriverType.EXHAUSTIVE_SEARCH);
        Time startTime = auctionRequests.get(0).requestTime.clone();

        SecondPriceAuctionWithReservedValueAlgorithm<AuctionDriver> sparvAlgo =
                new SecondPriceAuctionWithReservedValueAlgorithm<AuctionDriver>(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                sparvAlgo.GetName(),
                sparvAlgo.Run(auctionRequests, auctionDrivers));

    }

    private static String RunNearestNeighbor(File requestsFile, File driversFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        ArrayList<AuctionDriver> auctionDrivers = AuctionInput.GenerateDrivers(driversFile, Utils.NumberOfVehicles, AuctionDriverType.EXHAUSTIVE_SEARCH);
        Time startTime = auctionRequests.get(0).requestTime.clone();

        NearestNeighborAlgorithm nnAlgo = new NearestNeighborAlgorithm(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                nnAlgo.GetName(),
                nnAlgo.Run(auctionRequests, auctionDrivers));
    }

    private static String RunKineticTree(File requestsFile, File driversFile) {
        ArrayList<Request> ktRequests = KTInput.GenerateRequests(requestsFile);
        ArrayList<KTDriver> ktDrivers = KTInput.GenerateDrivers(driversFile, Utils.NumberOfVehicles);
        Time startTime = ktRequests.get(0).requestTime.clone();

        KineticTreeAlgorithm ktAlgo = new KineticTreeAlgorithm(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                ktAlgo.GetName(),
                ktAlgo.Run(ktRequests, ktDrivers));
    }

    private static String RunShortestPath(File requestsFile, File driversFile) {
        ArrayList<AuctionRequest> auctionRequests = AuctionInput.GenerateRequests(requestsFile);
        ArrayList<ShortestPathDriver> shortestPathDrivers =
                AuctionInput.GenerateShortestPathDrivers(driversFile, Utils.NumberOfVehicles);
        Time startTime = auctionRequests.get(0).requestTime.clone();

        ShortestPathAlgorithm spAlgo = new ShortestPathAlgorithm(startTime, 1);
        return String.format("%d,%d,%d,%.2f,%s,%s\n",
                Utils.MaxWaitTime,
                Utils.NumberOfVehicles,
                Utils.MaxPassengers,
                Utils.MaxDetourRelative,
                spAlgo.GetName(),
                spAlgo.Run(auctionRequests, shortestPathDrivers));
    }

}
