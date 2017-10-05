package edu.usc.infolab.ridesharing.dynamicpricing;

import edu.usc.infolab.ridesharing.Time;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohammad on 10/2/2017.
 */
public class TransitionModel {
    private HashMap<Integer, HashMap<Integer, List<Integer>>> transition_matrix;
    private int start_hour;
    private int interval_length;

    public TransitionModel() {
        transition_matrix = new HashMap<>();
        start_hour = 6;
        interval_length = 3;
    }

    public TransitionModel(String requests_file, int start_hour, int interval_length) {
        this.start_hour = start_hour;
        this.interval_length = interval_length;

        try {
            FileReader fr = new FileReader(requests_file);
            BufferedReader br = new BufferedReader(fr);

            String line = "";
            while ((line = br.readLine()) != null) {
                //TODO(masghari): Populate transition_matrix

            }

            br.close();
            fr.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public int getDemand(int source, int destination, Time time) {
        int hour = time.Get(Calendar.HOUR_OF_DAY);
        int interval = (hour - start_hour) / interval_length;
        return transition_matrix.get(interval).get(source).get(destination);
    }

}
