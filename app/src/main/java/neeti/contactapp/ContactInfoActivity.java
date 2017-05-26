package neeti.contactapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.security.AccessController.getContext;

public class ContactInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    ExpandableListAdapter listAdapter2;
    ExpandableListView expListView2;
    List<String> listDataHeader2;
    HashMap<String, List<String>> listDataChild2;
    List<String> agendaList;

    HashMap<String, String> agendaMap = new HashMap<String, String>();
    HashMap<String, String> contactMap = new HashMap<String, String>();
    private DatabaseReference contactDatabase, agendaDatabase, getAgendaDatabase;
    FirebaseUser user;
    Query query = null;
    Query contactQuery = null;

    String agendaTitle;
    String agendaKey;
    String conKey;
    String currentContact;

    String refContactName;

    String contactName;
    String contactPhone;
    String contactEmail;
    String contactCompany;
    String contactDomain;
    String contactReferal;
    String contactPhoto;
    String contactReferenceKey;
    String contactAddress;

    Double lat, lng;
    LatLng latLng;

    Marker marker;

    GoogleMap mMap;
    HashMap markerMap = new HashMap();
    HashMap markerMap1 = new HashMap();
    private HashMap<Marker, String> hashMapContact = new HashMap<Marker, String>();


    TextView cName;
    TextView cPhone;
    TextView cEmail;
    TextView cCompany;
    TextView cDomain;
    TextView cReferred;

    LinearLayout emailLayout;
    LinearLayout phoneLayout;
    LinearLayout domainLayout;
    LinearLayout companyLayout;
    LinearLayout referLayout;
    LinearLayout agendaLayout;
    LinearLayout refLayout;
    LinearLayout mapLayout;

    ImageButton callBtn;
    ImageButton smsBtn;
    ImageButton emailBtn;

    FloatingActionButton editBtn;

    ImageView contactPic;
    ImageView backBtn;
    String intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.agenda_lis);
        expListView2 = (ExpandableListView) findViewById(R.id.refer_list);

        cName = (TextView) findViewById(R.id.contact_name);
        cPhone = (TextView) findViewById(R.id.contact_phone);
        cEmail = (TextView) findViewById(R.id.contact_email);
        cCompany = (TextView) findViewById(R.id.contact_company);
        cDomain = (TextView) findViewById(R.id.contact_domain);
        cReferred = (TextView) findViewById(R.id.referred_by);
        emailLayout = (LinearLayout) findViewById(R.id.email_layout);
        phoneLayout = (LinearLayout) findViewById(R.id.phoneLayout);
        domainLayout = (LinearLayout) findViewById(R.id.domain_layout);
        companyLayout = (LinearLayout) findViewById(R.id.company_layout);
        referLayout = (LinearLayout) findViewById(R.id.refer_layout);
        agendaLayout = (LinearLayout) findViewById(R.id.agenda_layout);
        refLayout = (LinearLayout) findViewById(R.id.ref_layout);
        mapLayout = (LinearLayout) findViewById(R.id.map_layout);
        contactPic = (ImageView) findViewById(R.id.ContactPhoto);
        backBtn = (ImageView) findViewById(R.id.btn_back);
        callBtn = (ImageButton) findViewById(R.id.call);
        smsBtn = (ImageButton) findViewById(R.id.message);
        emailBtn = (ImageButton) findViewById(R.id.email);
        editBtn = (FloatingActionButton) findViewById(R.id.edit);

        getSupportActionBar().hide();

        user = FirebaseAuth.getInstance().getCurrentUser();


        intent = getIntent().getStringExtra("intent");
        currentContact = getIntent().getStringExtra("key");

        agendaDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child(user.getDisplayName()).child("agenda");

        contactDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");
        contactDatabase.keepSynced(true);
        query = contactDatabase.child(currentContact);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contactName = dataSnapshot.child("name").getValue(String.class);
                cName.setText(contactName);
                contactPhone = dataSnapshot.child("phone").getValue(String.class);
                cPhone.setText(contactPhone);
                contactCompany = dataSnapshot.child("company").getValue(String.class);
                if (contactCompany == null || contactCompany.length() < 1) {
                    companyLayout.setVisibility(View.GONE);
                } else {
                    cCompany.setText(contactCompany);
                }
                contactEmail = dataSnapshot.child("email").getValue(String.class);
                if (contactEmail == null || contactEmail.length() < 1) {
                    emailLayout.setVisibility(View.GONE);
                } else {
                    cEmail.setText(contactEmail);
                }
                contactDomain = dataSnapshot.child("domain").getValue(String.class);
                if (contactDomain == null || contactDomain.length() < 1) {
                    domainLayout.setVisibility(View.GONE);
                } else {
                    cDomain.setText(contactDomain);
                }
                contactReferal = dataSnapshot.child("reference").getValue(String.class);
                contactReferenceKey = dataSnapshot.child("referenceKey").getValue(String.class);
                if (contactReferal == null || contactReferal.length() < 1 || contactReferal == "NONE") {
                    referLayout.setVisibility(View.GONE);

                } else {
                    cReferred.setText(contactReferal);
                    cReferred.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent openContact = new Intent(ContactInfoActivity.this, ContactInfoActivity.class);
                            openContact.putExtra("key",contactReferenceKey);
                            startActivity(openContact);
                        }
                    });

                }

                contactPhoto = dataSnapshot.child("photoUrl").getValue(String.class);


                if (contactPhoto != null) {
                    Picasso.with(getApplicationContext())
                            .load(contactPhoto)
                            .transform(new CircleTransform())
                            .into(contactPic);
                } else {
                    Picasso.with(getApplicationContext())
                            .load(R.drawable.ic_default_photo)
                            .transform(new CircleTransform())
                            .into(contactPic);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        listAdapter2 = new ExpandableListAdapter(this, listDataHeader2, listDataChild2);

        expListView.setAdapter(listAdapter);

        expListView2.setAdapter(listAdapter2);


        expListView2.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                setListViewHeight(parent, groupPosition);
                return false;
            }
        });
        expListView2.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String clickedChild = listDataChild2.get(
                        listDataHeader2.get(groupPosition)).get(
                                childPosition);
                String conKey = contactMap.get(clickedChild);

                if (clickedChild == "+ Create New") {
                    Intent intent = new Intent(ContactInfoActivity.this, AddContactActivity.class);
                    intent.putExtra("selectedContact", contactName);
                    startActivity(intent);
                }

               else{
                    Intent openContact = new Intent(ContactInfoActivity.this, ContactInfoActivity.class);
                    openContact.putExtra("key",conKey);
                    startActivity(openContact);
                }
                Toast.makeText(getApplicationContext(), "" + clickedChild + " Key: " + conKey + " currentContact: " + currentContact
                        , Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                setListViewHeight(parent, groupPosition);
                return false;
            }
        });
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String clickedChild = listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);
                String key = agendaMap.get(clickedChild);

                if (clickedChild == "+ Create New") {
                    Intent intent = new Intent(ContactInfoActivity.this, AddAgendaActivity.class);
                    intent.putExtra("selectedContact", contactName);
                    startActivity(intent);
                }
                else{
                    Intent openAgenda = new Intent(ContactInfoActivity.this, AgendaInfoActivity.class);
                    openAgenda.putExtra("key", key);
                    startActivity(openAgenda);

                }
                Toast.makeText(getApplicationContext(), "" + clickedChild + " Key: " + key + " currentContact: " + currentContact
                        , Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactPhone));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }
        });

        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"+contactPhone));
                startActivity(sendIntent);
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"+contactEmail));
                startActivity(emailIntent);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent targetIntent = new Intent(getApplicationContext(), EditContactActivity.class);
                targetIntent.putExtra("key",currentContact);
                targetIntent.putExtra("name", contactName);
                targetIntent.putExtra("phone", contactPhone);
                targetIntent.putExtra("email", contactEmail);

                targetIntent.putExtra("company", contactCompany);

                targetIntent.putExtra("photoUri", contactPhoto);

                targetIntent.putExtra("lat", lat);
                targetIntent.putExtra("lng", lng);
                targetIntent.putExtra("address", contactAddress);


                startActivity(targetIntent);
            }
        });
    }


    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataHeader2 = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listDataChild2 = new HashMap<String, List<String>>();
        agendaList = new ArrayList<String>();

        // Adding child data
        listDataHeader.add("Agendas ");
        listDataHeader2.add("Referred Contacts");

        final List<String> contacts = new ArrayList<String>();
        final List<String> agendas = new ArrayList<String >();



        contactQuery = contactDatabase.child(currentContact).child("referenceList").orderByChild("refName");

        contactDatabase.child(currentContact).child("agendaList")
                .orderByChild("agendaKey").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String entryKey = dataSnapshot.getKey();

                agendaDatabase.child(dataSnapshot.child("agendaKey").getValue(String.class))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    agendaTitle = dataSnapshot.child("title").getValue(String.class);
                                    agendaKey = dataSnapshot.getKey();
                                    agendaMap.put(agendaTitle, agendaKey);
                                    agendas.add(agendaTitle);
                                }

                                else {
                                    contactDatabase.child(currentContact).child("agendaList").child(entryKey).removeValue();
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


        Query newQ = contactDatabase.orderByChild("referenceKey").equalTo(currentContact);
        newQ.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    refContactName = dataSnapshot.child("name").getValue(String.class);
                    conKey = dataSnapshot.getKey();
                    contactMap.put(refContactName, conKey);
                    contacts.add(refContactName);

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

        agendas.add("+ Create New");

        contacts.add("+ Create New");

        listDataChild.put(listDataHeader.get(0), agendas);
        listDataChild2.put(listDataHeader2.get(0), contacts);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

        contactDatabase.child(currentContact).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lat = dataSnapshot.child("selectLatitude").getValue(Double.class);
                lng = dataSnapshot.child("selectLongitude").getValue(Double.class);
                contactAddress = dataSnapshot.child("selectedPlaceAdd").getValue(String.class);
                if(lat!=null && lng!=null) {

                    latLng = new LatLng(lat, lng);
                    mapLayout.setVisibility(View.VISIBLE);
                    //Toast.makeText(getApplicationContext(), lat.toString()+lng, Toast.LENGTH_LONG).show();

                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                            .title(contactName).snippet(contactPhone));
                    hashMapContact.put(marker, currentContact);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10.0f));
                    markerMap.put(contactName, marker);
                    markerMap1.put(contactPhone, marker);
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

    @Override
    public void  onBackPressed(){
        if (intent!=null){
            Intent intentAct = new Intent(this,HomeActivity.class);
            startActivity(intentAct);
        }
        else{
            super.onBackPressed();
        }
    }

    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    }
