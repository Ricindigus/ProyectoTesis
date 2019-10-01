package pe.com.ricindigus.proyectotesis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private FloatingActionButton fabBuscar;
    private BottomNavigationView bottomNavigationView;


    private static final String TAG = MapsActivity.class.getSimpleName();


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 16;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private List<Result> results = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpButtonSearch();
        setupBottomNavigation();
    }

    private void setUpButtonSearch() {
        fabBuscar = findViewById(R.id.fabBuscarAgencia);
        fabBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null && !results.isEmpty()){


                    String origin = mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude();
                    mLastKnownLocation.getLongitude();
                    String destination = results.get(0).getGeometry().getLocation().getLat() + ","+
                            results.get(0).getGeometry().getLocation().getLng();
                    final Call<Route> call = WebServiceMaps.getInstance()
                            .createService(ServiceMapsApi.class)
                            .getRoute(origin,destination,getResources().getString(R.string.google_maps_key));
                    progressDialog = ProgressDialog.show(MapsActivity.this, "Mapas Tesis",
                            "Buscando la mejor opción", true, true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    call.cancel();
                                    finish();
                                }
                            });
                    call.enqueue(new Callback<Route>() {
                        @Override
                        public void onResponse(Call<Route> call, Response<Route> response) {
                            if (response.code() == HttpURLConnection.HTTP_OK){
                                progressDialog.dismiss();
                                String polyline = response.body()
                                        .getRoutes().get(0)
                                        .getOverviewPolyline()
                                        .getPoints();
                                drawPolyline(polyline);
                            }
                        }

                        @Override
                        public void onFailure(Call<Route> call, Throwable t) {
                            progressDialog.dismiss();
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void drawPolyline(String polylinePath) {
        List<LatLng> latLngList = PolyUtil.decode(polylinePath);

        PolylineOptions polylineOptions = new PolylineOptions();

        polylineOptions.width(8);
        //Definimos el color de la Polilíneas
        polylineOptions.color(Color.BLACK);

        for (LatLng latLng : latLngList){
            polylineOptions.add(latLng);
        }

//        mMap.addMarker(new MarkerOptions().position(latLngList.get(0)).title("Marker " + 0));
//        mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size()-1)).title("Marker " + (latLngList.size()-1)));
        Polyline polyline = mMap.addPolyline(polylineOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngList.get(0)));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();

        getDeviceLocation();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
//            mMap.setTrafficEnabled(true);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                        Manifest.permission.ACCESS_FINE_LOCATION, true);

            }else{
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            LatLng myPosition = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(myPosition).title("Tu posición"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            String locationString = mLastKnownLocation.getLatitude()+","+ mLastKnownLocation.getLongitude();
                            findAgencies(locationString,"1000","BCP",BitmapDescriptorFactory.HUE_AZURE,"BCP");
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void findAgencies(String location, String radius,
                              String keyword, final float colorPins,
                                String titleAgency) {
        mMap.clear();
        final Call<Places> call = WebServiceMaps.getInstance()
                .createService(ServiceMapsApi.class)
                .getPlaces(location,radius,"bank",keyword,
                        getResources().getString(R.string.google_maps_key));

        progressDialog = ProgressDialog.show(MapsActivity.this, "Mapas Tesis",
                "Buscando agencias " + titleAgency + " cercanas", true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        call.cancel();
                        finish();
                    }
                });

        call.enqueue(new Callback<Places>() {
            @Override
            public void onResponse(Call<Places> call, Response<Places> response) {
                if (response.code() == HttpURLConnection.HTTP_OK){
                    progressDialog.dismiss();
                    results = response.body().getResults();
                    for (Result r : results){
                        LatLng latLng = new LatLng(r.getGeometry().getLocation().getLat(),
                                r.getGeometry().getLocation().getLng());
                        LatLng myPosition = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        //add my marker
                        mMap.addMarker(new MarkerOptions()
                                .position(myPosition)
                                .title("Tu posición")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        //add all marker of agencies
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(r.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(colorPins)));
                    }
                }
            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {
                finish();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int idItem = menuItem.getItemId();
                if (idItem == R.id.action_bcp){
                    String locationString = mLastKnownLocation.getLatitude()+","+ mLastKnownLocation.getLongitude();
                    findAgencies(locationString,"1000","BCP",
                            BitmapDescriptorFactory.HUE_AZURE,"BCP");
                    return true;
                }else if (idItem == R.id.action_scotiabank){
                    String locationString = mLastKnownLocation.getLatitude()+","+ mLastKnownLocation.getLongitude();
                    findAgencies(locationString,"1000","Scotiabank",
                            BitmapDescriptorFactory.HUE_RED,"Scotiabank");
                    return true;
                }else if (idItem == R.id.action_interbank){
                    String locationString = mLastKnownLocation.getLatitude()+","+ mLastKnownLocation.getLongitude();
                    findAgencies(locationString,"1000","Interbank",
                            BitmapDescriptorFactory.HUE_YELLOW,"Interbank");
                    return true;
                }else if (idItem ==R.id.action_bbva){
                    String locationString = mLastKnownLocation.getLatitude()+","+ mLastKnownLocation.getLongitude();
                    findAgencies(locationString,"1000","BBVA",
                            BitmapDescriptorFactory.HUE_CYAN,"BBVA");
                    return true;
                }
                return false;
            }
        });
    }

    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-12.178163%2C-77.0239138&radius=1500&type=bank&keyword=BCP&key=AIzaSyB8H60Ay0aohbSWMURj5LgwNkHAJ6zppXk&fbclid=IwAR3ougOj8t9aOgQARW9hCN0vilcxQfoQ0L_y5hBQ_kHePfIKKRcnTsqMv9c
}
