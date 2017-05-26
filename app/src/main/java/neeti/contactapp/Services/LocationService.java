package neeti.contactapp.Services;

/**
 * Created by Darshan on 04-05-2017.
 */

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import neeti.contactapp.LoginActivity;
import neeti.contactapp.NotificationViewActivity;
import neeti.contactapp.R;

public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 1;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    String currentCity = null;
    String newCity = null;

    float count = 0;
    long nCount = 0;
    Context context;

    Intent intent;
    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        intent = new Intent(BROADCAST_ACTION);
        context = this;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {


        double newLat = location.getLatitude();
        double newlng = location.getLongitude();


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        if (currentBestLocation != null) {
            double currentLat = currentBestLocation.getLatitude();
            double currentLng = currentBestLocation.getLongitude();

            String link = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+currentLat+","+currentLng+"&sensor=true";

            StringRequest stringRequest = new StringRequest(Request.Method.GET, link,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray jObj = new JSONObject(response).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                                for (int i = 0; i < jObj.length(); i++) {
                                    String componentName = new JSONObject(jObj.getString(i)).getJSONArray("types").getString(0);
                                    if (componentName.equals("locality")) {
                                        currentCity = new JSONObject(jObj.getString(i)).getString("long_name");
                                    }
                                    if (currentCity == null) {
                                        if (componentName.equals("administrative_area_level_2")) {
                                            currentCity = new JSONObject(jObj.getString(i)).getString("long_name");
                                        }
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    int x = 1;
                }
            });
            queue.add(stringRequest);
        }
        String link2 = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+newLat+","+newlng+"&sensor=true";
        //RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, link2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jObj = new JSONObject(response).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                            for (int i = 0; i < jObj.length(); i++) {
                                String componentName = new JSONObject(jObj.getString(i)).getJSONArray("types").getString(0);
                                if (componentName.equals("locality")) {
                                    newCity = new JSONObject(jObj.getString(i)).getString("long_name");
                                }
                                if (newCity==null){
                                    if(componentName.equals("administrative_area_level_2")){
                                        newCity = new JSONObject(jObj.getString(i)).getString("long_name");
                                    }
                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int x = 1;
            }
        });
        queue.add(stringRequest2);

        //if()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Objects.equals(newCity, currentCity)) {
                // A new location is always better than no location

                return true;
            }
        }

        return false;
        // Check whether the new location fix is newer or older

        /*long timeDelta = location.getTime() - (currentBestLocation != null ? currentBestLocation.getTime() : 0);
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the ne     int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = acw location fix is more or less accurate
   curacyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;*/
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }




    public class MyLocationListener implements LocationListener{

        private DatabaseReference rDatabase;
        FirebaseUser user;
        private FirebaseAuth mAuth;
        private StorageReference mStorageRef;
        private FirebaseAuth.AuthStateListener mAuthListener;


        public void onLocationChanged(final Location loc)
        {
            Log.i("**********", "Location changed");


            if(isBetterLocation(loc, previousBestLocation)) {


                //initialize Firebase variables
                mStorageRef = FirebaseStorage.getInstance().getReference();
                user = FirebaseAuth.getInstance().getCurrentUser();

                mAuth = FirebaseAuth.getInstance();
                mAuthListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            // user auth state is changed - user is null
                            // launch login activity
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                    }
                };

                if (user != null) {

                    rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");

                }


                    Query query = rDatabase.orderByChild("city").equalTo(newCity);

                    /*query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            float c = 0;
                            c = +1;
                                // ..
                            }

                           if(dataSnapshot.getValue()!=null){
                               count = 1;
                           }
                           else {
                               count = 0;
                           }
                           // count = c;
                            //c = dataSnapshot.getChildrenCount();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                                count = 0;
                        }
                    });*/


                    count = 0;
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()!=null){
                            count =1;
                            NotificationCompat.Builder mBuilder =
                                    (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.icon)
                                            .setAutoCancel(true)
                                            .setContentTitle("Contacts Found!")
                                            .setContentText("You have contacts in " + newCity +" , tap to see");

                            int NOTIFICATION_ID = 12345;
                            Intent targetIntent = new Intent(getApplicationContext(), NotificationViewActivity.class);
                            targetIntent.putExtra("city",newCity);
                            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            mBuilder.setContentIntent(contentIntent).setVibrate(new long[] { 1000, 1000}).setSound(Settings.System.DEFAULT_NOTIFICATION_URI).getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
                            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nManager.notify(NOTIFICATION_ID, mBuilder.build());
                            currentCity = newCity;
                        }
                        else
                            count = 0;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                            count = 0;
                    }
                });



                    if (count>0) {
                        count=0;


                    }

                    //Toast.makeText(context, "Latitude" + loc.getLatitude() + "\nLongitude"+loc.getLongitude(),Toast.LENGTH_SHORT).show();
                    /*intent.putExtra("Latitude", loc.getLatitude());
                    intent.putExtra("Longitude", loc.getLongitude());
                    intent.putExtra("Provider", loc.getProvider());
                    sendBroadcast(intent);*/

            }
        }

        public void onProviderDisabled(String provider)
        {
            //Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            //Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }
}