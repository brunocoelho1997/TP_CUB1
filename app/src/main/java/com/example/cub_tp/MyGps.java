package com.example.cub_tp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class MyGps {

    private double actualLongitude, actualLatitude, actualAltitude;

    //----sensors
    // Acquire a reference to the system Location Manager
    private static LocationManager locationManager;
    // Define a listener that responds to location updates
    private static LocationListener locationListener;

    public MyGps() {
        //do nothing.
    }

    public void startGpsListening(Context context){
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = (LocationListener) getNewLocationListener(context);

        // Define a listener that responds to location updates
        defineLastLocation(context);
        requestGPSUpdates(context);
    }
    public void stopGpsListening(Context context){
        if(locationManager != null)
            locationManager.removeUpdates(locationListener);
        locationManager = null;
    }

    private LocationListener getNewLocationListener(final Context context) {
        return new LocationListener() {

            public void onLocationChanged(final Location location) {
                final double longitude = location.getLongitude();
                final double latitude = location.getLatitude();
                final double altitude= location.getAltitude();
                actualLatitude = latitude;
                actualLongitude = longitude;
                actualAltitude = altitude;

                String txtGps ="";
                txtGps +="Gps:\n";
                txtGps +="latitude:" + latitude + "\n";
                txtGps +="longitude:" + longitude + "\n";
                txtGps +="altitude:" + altitude + "\n";
                txtGps +="Providers: " + locationManager.getProviders(true) + "\n";
                txtGps +="Atual provider: " + location.getProvider() + "\n";
                txtGps +="Gps Accuracy: " + location.getAccuracy() + "\n";

                MainActivity.tvInfoGps.setText(txtGps);

                //Toast.makeText(context, "Network Provider update", Toast.LENGTH_SHORT).show();

                //every time we get a new location we save on the file
                FileManager.saveOnTxtFile();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    private static void defineLastLocation(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation("gps");
        MainActivity.tvInfoGps.setText("" + location.getLatitude() + " - " + location.getLongitude());
        Toast.makeText(context, "Last Location", Toast.LENGTH_SHORT).show();
    }

    private static void requestGPSUpdates(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(context, "requestLocationUpdates", Toast.LENGTH_SHORT).show();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 100, locationListener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 100, locationListener);

    }

    public double getActualLongitude() {
        return actualLongitude;
    }

    public double getActualLatitude() {
        return actualLatitude;
    }

    public double getActualAltitude() {
        return actualAltitude;
    }
}
