package neeti.contactapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AgendaMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseUser user;
    DatabaseReference mDatabase;
    Marker marker;

    private HashMap<Marker, String> hashMapContact = new HashMap<Marker, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_map);

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = database.getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("agenda");
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        System.out.print("abcd2");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot nameSnapshot: dataSnapshot.getChildren()){
                    String agendaTitle = nameSnapshot.child("title").getValue(String.class);
                    Double lat = nameSnapshot.child("selectLatitude").getValue(Double.class);
                    Double lng = nameSnapshot.child("selectLongitude").getValue(Double.class);
                    String agendaPlace = nameSnapshot.child("selectedPlace").getValue(String.class);
                    String agendaID = nameSnapshot.getKey();


                    System.out.print("abcd3");
                    // Add a marker in Sydney, Australia,
                    // and move the map's camera to the same location.
                    if(lat!=null && lng!=null){
                        LatLng agendaPosition = new LatLng(lat, lng);
                        marker = googleMap.addMarker(new MarkerOptions().position(agendaPosition)
                                .title(agendaTitle).snippet(agendaPlace));
                        if (agendaID!=null){
                            hashMapContact.put(marker, agendaID);
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

        startActivity(new Intent(AgendaMapActivity.this, HomeActivity.class));
        finish();
    }

}
