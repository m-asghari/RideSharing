package edu.usc.infolab.geo.semanticroute.demo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.semanticroute.dao.RouteBeanHelper;
import edu.usc.infolab.geo.util.ConnectionManager;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yaguang on 6/26/16.
 */
public class UpdateMainRoadName {

    public static void main(String[] args) throws SQLException {
        RouteBeanHelper routeBeanHelper = new RouteBeanHelper();
        List<RouteBean> routeBeanList = routeBeanHelper.getAllRoutes();
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql = "update routes set main_road=? where route_id=?;";
        for (RouteBean routeBean : routeBeanList) {
            int routeId = routeBean.getRoute_id();
            String jsonStr = routeBeanHelper.getRouteJsonStr(routeId);
            JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
            String mainRoad = jsonObject.get("summary").getAsString();
            run.update(conn, sql, mainRoad, routeId);
            System.out.print(routeId);
        }
        conn.close();
    }
}
