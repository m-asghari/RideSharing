package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.kinetictree.KTNode;
import edu.usc.infolab.ridesharing.kinetictree.KTTrip;
import edu.usc.infolab.ridesharing.kinetictree.KineticTree;

import java.util.ArrayList;

/**
 * Created by Mohammad on 4/19/2017.
 *
 * Implementation of the AuctionDriver class using an dynamic programming to find the best schedule.
 */
public class DPAuctionDriver extends AuctionDriver {
    private KineticTree _ktree;

    public DPAuctionDriver(GPSPoint initialLoc, Time start, Time end) {
        super(initialLoc, start, end);
        _ktree = new KineticTree(this.loc);
    }

    public DPAuctionDriver(String[] args) {
        super(args);
        _ktree = new KineticTree(this.loc);
    }

    @Override
    protected ProfitCostSchedule LaunchFindBestPCS(AuctionRequest request, Time time) {
        KTTrip bestTrip = null;
        KTNode rootCopy = _ktree.InsertRequest(request);
        if (rootCopy != null) {
            return FindMostProfitableSchedule(new ArrayList<GPSNode>(), rootCopy, ProfitCostSchedule.WorstPCS(), time);
        }
        else
            return null;
    }

    private ProfitCostSchedule FindMostProfitableSchedule(ArrayList<GPSNode> schedule, KTNode node, ProfitCostSchedule bestPCS, Time time) {
        ProfitCostSchedule localPCS = ProfitCostSchedule.WorstPCS();
        ArrayList<GPSNode> localSchedule = new ArrayList<>(schedule);
        localSchedule.add(node);
        if (node.next.isEmpty()) {
            return Utils.PRICING_MODEL.GetProfitAndCost(this, localSchedule, time, false);
        }
        for (KTNode child : node.next) {
            ProfitCostSchedule childPCS = FindMostProfitableSchedule(localSchedule, child, bestPCS, time);
            if (childPCS.profit > localPCS.profit) {
                localPCS = childPCS;
            }
        }
        return localPCS;
    }


}
