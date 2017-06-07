package edu.usc.infolab.ridesharing;

import edu.usc.infolab.ridesharing.pricing.DetourCompensatingModel;
import edu.usc.infolab.ridesharing.pricing.PerDistanceModel;
import edu.usc.infolab.ridesharing.pricing.PricingModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static Double Max_Double = 1. * Integer.MAX_VALUE;
    public static Double Min_Double = -1. * Integer.MAX_VALUE;
    public static Integer Max_Integer = Integer.MAX_VALUE;
    public static Integer Min_Integer = Integer.MIN_VALUE;

    public static int MaxWaitTime = 5;
    public static int NumberOfVehicles = 50;
    public static int MaxPassengers = 3;

    public static final SimpleDateFormat FILE_SYSTEM_SDF = new SimpleDateFormat(
            "yyyyMMdd_HHmmss");
    public static File resultsDir = new File(
            new File("Results"),
            String.format("Run_%s", FILE_SYSTEM_SDF.format(Calendar.getInstance().getTime())).replace(" ", "_").replace(":", "-"));

    public enum DistanceType {
        Euclidean,
        Network
    }

    public static DistanceType distanceType = DistanceType.Euclidean;
    public static long spComputations = 0;

    public enum DetourConstraint {
        FIXED,
        RELATIVE
    }

    public static DetourConstraint detourConstraintMethod = DetourConstraint.RELATIVE;
    public static double MaxDetourFixed = 15;
    public static double MaxDetourRelative = 0.5;

    public static boolean IsAcceptableDetour(double detour, double optDistance) {
        switch (detourConstraintMethod) {
            case FIXED:
                if (detour < Utils.MaxDetourFixed)
                    return true;
                break;
            case RELATIVE:
                if (detour < (Utils.MaxDetourRelative * optDistance))
                    return true;
                break;
            default:
                break;
        }
        return false;
    }

    //public static PricingModel PRICING_MODEL = PerDistanceModel.getInstance();
    public static PricingModel PRICING_MODEL = DetourCompensatingModel.getInstance();


    public static double CheatingPortion = 1.;
    public static double GetWaitTimeFactor(double waitTime) {
		//return ((waitTime/3.f)-1)/10.f;
        return 0.;
    }
}
