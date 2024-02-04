package com.example.newgpsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{

    LocationManager locationManager;
    TextView longitudeTextView;
    TextView latitudeTextView;
    TextView addressTextView;
    TextView totalDistanceTextView;
    TextView timeSpentTextView;
    TextView favoriteLocationTextView;
    Bundle bundle;
    double latitude;
    double longitude;
    Location initialLocation;
    double totalDistance = 0;
    ArrayList<Location> locationArrayList;
    ArrayList<Double> distanceArrayList;
    ArrayList<Long> timeArrayList;
    ArrayList<String> addressArrayList;
    Long startTime;
    Long endTime;
    Location favoriteLocation;
    Long longestTime;
    int indexOfLongestTime;
    boolean changedLocations = false;
    boolean listenerAdded = false;
    List<Address> addresses = new ArrayList<Address>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //bundle = savedInstanceState;

        longitudeTextView = findViewById(R.id.id_longitude);
        latitudeTextView = findViewById(R.id.id_latitude);
        addressTextView = findViewById(R.id.id_address);
        totalDistanceTextView = findViewById(R.id.id_totalDistance);
        timeSpentTextView = findViewById(R.id.id_timeSpent);
        favoriteLocationTextView = findViewById(R.id.id_favoriteLocation);

        locationArrayList = new ArrayList<Location>();
        distanceArrayList = new ArrayList<Double>();
        timeArrayList = new ArrayList<Long>();
        addressArrayList = new ArrayList<String>();


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        /*
        int a = 0;
        while(a==0){
            Log.d("TAG", "in while");
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "breaking");
                a++;
            }
        }

         */



        locationManager = (LocationManager) (getSystemService(LOCATION_SERVICE));

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, this);





    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

        Log.d("TAG", "locato"+location);

        try {
            int count = 0;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("TAG", "Latitude: " + (((int)(100*latitude))/(double)100));
            Log.d("TAG", "Longitude: " + (((int)(100*longitude))/(double)100));
            //latitudeTextView.setText("Lat: " + (((int)(10000*latitude))/(double)10000));
            //longitudeTextView.setText("Lon: " + (((int)(10000*longitude))/(double)10000));
            latitudeTextView.setText("Lat: " + latitude);
            longitudeTextView.setText("Lon: " + longitude);
            Log.d("TAG", "Setting");



            AsyncThread asyncThread = new AsyncThread();
            asyncThread.execute(latitude,longitude);


            Log.d("TAG", "point1");
            locationArrayList.add(location);
            Log.d("TAG", "point2");

            if(initialLocation == null){
                Log.d("TAG", "point3");
                initialLocation = location;
                startTime = System.currentTimeMillis();
                Log.d("TAG", "point4");
                favoriteLocation = location;
                indexOfLongestTime = 0;
            }
            else {
                changedLocations = true;
                endTime = System.currentTimeMillis();
                Long timePassed = endTime - startTime;
                timePassed/=1000;
                timeArrayList.add(timePassed);
                Long longestTime = timeArrayList.get(0);
                int indexOfLongestTime = 0;
                for(int i = 0; i< timeArrayList.size(); i++){
                    if(timeArrayList.get(i)>longestTime) {
                        longestTime = timeArrayList.get(i);
                        indexOfLongestTime = i;
                    }
                }


                timeSpentTextView.setText("Time Spent at favorite location: " + longestTime + " seconds");

                totalDistance += initialLocation.distanceTo(location);
                //int tempRound = (int)((totalDistance/1609)*10)/(double(10));
                totalDistanceTextView.setText("Total Distance: " + (((int)(100*(totalDistance/1609)))/(double)100) + " miles");
                distanceArrayList.add((double)initialLocation.distanceTo(location));
                initialLocation = location;
                startTime = endTime;
            }







        }catch (Exception e){
            Log.d("TAG", "exception");
        }
    }

    @SuppressLint({"MissingPermission"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults.length!=0 && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                Log.d("TAG", "deniedi");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
                //else {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1L, 1f, this);
           // }

    }
        // Other 'case' lines to check for other
        // permissions this app might request.



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        locationManager.removeUpdates(this);
        super.onSaveInstanceState(outState);
    }

    public class AsyncThread extends AsyncTask<Double,Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(Double... doubles) {
            Log.d("TAG", "here");
            Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.US);
            int count = 0;
            try {
                Log.d("TAG", "here2");
                if(geocoder.isPresent())
                    addresses = geocoder.getFromLocation(doubles[0], doubles[1], 1);
                //addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Log.d("TAG", "here3");
            }catch (Exception e) {
                Log.d("Tag", e.toString());
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

                addressTextView.setText(addresses.get(0).getAddressLine(0));
                addressArrayList.add(addresses.get(0).getAddressLine(0));
                if (changedLocations) {
                    Long longest = 0L;
                    for(int i = 0; i< timeArrayList.size(); i++){
                        if(timeArrayList.get(i)>longest) {
                            longest = timeArrayList.get(i);
                            indexOfLongestTime = i;
                        }
                    }
                    Log.d("TAG", "favaddy: " + addressArrayList.get(indexOfLongestTime));
                    favoriteLocationTextView.setText("Favorite location: " + addressArrayList.get(indexOfLongestTime));
                }
        }


    }




}