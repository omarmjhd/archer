package apps.bunch.im.archer;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class ArcherActivity extends Activity implements SensorEventListener {

    public static String LOG_TAG = "ArcherActivity";

    public enum State {
        WAITING, PULLING, FLYING
    }

    private TextView mStateView, mOrientation, mMyoAcceleration, mMyoOrientation, mGravity, mLinearAcceleration;
    private State mState = State.WAITING;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGeomagnetic;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    private Vector3 lastAcceleration;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            Log.d(LOG_TAG, "Myo connected.");
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            Log.d(LOG_TAG, "Myo disconnected.");
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            Log.d(LOG_TAG, getString(myo.getArm() == Arm.LEFT ?
                    R.string.arm_left : R.string.arm_right));
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            Log.d(LOG_TAG, "Myo unsynced.");
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            Log.d(LOG_TAG, "Myo unlocked.");
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            Log.d(LOG_TAG, "Myo locked");
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            //Log.d(LOG_TAG, "Acceleration: " + accel.toString());
            mMyoAcceleration.setText(String.format("Myo Accel (x,y,z) => %.5f, %.5f, %.5f", accel.x(), accel.y(), accel.z()));
            lastAcceleration = accel;
        }

        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            super.onGyroscopeData(myo, timestamp, gyro);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }
            Vector3 g = new Vector3(
                    2 * (rotation.x() * rotation.z() - rotation.w() * rotation.y()),
                    2 * (rotation.w() * rotation.x() + rotation.y() * rotation.z()),
                    rotation.w() * rotation.w() - rotation.x() * rotation.x() - rotation.y() * rotation.y() + rotation.z() * rotation.z()
            );
            /*
            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
            */
            mMyoOrientation.setText(String.format("Myo Orient (y,p,r) => %.2f, %.2f, %.2f", yaw, pitch, roll));
            mGravity.setText(String.format("Gravity (x,y,z) => %.4f, %.4f, %.4f", g.x(), g.y(), g.z()));
            setAccelerationDataNullGravity(g, lastAcceleration);
        }

        private void setAccelerationDataNullGravity(Vector3 g, Vector3 accel) {
            if (g != null && accel != null) {
                Vector3 linear = new Vector3(g);
                linear.subtract(accel);
                mLinearAcceleration.setText(String.format("LinAccel => (x,y,z): %.4f, %.4f, %.4f", linear.x(), linear.y(), linear.z()));
            }
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    Log.i(LOG_TAG, "Unknown pose.");
                    break;
                case REST:
                    break;
                case DOUBLE_TAP:
                    Log.i(LOG_TAG, "Double tap pose.");
                    if (mState == State.PULLING) {
                        setStateFlying();
                    }
                    break;
                case FIST:
                    Log.i(LOG_TAG, "Fist pose.");
                    if (mState == State.WAITING) {
                        setStatePulling();
                    }
                    break;
                case WAVE_IN:
                    Log.i(LOG_TAG, "Wave in.");
                    if (mState == State.PULLING) {
                        setStateFlying();
                    }
                    break;
                case WAVE_OUT:
                    Log.i(LOG_TAG, "Wave out.");
                    if (mState == State.PULLING) {
                        setStateFlying();
                    }
                    break;
                case FINGERS_SPREAD:
                    Log.i(LOG_TAG, "Fingers spread.");
                    if (mState == State.PULLING) {
                        setStateFlying();
                    }
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archer);

        mStateView = (TextView) findViewById(R.id.state);
        mOrientation = (TextView) findViewById(R.id.orientation);
        mMyoAcceleration = (TextView) findViewById(R.id.myo_acceleration);
        mMyoOrientation = (TextView) findViewById(R.id.myo_orientation);
        mGravity = (TextView) findViewById(R.id.gravity);
        mLinearAcceleration = (TextView) findViewById(R.id.linear_accel);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        if (mState == State.FLYING) {
            setStateWaiting();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values;
                break;
        }
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] foo = new float[3];
                SensorManager.getOrientation(R, foo);
                mOrientation.setText(String.format("Orientation (y,p,r) => %.2f, %.2f, %.2f", foo[0], foo[1], foo[2]));
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_archer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void setStateFlying() {
        Log.i(LOG_TAG, "Changing state to flying.");
        mState = State.FLYING;
        mStateView.setText(getString(R.string.state_flying));

        showTarget();
    }

    private void setStatePulling() {
        Log.i(LOG_TAG, "Changing state to pulling.");
        mState = State.PULLING;
        mStateView.setText(getString(R.string.state_pulling));
    }

    private void setStateWaiting() {
        Log.i(LOG_TAG, "Changing state to waiting.");
        mState = State.WAITING;
        mStateView.setText(getString(R.string.state_waiting));
    }

    private void showTarget() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.TARGET_LATITUDE, 33.948);
        intent.putExtra(MapsActivity.TARGET_LONGITUDE, -83.375);
        startActivity(intent);
    }
}
