package sg.edu.rp.c347.id19007966.gettingmylocationx;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    ToggleButton musicToggle;
    GoogleMap map;

    // singapore centre's coordinates
    LatLng singaporeCoords = new LatLng(1.3521, 103.8198);
    Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // location permission
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this, permissions, 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatesTextView = findViewById(R.id.lastCoordinatesTextView);
        getLocationButton = findViewById(R.id.getLocationButton);
        removeLocationButton = findViewById(R.id.removeLocationButton);
        checkRecordsButton = findViewById(R.id.checkRecordsButton);
        musicToggle = findViewById(R.id.musicToggle);

        musicToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Intent i = new Intent(MainActivity.this, AudioService.class);
                if (checked) {
                    startService(i);
                }
                else {
                    stopService(i);
                }
            }
        });

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
            Intent bindIntent = new Intent(MainActivity.this, LocationService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(bindIntent);
//            }
            bindService(bindIntent, connection, BIND_AUTO_CREATE);
            getLocationButton.setEnabled(false);
            removeLocationButton.setEnabled(true);
        });
        removeLocationButton.setOnClickListener(view -> {
            unbindService(connection);
            getLocationButton.setEnabled(true);
            removeLocationButton.setEnabled(false);
        });

        checkRecordsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(intent);
        });
    }

    private LocationService.LocationBinder binder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (LocationService.LocationBinder) iBinder;
            binder.start(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("disconnected");
        }
    };

    void updateLastLocation(Location location) {
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
    }

    private String textFrom(Location location) {
        return "Latitude: " + location.getLatitude()
                + "\nLongitude: " + location.getLongitude();
    }

    private LatLng coordinatesFrom(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }



}