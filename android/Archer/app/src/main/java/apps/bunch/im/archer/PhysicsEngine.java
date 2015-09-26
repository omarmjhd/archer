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
    public static double distanceTraveled(double force, double mass, double distanceDrawn, float[] orientation) {

        double acceleration = acceleration(force, mass);
        double velocity = velocity(acceleration, distanceDrawn);
        double arrowAngle = arrowAngle(orientation);
        double time = time(velocity, arrowAngle);

        return distance(time, velocity, arrowAngle);

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
