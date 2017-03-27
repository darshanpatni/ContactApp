package neeti.contactapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.craft.libraries.firebaseuiaddon.FirebaseSpinnerAdapter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.common.api.GoogleApiClient;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import layout.ContactFragment;
import neeti.contactapp.MultiSelectionSpinner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddAgendaActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private DatabaseReference mDatabase;
    private DatabaseReference rDatabase;
    FirebaseUser user;
    List<String> names;
    boolean doubleBackToExitPressedOnce = false;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    MultiSelectionSpinner multiSelectionSpinner;
    String selectedPlace = null;
    String selectedPlaceAdd = null;
    double selectLatitude;
    double selectLongitude;
    EditText title;
    EditText description;
    EditText datePick;
    Button getCurrentLocation;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private double MyLat;
    private double MyLong;

    PlaceAutocompleteFragment autocompleteFragment;

    protected LocationManager locationManager;

    private LocationListener listener;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_agenda);
        datePick = (EditText) findViewById(R.id.datePicker);
        myCalendar = Calendar.getInstance();
        getCurrentLocation = (Button) findViewById(R.id.buttonGPS);
        title = (EditText) findViewById(R.id.agenda_title);
        description = (EditText) findViewById(R.id.agenda_description);

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
                new DatePickerDialog(AddAgendaActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        rDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");
        Query query = rDatabase.orderByChild("name");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("agenda");


        multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.mySpinner);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<String>();
                names.add("NONE");
                for (DataSnapshot nameSnapshot : dataSnapshot.getChildren()) {
                    String contactName = nameSnapshot.child("name").getValue(String.class);
                    names.add(contactName);
                }
                multiSelectionSpinner.setItems(names);

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


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //  Log.i(TAG, "An error occurred: " + status);
            }
        });

     /*   try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
*/







        // Create an instance of GoogleAPIClient.
       /* if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks((ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((OnConnectionFailedListener) this)
                    .build();
        }*/
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        ;
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                System.out.println("ABCD "+selectedPlace);

                MyLat = location.getLatitude();
                MyLong = location.getLongitude();

                System.out.println(MyLat+" "+MyLong);
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(MyLat, MyLong, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                selectedPlace = addresses.get(0).getAddressLine(0);
                    selectLatitude = MyLat;
                    selectLongitude = MyLong;
                    autocompleteFragment.setText(selectedPlace);



            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        getCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            String agendaTitle = title.getText().toString();
            String agendaDescription = description.getText().toString();

            if (TextUtils.isEmpty(agendaTitle)) {
                Toast.makeText(getApplication(), "Please enter an agenda title.", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(agendaDescription)) {
                Toast.makeText(getApplication(), "Please enter some description", Toast.LENGTH_SHORT).show();
            } else if (date == null) {
                Toast.makeText(getApplication(), "Please select a date", Toast.LENGTH_SHORT).show();
            } else if (selectedPlace == null) {
                Toast.makeText(getApplication(), "Please enter a place", Toast.LENGTH_SHORT).show();
            } else {
                DatabaseReference newAgenda = mDatabase.push();
                newAgenda.child("selectedPlace").setValue(selectedPlace);
                newAgenda.child("selectedPlaceAdd").setValue(selectedPlaceAdd);
                newAgenda.child("selectLatitude").setValue(selectLatitude);
                newAgenda.child("selectLongitude").setValue(selectLongitude);
                newAgenda.child("date").setValue(datePick.getText().toString());
                newAgenda.child("title").setValue(agendaTitle);
                newAgenda.child("description").setValue(agendaDescription);
                newAgenda.child("contacts").setValue(multiSelectionSpinner.getSelectedStrings());

                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("fragmentValue", 1); //for example
                startActivity(intent);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        Toast.makeText(this, strings.toString(), Toast.LENGTH_LONG).show();
    }

    private void updateLabel() {

        EditText datePick = (EditText) findViewById(R.id.datePicker);
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        datePick.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(AddAgendaActivity.this, HomeActivity.class));
        finish();
    }

   /* protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
*/
    /*public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            MyLat = mLastLocation.getLatitude();
            MyLong = mLastLocation.getLongitude();
            try {
                List<Address> addresses = geocoder.getFromLocation(MyLat, MyLong, 1);
                selectedPlace = addresses.get(0).getAddressLine(0);
                selectLatitude = MyLat;
                selectLongitude = MyLong;
                autocompleteFragment.setText(selectedPlace);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
