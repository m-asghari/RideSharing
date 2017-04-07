package edu.usc.infolab.geo.semanticroute.dao;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.util.ConnectionManager;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.postgis.LineString;
import org.postgis.Point;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RouteBeanHelper {
    public int addRoute(RouteBean route) throws SQLException {
        List<RouteBean> routeList = new ArrayList<RouteBean>();
        routeList.add(route);
        return addRoutes(routeList);
    }

    public int addRouteReturnId(RouteBean route) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql =
                "insert into routes(node_sequence, from_place_id, to_place_id, path, json_str, main_road) values (?,?,?,?,?,?)";
        int routeId = run.insert(conn, sql, new ScalarHandler<Integer>(1),
                route.getNode_sequence(),
                route.getFrom_place_id(), route.getTo_place_id(), route.getPath(),
                route.getJson_str(), route.getMain_road());
        conn.close();
        return routeId;
    }

    public int addRoutes(List<RouteBean> routeList) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql =
                "insert into routes(node_sequence, from_place_id, to_place_id, path, json_str, main_road) values (?,?,?,?,?,?)";
        Object[][] params = new Object[routeList.size()][];
        for (int i = 0; i < routeList.size(); ++i) {
            RouteBean route = routeList.get(i);
            params[i] = new Object[]{route.getNode_sequence(), route.getFrom_place_id(),
                    route.getTo_place_id(), route.getPath(), route.getJson_str(),
                    route.getMain_road()};
        }
        int[] affectedRows = run.batch(conn, sql, params);
        int result = 0;
        for (int num : affectedRows) {
            result += num;
        }
        conn.close();
        return result;
    }

    public int setNodeSequence(int routeId, String nodeSequence) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql = "update routes set node_sequence=? where route_id=?;";
        int affectedRows = run.update(conn, sql, nodeSequence, routeId);
        conn.close();
        return affectedRows;
    }

    public RouteBean getRoute(int routeId) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        String sql =
                "select route_id, node_sequence, from_place_id, to_place_id, path, main_road from routes where route_id = ?;";
        QueryRunner run = new QueryRunner();
        RouteBean result = run.query(conn, sql, new BeanHandler<RouteBean>(RouteBean.class), routeId);
        conn.close();
        return result;
    }

    public String getRouteJsonStr(int routeId) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        String sql = "select json_str from routes where route_id = ?;";
        QueryRunner run = new QueryRunner();
        String result = run.query(conn, sql, new ScalarHandler<String>(), routeId);
        conn.close();
        return result;
    }

    public int deleteRoute(int routeId) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql = "delete from routes where route_id=?;";
        int affectedRows = run.update(conn, sql, routeId);
        conn.close();
        return affectedRows;
    }

    private List<Coordinate> getTestTrajectoryFromDB() {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        int routeId = 86;
        final int POINT_NUM = 10000;
        RouteBeanHelper routeHelper = new RouteBeanHelper();
        RouteBean route = null;
        try {
            route = routeHelper.getRoute(routeId);
            LineString path = (LineString) route.getPath().getGeometry();
            Point[] points = path.getPoints();
            for (int i = 0; i < Math.min(points.length, POINT_NUM); ++i) {
                Point point = points[i];
                Coordinate coord = new Coordinate(point.getX(), point.getY());
                coords.add(coord);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coords;
    }

    public List<Coordinate> getCoordinates(RouteBean route) {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        LineString path = (LineString) route.getPath().getGeometry();
        Point[] points = path.getPoints();
        for (int i = 0; i < points.length; ++i) {
            Point point = points[i];
            Coordinate coord = new Coordinate(point.getX(), point.getY());
            coords.add(coord);
        }
        return coords;
    }

    public List<RouteBean> getAllRoutes() throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        String sql = "select route_id, node_sequence, from_place_id, to_place_id, path, main_road from routes;";
        QueryRunner run = new QueryRunner();
        List<RouteBean> result = run.query(conn, sql, new BeanListHandler<RouteBean>(RouteBean.class));
        conn.close();
        return result;
    }

    public List<RouteBean> getUserRoutes(int userId) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        String sql = "select routes.route_id, node_sequence, from_place_id, user_route.from_place, " +
                "to_place_id, user_route.to_place, path, main_road, score from routes, user_route " +
                "where routes.route_id=user_route.route_id and user_id=? order by score desc;";
        QueryRunner run = new QueryRunner();
        List<RouteBean> result = run.query(conn, sql, new BeanListHandler<>(RouteBean.class), userId);
        conn.close();
        return result;
    }
}
