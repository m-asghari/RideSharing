package edu.usc.infolab.geo.semanticroute.dao;

import edu.usc.infolab.geo.util.ConnectionManager;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PlaceHelper {
    private static Logger logger = Logger.getLogger(PlaceHelper.class.getName());

    public int addPlace(PlaceBean place) throws SQLException {
        List<PlaceBean> placeList = new ArrayList<PlaceBean>();
        placeList.add(place);
        return addPlaces(placeList);
    }

    public int addPlaces(List<PlaceBean> placeList) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql =
                "insert into places(place_id, address, types, score, name, lat, lng, location) values  (?,?,?,?,?,?,?,?);";
        Object[][] params = new Object[placeList.size()][];
        for (int i = 0; i < placeList.size(); ++i) {
            PlaceBean place = placeList.get(i);
            Geometry point = new Point(place.getLng(), place.getLat());
            point.setSrid(4326);
            PGgeometry location = new PGgeometry(point);
            params[i] = new Object[]{place.getPlace_id(), place.getAddress(), place.getTypes(), place.getScore(), place.getName(),
                    place.getLat(), place.getLng(), location};
        }
        int[] affectedRows = run.batch(conn, sql, params);
        int result = 0;
        for (int num : affectedRows) {
            result += num;
        }
        conn.close();
        return result;
    }

    public PlaceBean getPlace(String placeId) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        String sql = "select * from places where place_id = ?;";
        QueryRunner run = new QueryRunner();
        PlaceBean result = run.query(conn, sql, new BeanHandler<PlaceBean>(PlaceBean.class), placeId);
        conn.close();
        return result;
    }

    public int deletePlace(String placeId) throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        QueryRunner run = new QueryRunner();
        String sql = "delete from places where place_id=?;";
        int affectedRows = run.update(conn, sql, placeId);
        conn.close();
        return affectedRows;
    }

    public List<String> getPlaceIds() throws SQLException {
        Connection conn = ConnectionManager.getDefaultConnection();
        String sql = "select place_id from places;";
        QueryRunner run = new QueryRunner();
        List<String> result = run.query(conn, sql, new ColumnListHandler<>(1));
        conn.close();
        return result;
    }
}
