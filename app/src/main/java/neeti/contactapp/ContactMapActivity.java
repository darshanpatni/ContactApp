package neeti.contactapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseUser user;
    DatabaseReference mDatabase;
    Marker marker;

    GoogleMap mMap;
    HashMap markerMap = new HashMap();
    HashMap markerMap1 = new HashMap();
    private HashMap<Marker, String> hashMapContact = new HashMap<Marker, String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_map);


        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = database.getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        System.out.print("abcd1");
        EditText locationSearch = (EditText) findViewById(R.id.editText);

        locationSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    try {
                        onMapSearch(ContactMapActivity.super.getCurrentFocus());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
    }




    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        System.out.print("abcd2");
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot nameSnapshot: dataSnapshot.getChildren()){
                    String contactName = nameSnapshot.child("name").getValue(String.class);
                    Double lat = nameSnapshot.child("selectLatitude").getValue(Double.class);
                    Double lng = nameSnapshot.child("selectLongitude").getValue(Double.class);
                    String contactPhone = nameSnapshot.child("phone").getValue(String.class);
                    String contactID = nameSnapshot.getKey();


                    System.out.print("abcd3");
                    // Add a marker in Sydney, Australia,
                    // and move the map's camera to the same location.
                    if(lat!=null && lng!=null){
                        LatLng contactPosition = new LatLng(lat, lng);
                        marker = mMap.addMarker(new MarkerOptions().position(contactPosition)
                                .title(contactName).snippet(contactPhone));
                        if (contactID!=null){
                            hashMapContact.put(marker, contactID);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 10.0f));
                        markerMap.put(contactName, marker);
                        markerMap1.put(contactPhone, marker);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = hashMapContact.get(marker);
                Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    public void onMapSearch(View view) throws JSONException {
        EditText locationSearch = (EditText) findViewById(R.id.editText);

        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        Marker marker = (Marker) markerMap.get(location);

        if (marker != null) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 10.0f));
        } else if ((marker = (Marker) markerMap1.get(location)) != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 10.0f));
        } else if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } else {

                String link = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&sensor=false";

                GetLocationDownloadTask getLocation = new GetLocationDownloadTask();

                getLocation.execute(link);


            }

        }
    }



    @Override
    public void onBackPressed() {

        startActivity(new Intent(ContactMapActivity.this, HomeActivity.class));
        finish();
    }

    public class GetLocationDownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {


            String result = "";
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(is);

                int data = inputStreamReader.read();
                while (data != -1) {
                    char curr = (char) data;
                    result += curr;
                    data = inputStreamReader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject locationObject = new JSONObject(result);
                    //JSONObject locationGeo = locationObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                    double lng = ((JSONArray) locationObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");

                    double lat = ((JSONArray) locationObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");


                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10.0f));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
