package neeti.contactapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
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

import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener{

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private DatabaseReference mDatabase;
    private DatabaseReference rDatabase;
    FirebaseUser user;
    List<String> names;

    MultiSelectionSpinner multiSelectionSpinner;
    Spinner contactSpinner;
    EditText contactName;
    EditText contactPhone;
    EditText contactEmail;
    EditText contactCompany;

    //permission constants
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 2;

    Button selectImage;
    ImageView dPhoto;

    String selectedPlace = null;
    String selectedPlaceAdd = null;
    double selectLatitude;
    double selectLongitude;

    private double MyLat;
    private double MyLong;

    Uri uri;//To store image Uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        contactName = (EditText) findViewById(R.id.contact_name);
        contactPhone = (EditText) findViewById(R.id.contact_phone);
        contactCompany = (EditText) findViewById(R.id.contact_company);
        contactEmail = (EditText) findViewById(R.id.contact_email);
        contactSpinner = (Spinner) findViewById(R.id.mySpinner);

        user = FirebaseAuth.getInstance().getCurrentUser();
        rDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");
        Query query = rDatabase.orderByChild("name");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");

       // multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.mySpinner);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<String>();
                names.add("NONE");
                for(DataSnapshot nameSnapshot: dataSnapshot.getChildren()){
                    String contactName = nameSnapshot.child("name").getValue(String.class);
                    names.add(contactName);
                }
                ArrayAdapter<String> contactAdapter = new ArrayAdapter<String>(AddContactActivity.this, android.R.layout.simple_spinner_item, names);
                contactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                contactSpinner.setAdapter(contactAdapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {


            if (TextUtils.isEmpty(contactName.getText())) {
                Toast.makeText(getApplication(), "Please enter an agenda title.", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(contactPhone.getText())) {
                Toast.makeText(getApplication(), "Please enter some description", Toast.LENGTH_SHORT).show();
            }


            else {
                DatabaseReference newContact = mDatabase.push();

                newContact.child("name").setValue(contactName.getText().toString());
                newContact.child("phone").setValue(contactPhone.getText().toString());
                if (selectedPlace!=null){
                    newContact.child("selectedPlace").setValue(selectedPlace);
                    newContact.child("selectedPlaceAdd").setValue(selectedPlaceAdd);
                    newContact.child("selectLatitude").setValue(selectLatitude);
                    newContact.child("selectLongitude").setValue(selectLongitude);
                }
                if (contactSpinner.getSelectedItem()!=null){
                    newContact.child("reference").setValue(contactSpinner.getSelectedItem().toString());
                }
                if (contactEmail.getText()!=null){
                    newContact.child("email").setValue(contactEmail.getText().toString());
                }
                if (contactCompany.getText()!=null){
                    newContact.child("company").setValue(contactCompany.getText().toString());
                }

                Intent intent = new Intent(this,HomeActivity.class);
                intent.putExtra("fragmentValue", 1); //for example
                startActivity(intent);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(AddContactActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
