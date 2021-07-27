package sg.edu.rp.c347.id19007966.gettingmylocationx;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;

public class LocationService extends Service {

    private LocationBinder binder = new LocationBinder();
    FusedLocationProviderClient client;
    LocationCallback locationCallback;

    class LocationBinder extends Binder {
        //Location lastLocation;
        //LocationService locationService = LocationService.this;
        void start(MainActivity mainActivity) {

            String folderLocation = getFilesDir().getAbsolutePath() + "/LocationLogs";
            File folder = new File(folderLocation);
            if (!folder.exists()) {
                boolean result = folder.mkdir();
                if (result) {
                    Log.d("File read/write", "Folder Created");
                }
                else {
                    Toast.makeText(getApplicationContext(), "folder creation FAILED!!", Toast.LENGTH_SHORT).show();
                }
            }

            File locationLog = new File(folderLocation, "log.txt");


            client = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(0); // in ms
            locationRequest.setSmallestDisplacement(0);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (mainActivity != null) {
                        mainActivity.updateLastLocation(location);
                    }

                    try {
                        FileWriter writer = new FileWriter(locationLog, true);
                        writer.write(coordinatesForRecords(location));
                        writer.flush();
                        writer.close();
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Fail to log location", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            };
            if (checkLocationPermission()) {
                client.requestLocationUpdates(locationRequest, locationCallback, null);
            }
            else {
                failedPermissionToast();
            }
        }
    }
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private boolean checkLocationPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_Bg = ContextCompat.checkSelfPermission(
                LocationService.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

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

    @Override
    public void onDestroy() {
        if (checkLocationPermission()) {
            client.removeLocationUpdates(locationCallback);

        }
        else {
            failedPermissionToast();
        }
        super.onDestroy();
    }

    private String coordinatesForRecords(Location location) {
        return location.getLatitude() + ", " + location.getLongitude() + "\n";
    }
}