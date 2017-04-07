package neeti.contactapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;

public class ContactMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseUser user;
    DatabaseReference mDatabase;
    Marker marker;

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
    }




    @Override
    public void onMapReady(final GoogleMap googleMap) {

        System.out.print("abcd2");
        googleMap.setMapStyle(
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
                        marker = googleMap.addMarker(new MarkerOptions().position(contactPosition)
                                .title(contactName).snippet(contactPhone));
                        if (contactID!=null){
                            hashMapContact.put(marker, contactID);
                        }
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 10.0f));

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

    @Override
    public void onBackPressed() {

        startActivity(new Intent(ContactMapActivity.this, HomeActivity.class));
        finish();
    }

}
