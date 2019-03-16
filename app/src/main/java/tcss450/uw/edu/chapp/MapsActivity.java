package tcss450.uw.edu.chapp;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Map Activity to select a location for the weather
 *
 * @author Mike Osborne, Trung Thai, Michael Josten, Jessica Medrzycki
 */
public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private Location mCurrentLocation = null;
    private Bundle mArguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //current location could be null
        mCurrentLocation = getIntent().getParcelableExtra(getString(R.string.keys_weather_map_intent_location));
        mArguments = getIntent().getExtras();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mCurrentLocation != null) {
            //add a marker in the current device location
            LatLng current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 4f));


        } else {
            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }

        mMap.setOnMapClickListener(this::onMapClick);


    }

    /**
     * create a dialog, that when is confirmed, will load the HomeActivity,
     * with the location from the map
     * @param latLng of the area of the map clicked
     */
    @Override
    public void onMapClick(LatLng latLng) {
        new AlertDialog.Builder(this)
                .setTitle("Get Weather?")
                .setMessage("lat: " + latLng.latitude + "\nlon: " + latLng.longitude)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", (i, d) -> {
                    //create location
                    Location location = new Location("");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    //create intent for loading HomeActivity
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra(getString(R.string.keys_weather_from_map_activity), true);
                    intent.putExtra(getString(R.string.keys_weather_location_from_map), location);
                    if (mArguments != null) {
                        intent.putExtra(getString(R.string.key_credentials),
                                mArguments.getSerializable(getString(R.string.key_credentials)));
                        intent.putExtra(getString(R.string.keys_intent_jwt),
                                mArguments.getString(getString(R.string.keys_intent_jwt)));
                    }
                    startActivity(intent);
                    finish();

                })
                .show();
    }
}
