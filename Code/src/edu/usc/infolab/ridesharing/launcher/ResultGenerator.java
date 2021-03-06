package edu.usc.infolab.ridesharing.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map.Entry;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.FailureReason;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;

public class ResultGenerator {

	public static <R extends Request, D extends Driver<R>> String Summary(
			ArrayList<R> requests, ArrayList<D> drivers) {
		int totalRequests = requests.size();
		int assignedRequests = 0;
		int usedDrivers = 0;
		int mostProfitable = 0;
		int looseMoney = 0;
		double totalCollectedFare = 0;
		double totalCollectedOtherFare = 0;
		double totalPaidFare = 0;
		double totalPaidOtherFare = 0;
		double totalIncome = 0;
		double serverProfit = 0;
		double serverOtherProfit = 0;
		double totalTravelledDistance = 0;
		double avgResponseTime = 0;
		double avgProfitDiff = 0;
		for (D driver : drivers) {
			if (!driver.servicedRequests.isEmpty()) {
				usedDrivers++;
				totalCollectedFare += driver.collectedFare;
				totalCollectedOtherFare += driver.perDistanceIncome;
				totalIncome += driver.income;
				totalTravelledDistance += driver.travelledDistance;
			}
		}
		for (R request : requests) {
			if (request.stats.assigned == 1) {
				assignedRequests++;
				if (request.stats.mostProfitable == 1) {
					mostProfitable++;
				}
				if (request.stats.looseMoney == 1) {
				  looseMoney++;
				}
				totalPaidFare += request.finalFare;
				totalPaidOtherFare += request.perDistanceFare;
				avgProfitDiff += request.stats.profitDiff;
				avgResponseTime += request.stats.assignmentTime;
				avgResponseTime += request.stats.schedulingTime;
			}
			if (request instanceof AuctionRequest) {
				AuctionRequest r = (AuctionRequest) request;
				serverProfit += r.serverProfit;
				serverOtherProfit += r.serverProfit;
			}
		}
		return new StringBuilder()
				.append(String.format("Total Requests: %d\n", totalRequests))
				.append(String.format("Assigned Requests: %d\n",
						assignedRequests))
				.append(String.format("Most Profitable: %d\n",
						mostProfitable))
				.append(String.format("Loose Money: %d\n", looseMoney))
				.append(String.format("Avg Profit Diff: %.2f\n", avgProfitDiff))
				.append(String.format("Used Drivers: %d\n", usedDrivers))
				.append(String.format("Total Collected Fare: %.2f\n",
						totalCollectedFare))
				.append(String.format("Total Collected Other Fare: %.2f\n",
				    totalCollectedOtherFare))
				.append(String.format("Total Paid Fare: %.2f\n", totalPaidFare))
				.append(String.format("Total Paid Other Fare: %.2f\n", totalPaidOtherFare))
				.append(String.format("Total Income: %.2f\n", totalIncome))
				.append(String.format("Server Profit: %.2f and %.2f\n",
						totalCollectedFare - totalIncome, serverProfit))
				.append(String.format("Server Other Profit: %.2f and %.2f\n", 
				    0.2 * totalCollectedOtherFare, serverOtherProfit))
				.append(String.format("Total Travelled Distance: %.2f\n",
						totalTravelledDistance))
				.append(String.format("Response Time: %.2f\n", avgResponseTime
						/ assignedRequests)).toString();
	}

	public static <R extends Request, D extends Driver<R>> String ShortSummary(
			ArrayList<R> requests, ArrayList<D> drivers) {
		int totalRequests = requests.size();
		int assignedRequests = 0;
		int usedDrivers = 0;
		int mostProfitableDriver = 0;
		int looseMoney = 0;
		int unfairRiders = 0;
		double totalCollectedFare = 0;
		double totalIncome = 0;
		double avgResponseTime = 0;
		double avgProfitDiff = 0;
		for (D driver : drivers) {
			if (!driver.servicedRequests.isEmpty()) {
				usedDrivers++;
				totalCollectedFare += driver.collectedFare;
				totalIncome += driver.income;
			}
		}
		for (R request : requests) {
			if (request.stats.assigned == 1) {
				assignedRequests++;
				if (request.stats.mostProfitable == 1) {
					mostProfitableDriver++;
				}
				if (request.stats.looseMoney == 1) {
				  looseMoney++;
				}
				if (request.finalFare > request.defaultFare) {
				  unfairRiders++;
				}
				avgProfitDiff += request.stats.profitDiff;
				avgResponseTime += request.stats.assignmentTime;
				avgResponseTime += request.stats.schedulingTime;
			}
		}
		return String.format("%d,%d,%d,%d,%.2f,%d,%d,%.2f,%.2f,", totalRequests,
				assignedRequests, mostProfitableDriver, looseMoney,
				avgProfitDiff, unfairRiders, usedDrivers,
				totalCollectedFare - totalIncome, avgResponseTime
						/ assignedRequests);
	}

	public static <R extends Request, D extends Driver<R>> void SaveData(
			String name, ArrayList<R> requests, ArrayList<D> drivers) {
		try {
			String now = Utils.FILE_SYSTEM_SDF.format(Calendar.getInstance().getTime());
			File driversFile = new File(Utils.resultsDir, String.format(
					"%s_%d_%d_%d_%s_drivers.csv", name, Utils.MaxWaitTime,
					Utils.NumberOfVehicles, Utils.MaxPassengers, now));
			FileWriter fw = new FileWriter(driversFile);
			BufferedWriter bw = new BufferedWriter(fw);
			for (D d : drivers) {
				bw.write(d.PrintShortResults());
				bw.write("\n");
			}
			bw.close();
			fw.close();

			File requestsFile = new File(Utils.resultsDir, String.format(
					"%s_%d_%d_%d_%s_requests.csv", name, Utils.MaxWaitTime,
					Utils.NumberOfVehicles, Utils.MaxPassengers, now));
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

	public static Pair<ArrayList<AuctionRequest>, ArrayList<AuctionDriver>> ReadSavedData(
			String resultsDir, String now) {
		ArrayList<AuctionRequest> requests = new ArrayList<AuctionRequest>();
		ArrayList<AuctionDriver> drivers = new ArrayList<AuctionDriver>();
		try {
			File driversFile = new File(resultsDir, String.format(
					"drivers_%s.csv", now));
			FileReader fr = new FileReader(driversFile);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				drivers.add(new AuctionDriver(line.split(",")));
			}
			br.close();
			fr.close();

			File requestsFile = new File(resultsDir, String.format(
					"requests_%s.csv", now));
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

		return new Pair<ArrayList<AuctionRequest>, ArrayList<AuctionDriver>>(
				requests, drivers);
	}

	public static <R extends Request> String FindFailureReasons(
			ArrayList<R> requests) {
		StringBuilder stringBuilder = new StringBuilder();
		int counter = 0;
		for (Request request : requests) {
			if (request.stats.assigned == 0) {
				String reasons = String.format("Potential Drivers: %d, ",
						request.stats.potentialDrivers);
				for (Entry<FailureReason, Integer> entry : request.stats.failureReasons
						.entrySet()) {
					reasons += String.format("%s: %d, ", entry.getKey()
							.toString(), entry.getValue());
				}
				stringBuilder.append(reasons + "\n");
				if (counter++ > 200) {
					break;
				}
			}
		}
		return stringBuilder.toString();
	}

	public static void main(String[] args) {
	}
}
