package neeti.contactapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class EditAgendaActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener{

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private DatabaseReference mDatabase;
    private DatabaseReference rDatabase;
    FirebaseUser user;
    List<String> names;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    MultiSelectionSpinner multiSelectionSpinner;
    EditText title;
    EditText description;
    EditText datePick;

    String city;
    String entryKey;


    private HashMap<String, String> hashMapContact = new HashMap<String, String>();

    private HashMap<String, String> hashMapContact1 = new HashMap<String, String>();
    HashMap<String, String> contactMap = new HashMap<String, String>();
    String selectedPlace = null;
    String selectedPlaceAdd = null;
    double selectLatitude;
    double selectLongitude;
    List contactNames;

    String agendaTitle;
    String agendaDescription;
    String agendaDate;
    String currentAgenda;
    String contactKey;
    ArrayList<String> contactList;

    PlaceAutocompleteFragment autocompleteFragment;
    RatingBar ratingBar;

    float rating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_agenda);


        datePick = (EditText) findViewById(R.id.datePicker);
        myCalendar = Calendar.getInstance();
        title = (EditText) findViewById(R.id.agenda_title);
        description = (EditText) findViewById(R.id.agenda_description);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        datePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To show current date in the datepicker
                new DatePickerDialog(EditAgendaActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        user = FirebaseAuth.getInstance().getCurrentUser();

        currentAgenda = getIntent().getStringExtra("key");
        agendaTitle = getIntent().getStringExtra("title");
        agendaDescription = getIntent().getStringExtra("description");
        agendaDate = getIntent().getStringExtra("date");
        contactList = getIntent().getStringArrayListExtra("selectedContacts");
        String latitude = getIntent().getStringExtra("lat");
        String longitude = getIntent().getStringExtra("lng");
        selectLatitude = Double.parseDouble(latitude);
        selectLongitude = Double.parseDouble(longitude);
        selectedPlace = getIntent().getStringExtra("place");
        selectedPlaceAdd = getIntent().getStringExtra("address");
        rating = getIntent().getFloatExtra("rating", 0);

        getSupportActionBar().setTitle("Edit: "+agendaTitle);

        ratingBar.setRating(rating);
        Intent intent = getIntent();
        contactMap = (HashMap<String, String>)intent.getSerializableExtra("contactMap");


        datePick.setText(agendaDate);
        title.setText(agendaTitle);
        description.setText(agendaDescription);

        multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.mySpinner);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");
        Query query = mDatabase.orderByChild("lowName");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<String>();
                names.add("NONE");
                for(DataSnapshot nameSnapshot: dataSnapshot.getChildren()){
                    String contactName = nameSnapshot.child("name").getValue(String.class);
                    String id = nameSnapshot.getKey();
                    hashMapContact.put(contactName, id);
                    names.add(contactName);


                }

                multiSelectionSpinner.setItems(names);
                multiSelectionSpinner.setSelection(contactList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        multiSelectionSpinner.setListener(this);


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);



        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //   Toast.makeText(this, , Toast.LENGTH_LONG).show();
                //  Log.i(TAG, "Place: " + place.getName());
                selectedPlace = place.getName().toString();
                selectedPlaceAdd = place.getAddress().toString();

                LatLng selectedPlaceLatLng = place.getLatLng();

                selectLatitude = selectedPlaceLatLng.latitude;
                selectLongitude = selectedPlaceLatLng.longitude;

                String link = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+selectLatitude+","+selectLongitude+"&sensor=true";
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, link,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONArray jObj = new JSONObject(response).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                                    for (int i = 0; i < jObj.length(); i++) {
                                        String componentName = new JSONObject(jObj.getString(i)).getJSONArray("types").getString(0);
                                        if (componentName.equals("locality") || componentName.equals("administrative_area_level_2")) {
                                            city = new JSONObject(jObj.getString(i)).getString("long_name");
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


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //  Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragment.setText(selectedPlace);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                //  Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        rDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child(user.getDisplayName()).child("agenda").child(currentAgenda);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            String agendaTitle = title.getText().toString();
            String agendaDescription = description.getText().toString();

            if (TextUtils.isEmpty(agendaTitle)) {
                Toast.makeText(getApplication(), "Please enter an agenda title.", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(agendaDescription)) {
                Toast.makeText(getApplication(), "Please enter some description", Toast.LENGTH_SHORT).show();
            }
            else if(date==null){
                Toast.makeText(getApplication(), "Please select a date", Toast.LENGTH_SHORT).show();
            }
            else if(selectedPlace==null){
                Toast.makeText(getApplication(), "Please enter a place", Toast.LENGTH_SHORT).show();
            }

            else {
                rDatabase.child("selectedPlace").setValue(selectedPlace);
                rDatabase.child("selectedPlaceAdd").setValue(selectedPlaceAdd);
                rDatabase.child("selectLatitude").setValue(selectLatitude);
                rDatabase.child("selectLongitude").setValue(selectLongitude);
                rDatabase.child("date").setValue(datePick.getText().toString());
                rDatabase.child("title").setValue(agendaTitle);
                rDatabase.child("lowTitle").setValue(agendaTitle.toLowerCase());
                rDatabase.child("description").setValue(agendaDescription);
                rDatabase.child("city").setValue(city);
                rDatabase.child("rating").setValue(ratingBar.getRating());
                contactNames = multiSelectionSpinner.getSelectedStrings();

                for (int i  = 0; i < contactList.size(); i++){
                    if (!contactNames.contains(contactList.get(i))){
                        contactKey = contactMap.get(contactList.get(i));
                        mDatabase.child(contactKey).child("agendaList").orderByChild("agendaKey").equalTo(currentAgenda)
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        entryKey = dataSnapshot.getKey();
                                        mDatabase.child(contactKey).child("agendaList").child(entryKey).removeValue();
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

                        rDatabase.child("contacts").orderByChild("contactID").equalTo(contactKey).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                entryKey = dataSnapshot.getKey();
                                rDatabase.child("contacts").child(entryKey).removeValue();
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


                    }
                }
                for(int i=0; i<contactNames.size();i++){
                    if (!contactList.contains(contactNames.get(i))) {
                        String contactName = (String) contactNames.get(i);
                        String contactID = hashMapContact.get(contactName);

                        hashMapContact1.put(contactName, contactID);
                        if (contactName == "NONE") {
                            contactNames.remove(i);
                        } else {

                            DatabaseReference contact = rDatabase.child("contacts").push();
                            contact.child("contactName").setValue(contactName);
                            contact.child("contactID").setValue(contactID);
                            DatabaseReference addInContact = mDatabase.child(contactID).child("agendaList").push();
                            addInContact.child("title").setValue(agendaTitle);
                            addInContact.child("agendaKey").setValue(currentAgenda);


                        }
                    }
                }
                Intent intentAct = new Intent(this,AgendaInfoActivity.class);
                intentAct.putExtra("key", currentAgenda); //for example
                String intentAc = "edit";
                intentAct.putExtra("intent", intentAc);
                startActivity(intentAct);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }


    private void updateLabel() {

        EditText datePick = (EditText) findViewById(R.id.datePicker);
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        datePick.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {

    }
}
