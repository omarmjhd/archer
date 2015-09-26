package apps.bunch.im.archer;

import com.thalmic.myo.Arm;

/**
 * Physics engine to handle arrow being shot, and return distance traveled by the arrow
 */
public class PhysicsEngine {

    private static final double earthRadius = 6371;
    private static final double metersInKilometer = 1000;
    public static final double mass = 500;

    /**
     *
     * @param force force that arrow is exerting
     * @return acceleration of the arrow
     */
    private static double acceleration(double force) {
        return force / mass;
    }

    /**
     *
     * @param acceleration acceleration of the arrow as it leaves the bow
     * @return velocity at which the arrow leaves the bow
     */
    private static double velocity(double acceleration) {
        return Math.sqrt((double) 2 * acceleration);
    }

    /**
     *
     * @param velocity velocity at which the arrow leaves the bow
     * @param radians angle at which the arrow leaves the bow
     * @return time that arrow is in the air
     */
    private static double time(double velocity, double radians) {
        return (velocity * Math.sin(radians)) / 9.8;
    }

    /**
     *
     * @param time time arrow is in the air
     * @param velocity velocity that has when leaving the bow
     * @param radians angle at which the arrow leaves the bow
     * @return
     */
    private static double distance(double time, double velocity, double radians) {
        return velocity * Math.cos(radians) * time;
    }

    //azimuth, pitch, roll,
    //this is for angle of launch (shoulder tilt)

    /**
     *
     * @param angles angles vector that contains azimuth, pitch and roll
     * @return the roll, the angle at which the arrow leaves the bow
     */
    private static double arrowAngle(float[] angles) {
        return angles[2];
    }

    //azimuth, pitch, roll,
    //this is for the direction that the arrow will be fired (long, latd type)

    /**
     *
     * @param angles angles vector that contains azimuth, pitch and roll
     * @return the direction, adjusted for the rotation due to phone orientation
     */
    private static double directionAngle(float[] angles, Arm arm) {

        //return arm == Arm.RIGHT ? angles[0] + (Math.PI / 2) : angles[0] - (Math.PI / 2);
        return angles[0];
    }

    /**
     *
     * @param force force of arrow's motion
     * @param orientation orientation array of the phone
     * @return distanceTraveled, the distance traveled by an arrow who's force, mass, and
     * angle (in radians) are given as inputs
     */
    private static double distanceTraveled(double force, float[] orientation) {

        double acceleration = acceleration(force);
        double velocity = velocity(acceleration);
        double arrowAngle = arrowAngle(orientation);
        double time = time(velocity, arrowAngle);

        return distance(time, velocity, arrowAngle);

    }

    /**
     *
     * Method follows this formula: Math.asin( Math.sin(φ1)*Math.cos(d/R) +
     * Math.cos(φ1)*Math.sin(d/R)*Math.cos(brng) );
     *
     * @param latitude latitude of current location
     * @param bearing bearing that the phone is pointed at
     * @param distance distance that the arrow will fly
     * @return latitude that the arrow will land at
     */
    private static double arrowLandingLatitude(double latitude, double bearing,
                                               double distance) {

        return Math.asin(Math.sin(Math.toRadians(latitude)) * Math.cos((distance / metersInKilometer) / earthRadius) +
                Math.cos(Math.toRadians(latitude)) * Math.sin((distance / metersInKilometer) / earthRadius) * Math.cos(Math.toRadians(bearing)));

    }

    /**
     *
     * Method follows this formula: λ1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(φ1),
     * Math.cos(d/R)-Math.sin(φ1)*Math.sin(φ2));
     *
     * @param latitudeInitial latitude of current location
     * @param longitude longitude of current position
     * @param bearing bearing that the phone is pointed at
     * @param distance distance that the arrow will fly
     * @return longitude that the arrow will land at
     */
    private static double arrowLandingLongitude(double latitudeInitial, double longitude,
                                               double bearing, double distance) {

        double latitudeFinal = arrowLandingLatitude(latitudeInitial, bearing, distance);

        return longitude + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin((distance / metersInKilometer) / earthRadius ) * Math.cos(Math.toRadians(latitudeInitial)),
                Math.cos((distance / metersInKilometer) / earthRadius) - Math.sin(Math.toRadians(latitudeInitial)) * Math.sin(Math.toRadians(latitudeFinal)));

    }

    /**
     *
     * Function that the ArcherActivity will call to get the longitude that the arrow will land at
     *
     * @param latitudeInitial initial lat of the arrow
     * @param longitudeInitial initial long of the arrow
     * @param force force that the arrow is launched at
     * @param orientation orientation that the arrow is fired at
     * @param arm arm that the Myo band is on
     * @return longitude where the arrow lands
     */
    public static double arrowFlightLongitude(double latitudeInitial, double longitudeInitial, double force, float[] orientation, Arm arm) {

        double distance = distanceTraveled(force, orientation);
        double bearing = directionAngle(orientation, arm);

        return arrowLandingLongitude(latitudeInitial,longitudeInitial, bearing, distance);

    }

    /**
     *
     * Function that the ArcherActivity will call to get the latitude that the arrow will land at
     *
     * @param latitudeInitial initial lat of the arrow
     * @param force force that the arrow is launched at
     * @param orientation orientation that the arrow is fired at
     * @param arm arm that the Myo band is on
     * @return latitude where the arrow lands
     */
    public static double arrowFlightLatitude(double latitudeInitial, double force, float[] orientation, Arm arm) {

        double distance = distanceTraveled(force, orientation);
        double bearing = directionAngle(orientation, arm);

        return arrowLandingLatitude(latitudeInitial, bearing, distance);

    }

}
