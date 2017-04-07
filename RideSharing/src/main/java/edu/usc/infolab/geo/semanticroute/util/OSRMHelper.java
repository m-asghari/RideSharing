package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.util.Utility;
import se.walkercrou.places.DefaultRequestHandler;
import se.walkercrou.places.RequestHandler;

/**
 * Created by yaguang on 4/9/16.
 */
public class OSRMHelper {
    private String baseUrl = Utility.getProperty("osrm_base_url");
    private RequestHandler requestHandler;

    public OSRMHelper() {
        requestHandler = new DefaultRequestHandler();
    }
}
