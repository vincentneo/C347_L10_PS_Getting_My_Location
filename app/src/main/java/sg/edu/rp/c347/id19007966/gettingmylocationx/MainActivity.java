package sg.edu.rp.c347.id19007966.gettingmylocationx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView coordinatesTextView;
    Button getLocationButton, removeLocationButton, checkRecordsButton;
    GoogleMap map;

    // singapore centre's coordinates
    LatLng singaporeCoords = new LatLng(1.3521, 103.8198);
    Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // location permission
        String[] fineLoc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this, fineLoc, 0);

        String folderLocation = getFilesDir().getAbsolutePath() + "/LocationLogs";
        File folder = new File(folderLocation);
        if (!folder.exists()) {
            boolean result = folder.mkdir();
            if (result) {
                Log.d("File read/write", "Folder Created");
            }
            else {
                Toast.makeText(this, "folder creation FAILED!!", Toast.LENGTH_SHORT).show();
            }
        }

        File locationLog = new File(folderLocation, "log.txt");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatesTextView = findViewById(R.id.lastCoordinatesTextView);
        getLocationButton = findViewById(R.id.getLocationButton);
        removeLocationButton = findViewById(R.id.removeLocationButton);
        checkRecordsButton = findViewById(R.id.checkRecordsButton);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000); // in ms
        locationRequest.setSmallestDisplacement(500);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                coordinatesTextView.setText(textFrom(location));

                if (currentLocationMarker == null) {
                    MarkerOptions options = new MarkerOptions()
                            .position(coordinatesFrom(location))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    currentLocationMarker = map.addMarker(options);
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(coordinatesFrom(location), 16));
                }
                else {
                    currentLocationMarker.setPosition(coordinatesFrom(location));
                    map.moveCamera(CameraUpdateFactory
                            .newLatLng(coordinatesFrom(location)));
                }

                try {
                    FileWriter writer = new FileWriter(locationLog, true);
                    writer.write(coordinatesForRecords(location));
                    writer.flush();
                    writer.close();
                }
                catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Fail to log location", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        };

        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.mapView);

        mapFragment.getMapAsync(gMap -> {
            map = gMap;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(singaporeCoords, 10));
            UiSettings uiSettings = map.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
        });

        removeLocationButton.setEnabled(false);

        getLocationButton.setOnClickListener(view -> {
            if (checkLocationPermission()) {
                client.requestLocationUpdates(locationRequest, locationCallback, null);
                getLocationButton.setEnabled(false);
                removeLocationButton.setEnabled(true);
            }
            else {
                failedPermissionToast();
            }
        });
        removeLocationButton.setOnClickListener(view -> {
            if (checkLocationPermission()) {
                client.removeLocationUpdates(locationCallback);
                getLocationButton.setEnabled(true);
                removeLocationButton.setEnabled(false);
            }
            else {
                failedPermissionToast();
            }
        });

        checkRecordsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkLocationPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    private void failedPermissionToast() {
        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
    }
    private String textFrom(Location location) {
        return "Latitude: " + location.getLatitude()
                + "\nLongitude: " + location.getLongitude();
    }

    private LatLng coordinatesFrom(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private String coordinatesForRecords(Location location) {
        return location.getLatitude() + ", " + location.getLongitude() + "\n";
    }

}