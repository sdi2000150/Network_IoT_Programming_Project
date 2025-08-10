package misc;
import data_util.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

/**Utility class for geospatial calculations*/
public class GeoCalculator {
    private static final Logger logger = LoggerFactory.getLogger(GeoCalculator.class);
    /**Calculates centroid of points and returns it in degrees
     * @param points vector of Locations (coordinates given in degrees)*/
    public static Location computeCenter(Vector<Location> points) {
        logger.debug("[GeoCalculator] finding center of {} points", points.size());
        if(points.size() == 1) {
            return points.elementAt(0);
        }
        Point3D center = new Point3D(0, 0, 0);
        // calculate centroid: convert coordinates of each point to cartesian and compute mean of each coordinate
        for (Location point : points) {
            Point3D p = new Point3D(point.getLongitude(), point.getLatitude());
            center.x += p.x;
            center.y += p.y;
            center.z += p.z;
        }
        center.x /= points.size();
        center.y /= points.size();
        center.z /= points.size();

        // Convert centroid coordinates to degrees and return location object
        double longitudeRad = Math.atan2(center.y, center.x);
        double latitudeRad =  Math.atan2(center.z, Math.sqrt(center.x * center.x + center.y * center.y));
        float longitudeDeg = (float) Math.toDegrees(longitudeRad);
        float latitudeDeg = (float) Math.toDegrees(latitudeRad);

        return new Location(longitudeDeg, latitudeDeg);
    }

    /**Computes the distance between two points (given in degrees)
     * @return their distance in kilometers*/
    public static float computeDistanceKm(Location point1, Location point2) {
        if (point1.equals(point2)) {
            return 0;
        }
        else {
            float lon1 = point1.getLongitude(); float lon2 = point2.getLongitude();
            float lat1 = point1.getLatitude(); float lat2 = point2.getLatitude();
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;

            return (float) dist;
        }
    }


    private static class Point3D {
        public double x;
        public double y;
        public double z;

        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point3D(float longitude, float latitude) {
            double longitudeRad = Math.toRadians(longitude);
            double latitudeRad =  Math.toRadians(latitude);
            this.x = Math.cos(longitudeRad) * Math.cos(latitudeRad);
            this.y = Math.cos(latitudeRad) * Math.sin(longitudeRad);
            this.z = Math.sin(latitudeRad);
        }
    }


}
