package apps.bunch.im.archer;


/**
 * Physics engine to handle arrow being shot, and return distance traveled by the arrow
 */
public class PhysicsEngine {

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

    //yaw, pitch, roll,
    private static double launchAngleFormat(float[] angles) {
        return angles[2];
    }

    /**
     *
     * @param force force of arrow's motion
     * @param mass mass of arrow
     * @param distanceDrawn distance that the arrow is drawn
     * @param radians angle that the bow is held at (in radians)
     * @return distanceTraveled: the distance traveled by an arrow who's force, mass, distanceDrawn and
     * angle (in radias) are given as inputs
     */
    public static double distanceTraveled(double force, double mass, double distanceDrawn, double radians) {

        return distance(time(velocity(acceleration(force, mass), distanceDrawn), radians),
                velocity(acceleration(force, mass), distanceDrawn), radians);

    }

    //yaw, pitch, roll,
    private static double azimuthFormat(float[] angles) {
        return angles[0];
    }

    //x = long y = lat
    private static double arrowTravelX(double distance, double longitude, double azimuth) {

    }

    private static double arrowTravelY(double distance, double latitude, double azimuth) {

    }



}
