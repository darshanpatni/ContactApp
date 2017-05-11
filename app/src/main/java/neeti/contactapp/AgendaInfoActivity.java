package neeti.contactapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgendaInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    List<String> contactList;

    HashMap<String, String> agendaMap = new HashMap<String, String>();
    HashMap<String, String> contactMap = new HashMap<String, String>();
    private DatabaseReference contactDatabase, agendaDatabase, getAgendaDatabase;
    FirebaseUser user;
    Query query = null;
    Query agendaQuery = null;
    Query contactQuery = null;

    TextView agenda_title;
    TextView agenda_description;
    TextView agenda_date;

    String agendaTitle;
    String agendaDescription;
    String agendaDate;
    String contactName;
    String contactKey;

    String currentAgenda;

    Double lat, lng;
    LatLng latLng;

    Marker marker;

    GoogleMap mMap;
    HashMap markerMap = new HashMap();
    HashMap markerMap1 = new HashMap();
    private HashMap<Marker, String> hashMapContact = new HashMap<Marker, String>();

    ImageView backBtn;

    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_info);

        getSupportActionBar().hide();
        expListView = (ExpandableListView) findViewById(R.id.contact_list);

        agenda_title = (TextView) findViewById(R.id.agenda_title);
        agenda_description = (TextView) findViewById(R.id.agenda_description);
        agenda_date = (TextView) findViewById(R.id.agenda_date);
        backBtn = (ImageView) findViewById(R.id.btn_back);

        user = FirebaseAuth.getInstance().getCurrentUser();

        currentAgenda = getIntent().getStringExtra("key");

        agendaDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child(user.getDisplayName()).child("agenda");

        agendaDatabase.keepSynced(true);
        contactDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");

        contactDatabase.keepSynced(true);
        query = agendaDatabase.child(currentAgenda);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                agendaTitle = dataSnapshot.child("title").getValue(String.class);
                agenda_title.setText(agendaTitle);

                agendaDate = dataSnapshot.child("date").getValue(String.class);
                agenda_date.setText(agendaDate);

                agendaDescription = dataSnapshot.child("description").getValue(String.class);
                agenda_description.setText(agendaDescription);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);


        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String clickedChild = listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);
                String conKey = contactMap.get(clickedChild);

                if (clickedChild == "+ Add New") {
                    Intent intent = new Intent(AgendaInfoActivity.this, AddAgendaActivity.class);
                    intent.putExtra("selectedContact", contactName);
                    startActivity(intent);
                }

                else{
                    Intent openContact = new Intent(AgendaInfoActivity.this, ContactInfoActivity.class);
                    openContact.putExtra("key",conKey);
                    startActivity(openContact);
                }
                Toast.makeText(getApplicationContext(), "" + clickedChild + " Key: " + conKey + " currentContact: " + currentAgenda
                        , Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        prepareListData();

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        contactList = new ArrayList<String>();

        // Adding child data
        listDataHeader.add("Contacts ");

        final List<String> contacts = new ArrayList<String>();
        final List<String> agendas = new ArrayList<String >();


            agendaDatabase.child(currentAgenda).child("contacts")
                    .orderByChild("contactID").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //contactName = dataSnapshot.child("contactName").getValue(String.class);
                //contactKey = dataSnapshot.child("contactID").getValue(String.class);
                final String entryKey = dataSnapshot.getKey();


                contactDatabase.child(dataSnapshot.child("contactID").getValue(String.class))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            contactName = dataSnapshot.child("name").getValue(String.class);
                            contactKey = dataSnapshot.getKey();
                            contactMap.put(contactName, contactKey);
                            contacts.add(contactName);
                        }
                        else{
                            agendaDatabase.child(currentAgenda).child("contacts").child(entryKey).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        contacts.add("+ Add New");

        listDataChild.put(listDataHeader.get(0), contacts);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

        agendaDatabase.child(currentAgenda).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lat = dataSnapshot.child("selectLatitude").getValue(Double.class);
                lng = dataSnapshot.child("selectLongitude").getValue(Double.class);
                if(lat!=null && lng!=null) {

                    latLng = new LatLng(lat, lng);
                    Toast.makeText(getApplicationContext(), lat.toString()+lng, Toast.LENGTH_LONG).show();

                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                            .title(agendaTitle).snippet(agendaDate));
                    hashMapContact.put(marker, agendaTitle);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10.0f));
                    markerMap.put(contactName, marker);
                    markerMap1.put(agendaTitle, marker);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = hashMapContact.get(marker);
                Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }
}
