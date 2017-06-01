package edu.usc.infolab.ridesharing.prediction;

/**
 * Created by Mohammad on 5/31/2017.
 */
public class Divergence {
    public static double KLD(double[] dist, double[] target) {
        double sum = 0.f;
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] == 0.f && target[i] == 0.f) continue;
            if (dist[i] == 0.f && target[i] != 0.f) dist[i] = 0.0000000001;
            if (target[i] == 0.f && dist[i] != 0.f) target[i] = 0.0000000001;
            double log = Math.log(dist[i]/target[i]);
            if (Double.isInfinite(log) || Double.isNaN(log)) {
                // Wait here
                System.out.println("Divergence-KLD: Wait Here.");
            }
            sum += (dist[i] * log);
        }
        return sum;
    }
}
