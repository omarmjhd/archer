package apps.bunch.im.archer;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private LatLng mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_target);

        Intent intent = getIntent();
        Double hitLong = intent.getDoubleExtra(TARGET_LONGITUDE, 0.0);
        Double hitLat = intent.getDoubleExtra(TARGET_LATITUDE, 0.0);
        Double srcLong = intent.getDoubleExtra(SOURCE_LONGITUDE, 0.0);
        Double srcLat = intent.getDoubleExtra(SOURCE_LATITUDE, 0.0);
        mHit = new LatLng(hitLat, hitLong);
        mSource = new LatLng(srcLat, srcLong);

        setUpMapIfNeeded();
        //mMap.setMyLocationEnabled(true);

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
        Log.d(LOG_TAG, "Hit: " + mHit.toString());

        MarkerOptions hitMarker = new MarkerOptions().position(mHit).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        ).title("Hit");

        MarkerOptions srcMarker = new MarkerOptions().position(mSource).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        ).title("You");

        mMap.addMarker(hitMarker);
        mMap.addMarker(srcMarker);
    }
}
