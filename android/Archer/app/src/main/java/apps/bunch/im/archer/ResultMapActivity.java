package apps.bunch.im.archer;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;


public class ResultMapActivity extends FragmentActivity {

    public static final String LOG_TAG = "ResultMapActivity";
    public static final String TARGET_LONGITUDE = "im.bunch.apps.archer.TARGET_LONGITUDE";
    public static final String TARGET_LATITUDE = "im.bunch.apps.archer.TARGET_LATITUDE";
    public static final String SOURCE_LONGITUDE = "im.bunch.apps.archer.SOURCE_LONGITUDE";
    public static final String SOURCE_LATITUDE = "im.bunch.apps.archer.SOURCE_LATITUDE";
    public static final String HIT_LONGITUDE = "im.bunch.apps.archer.HIT_LONGITUDE";
    public static final String HIT_LATITUDE = "im.bunch.apps.archer.HIT_LATITUDE";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng mHit;
    private LatLng mTarget;
    private LatLng mSource;
    private Circle mCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_target);

        Intent intent = getIntent();
        mHit = new LatLng(intent.getDoubleExtra(HIT_LATITUDE, 0.0), intent.getDoubleExtra(HIT_LONGITUDE, 0.0));
        mSource = new LatLng(intent.getDoubleExtra(SOURCE_LATITUDE, 0.0), intent.getDoubleExtra(SOURCE_LONGITUDE, 0.0));
        mTarget = new LatLng(intent.getDoubleExtra(TARGET_LATITUDE, 0.0), intent.getDoubleExtra(TARGET_LONGITUDE, 0.0));

        setUpMapIfNeeded();

        mCircle = mMap.addCircle(new CircleOptions()
                .center(mTarget)
                .radius(10000)
                .strokeColor(Color.BLACK)
                .fillColor(Color.argb(100, 215, 44, 44)));

        mMap.setMyLocationEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            Log.d(LOG_TAG, "mMap is null");
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        Log.d(LOG_TAG, "You: " + mSource.toString());
        Log.d(LOG_TAG, "Target: " + mTarget.toString());
        Log.d(LOG_TAG, "Hit: " + mHit.toString());

        mMap.setMyLocationEnabled(true);

        MarkerOptions targetMarker = new MarkerOptions().position(mTarget).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        ).title("Target");

        MarkerOptions hitMarker = new MarkerOptions().position(mHit).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        ).title("Hit");

        mMap.addMarker(hitMarker);
        mMap.addMarker(targetMarker);
        hitSensor();

        mMap.addPolyline(new PolylineOptions().add(mSource, mHit)
                        .width(3)
                        .color(Color.RED)
        );

    }

    private double distanceFromTarget() {
        return SphericalUtil.computeDistanceBetween(mTarget, mHit); //distanceTo returns meters
    }

    private double distanceBetweenSourceTarget() {
        return SphericalUtil.computeDistanceBetween(mSource, mTarget);
    }

    private void hitSensor() {

        if (distanceFromTarget() < 0.1 * distanceBetweenSourceTarget()) { //abitrarily high to test
            mCircle.setFillColor(Color.argb(100, 44, 215, 44));
        }

    }
}
