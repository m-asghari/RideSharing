package edu.usc.infolab.ridesharing;

import java.io.File;
import java.util.Calendar;

public class Utils {
	public static Double Max_Double = 1. * Integer.MAX_VALUE;
	public static Double Min_Double = -1. * Integer.MAX_VALUE;
	public static Integer Max_Integer = Integer.MAX_VALUE;
	public static Integer Min_Integer = Integer.MIN_VALUE;
	
	public static int MaxWaitTime = 5;
	public static int NumberOfVehicles = 50;
	public static int MaxPassengers = 3;
	
	public static File resultsDir = new File(String.format(
	    "Results/Run_%s", Time.sdf.format(Calendar.getInstance().getTime())));
	
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
	
}
