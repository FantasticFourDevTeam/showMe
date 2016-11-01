package com.example.FundigoApp.StaticMethod;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.example.FundigoApp.GlobalVariables;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSMethods {
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static Geocoder gcd;

    public interface GpsICallback {
        void gpsCallback();
    }


    public static void updateDeviceLocationGPS(Context context, GpsICallback iCallback) {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        boolean passive_enabled = false;

        locationManager = (LocationManager) context.getSystemService (Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener (iCallback, context);
        try {
            gps_enabled = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        try {
            network_enabled = locationManager.isProviderEnabled (LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        try {
            passive_enabled = locationManager.isProviderEnabled (LocationManager.PASSIVE_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission (context, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (context, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //do none
            } else {
                locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER,
                                                               GlobalVariables.GPS_UPDATE_TIME_INTERVAL,
                                                               0,
                                                               locationListener);
            }
        }
        if (network_enabled) {
            locationManager.requestLocationUpdates (LocationManager.NETWORK_PROVIDER,
                                                           GlobalVariables.GPS_UPDATE_TIME_INTERVAL,
                                                           0,
                                                           locationListener);
        }
        if (passive_enabled) {
            locationManager.requestLocationUpdates (LocationManager.PASSIVE_PROVIDER,
                                                           GlobalVariables.GPS_UPDATE_TIME_INTERVAL,
                                                           0,
                                                           locationListener);
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt (context.getContentResolver (), Settings.Secure.LOCATION_MODE);
            } catch (SettingNotFoundException e) {
                e.printStackTrace ();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString (context.getContentResolver (), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty (locationProviders);
        }
    }

    private static class MyLocationListener implements LocationListener {
        GpsICallback ic;
        Context context;

        MyLocationListener(GpsICallback iCallback, Context context) {
            ic = iCallback;
            this.context = context;
        }

        @Override
        public void onLocationChanged(Location location) {

            if (location != null) {
                String cityGPS = findCurrentCityGPS (location);
                GlobalVariables.MY_LOCATION = location;
               // if (!cityGPS.isEmpty ()) {
                    GlobalVariables.CITY_GPS = cityGPS;
                    ic.gpsCallback();
               // }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
//                GlobalVariables.MY_LOCATION=null;
//                ic.gpsCallback(); // call gps when there is a disconnection or out of GPS is out service//            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            ic.gpsCallback(); // call gps when there is a disconnection
        }

        public String findCurrentCityGPS(Location loc) {
            Geocoder gcd = new Geocoder (context, Locale.getDefault());// 31.10 assaf changed to support also Hebrew locations
            if (loc != null) {
                List<Address> addresses = null;
                try {
                   addresses = gcd.getFromLocation (loc.getLatitude (), loc.getLongitude (), 1);
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                if (addresses != null && addresses.size () > 0) {

                    return addresses.get (0).getLocality ();
                }
            }
            return "";
        }
    }

    public static int getCityIndexFromName(String name) {
        for (int i = 0; i < GlobalVariables.namesCity.length; i++) {
            String city = GlobalVariables.namesCity[i];
            if (city.equals (name)) {
                return i;
            }
        }
        return -1;
    }


    //Assaf - 30.10 -Not In Use
   /* public static String findAddressByCoordinates (Context context , Double latitude , Double Longitude){ // 26.10 assaf to get Address in Other language we use coordoinates to get the address


        if (gcd == null) {

            gcd = new Geocoder(context, Locale.getDefault());
        }
        if (latitude != null && Longitude != null) {
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation (latitude, Longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                if (addresses != null && addresses.size () > 0) {

                   String city  = addresses.get (0).getLocality();
                   String streetAddress = addresses.get(0).getThoroughfare(); //addresses.get (0).getAddressLine(0);
                   String number = addresses.get(0).getSubThoroughfare();
                   return streetAddress + ", " + number + ", " + city + " ";
                }
            }
            return "";
    }*/

    //Assaf - 30.10 - Not in Use

    /*public static void findAddressByAddress(Context context , String eventAddress){ // 26.10 assaf to get Address in Other language we use coordoinates to get the address using Geodocer

        if (gcd == null) {

            gcd = new Geocoder(context);
        }
        if (eventAddress != null && eventAddress != null) {
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocationName(eventAddress, 1);

            } catch (IOException e) {
                e.printStackTrace ();
            }
            if (addresses != null && addresses.size () > 0) {

                String city  = addresses.get (0).getLocality();
                String streetAddress =  addresses.get (0).getAddressLine(0);
                Double lat = addresses.get(0).getLatitude();
                Double lnt = addresses.get(0).getLongitude();

                //return streetAddress + ", " + city;
                Log.i("Geo-Geocoder", streetAddress + ", " + city + " " + Double.toString(lat) + Double.toString(lnt));
            }
        }
        //return "";
    }*/


}
