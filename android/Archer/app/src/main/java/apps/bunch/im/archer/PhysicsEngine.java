package apps.bunch.im.archer;


/**
 * Physics engine to handle arrow being shot, and return distance traveled by the arrow
 */
public class PhysicsEngine {

    public static final double earthRadius = 6371;
    public static final double metersInKilometer = 1000;

    private static double acceleration(double force, double mass) {
        return force / mass;
    }

    private static double velocity(double acceleration, double distanceDrawn) {
        return Math.sqrt((double) 2 * acceleration * distanceDrawn);
    }

    private static double time(double velocity, double radians) {
        return (velocity * Math.sin(radians)) / 9.8;
    }

    private static double distance(double time, double velocity, double radians) {
        return velocity * Math.cos(radians) * time;
    }

    //azimuth, pitch, roll,
    //this is for angle of launch (shoulder tilt)
    private static double arrowAngle(float[] angles) {
        return angles[2];
    }

    //azimuth, pitch, roll,
    //this is for the direction that the arrow will be fired (long, latd type)
    private static double directionAngle(float[] angles) {
        return angles[0] + 1.57;
    }

    /**
     *
     * @param force force of arrow's motion
     * @param mass mass of arrow
     * @param distanceDrawn distance that the arrow is drawn
     * @param orientation orientation array of the phone
     * @return distanceTraveled: the distance traveled by an arrow who's force, mass, distanceDrawn and
     * angle (in radias) are given as inputs
     */
    public static double distanceTraveled(double force, double mass, double distanceDrawn,
                                          float[] orientation) {

        double acceleration = acceleration(force, mass);
        double velocity = velocity(acceleration, distanceDrawn);
        double arrowAngle = arrowAngle(orientation);
        double time = time(velocity, arrowAngle);

        return distance(time, velocity, arrowAngle);

    }

    private static double arrowLandingLatitude(double latitude, double bearing,
                                               double distance) {
        /*Math.asin( Math.sin(φ1)*Math.cos(d/R) +
                Math.cos(φ1)*Math.sin(d/R)*Math.cos(brng) );*/

        return Math.asin(Math.sin(Math.toRadians(latitude)) * Math.cos((distance / metersInKilometer) / earthRadius) +
                Math.cos(Math.toRadians(latitude)) * Math.sin((distance / metersInKilometer) / earthRadius) * Math.cos(Math.toRadians(bearing)));

    }

    private static double arrowLandingLongitude(double latitudeInitial, double longitude,
                                               double bearing, double distance) {
        /*λ1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(φ1),
                Math.cos(d/R)-Math.sin(φ1)*Math.sin(φ2));*/

        double latitudeFinal = arrowLandingLatitude(latitudeInitial, bearing, distance);

        return longitude + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin((distance / metersInKilometer) / earthRadius ) * Math.cos(Math.toRadians(latitudeInitial)),
                Math.cos((distance / metersInKilometer) / earthRadius) - Math.sin(Math.toRadians(latitudeInitial)) * Math.sin(Math.toRadians(latitudeFinal)));

    }

    public static double arrowFlightLongitude(double latitudeInitial, double longitudeInitial, double force, double mass, double distanceDrawn, float[] orientation) {

        double distance = distanceTraveled(force, mass, distanceDrawn, orientation);
        double bearing = directionAngle(orientation);

        return arrowLandingLongitude(latitudeInitial,longitudeInitial, bearing, distance);

    }

    public static double arrowFlightLatitude(double latitudeInitial, double force, double mass, double distanceDrawn, float[] orientation) {

        double distance = distanceTraveled(force, mass, distanceDrawn, orientation);
        double bearing = directionAngle(orientation);

        return arrowLandingLatitude(latitudeInitial, bearing, distance);

    }

}
