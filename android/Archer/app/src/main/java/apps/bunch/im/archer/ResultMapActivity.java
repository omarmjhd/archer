package apps.bunch.im.archer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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

    public static final double RADIUS_DISTANCE_RATIO = 0.15;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng mHit;
    private LatLng mTarget;
    private LatLng mSource;
    private Marker mAnimatedMarker;

    private TextView mDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_target);

        mDistance = (TextView) findViewById(R.id.distance);

        Intent intent = getIntent();
        mHit = new LatLng(intent.getDoubleExtra(HIT_LATITUDE, 0.0), intent.getDoubleExtra(HIT_LONGITUDE, 0.0));
        mSource = new LatLng(intent.getDoubleExtra(SOURCE_LATITUDE, 0.0), intent.getDoubleExtra(SOURCE_LONGITUDE, 0.0));
        mTarget = new LatLng(intent.getDoubleExtra(TARGET_LATITUDE, 0.0), intent.getDoubleExtra(TARGET_LONGITUDE, 0.0));

        setUpMapIfNeeded();
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

        mMap.addMarker(targetMarker);

        int fill = Color.argb(100, 216, 44, 44);

        double distance = distanceFromTarget();

        Log.i(LOG_TAG, "Distance hit->target: " + Double.toString(distance));

        mDistance.setText(String.format("Distance: %.3f meters", distance));

        if (distance < RADIUS_DISTANCE_RATIO * distanceBetweenSourceTarget()) { //abitrarily high to test
            fill = Color.argb(100, 44, 216, 44);
        }

        mMap.addCircle(new CircleOptions()
                .center(mTarget)
                .radius(RADIUS_DISTANCE_RATIO * distanceBetweenSourceTarget())
                .strokeColor(Color.BLACK)
                .fillColor(fill));


        /*
        double heading = SphericalUtil.computeHeading(mSource, mHit);

        MarkerOptions animatedMarkerOptions = new MarkerOptions()
                .position(mSource)
                .rotation((float) heading)
                .anchor((float) 0.5, 0)
                .title("Animated")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_arrow));
        */

        MarkerOptions animatedMarkerOptions = new MarkerOptions()
                .position(mSource).title("Animated")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mAnimatedMarker = mMap.addMarker(animatedMarkerOptions);

        mMap.addPolyline(new PolylineOptions().add(mSource, mHit)
                        .width(3)
                        .color(Color.RED)
        );

        /*


        LatLngInterpolator mLatLngInterpolator = new LatLngInterpolator.Spherical();
        MarkerAnimation.animateMarkerToGB(mAnimatedMarker, mHit, mLatLngInterpolator);

        CameraUpdate center = CameraUpdateFactory.newLatLng(mHit);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        */

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        new LatLngBounds.Builder().include(mTarget).include(mSource).build(), 256));

                LatLngInterpolator mLatLngInterpolator = new LatLngInterpolator.Spherical();
                MarkerAnimation.animateMarkerToGB(mAnimatedMarker, mHit, mLatLngInterpolator);

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        new LatLngBounds.Builder().include(mSource).include(mTarget).include(mHit).build(),
                        256
                ));

                mMap.setOnMapLoadedCallback(null);
            }
        });
    }

    private double distanceFromTarget() {
        return SphericalUtil.computeDistanceBetween(mTarget, mHit); //distanceTo returns meters
    }

    private double distanceBetweenSourceTarget() {
        return SphericalUtil.computeDistanceBetween(mSource, mTarget);
    }
}
