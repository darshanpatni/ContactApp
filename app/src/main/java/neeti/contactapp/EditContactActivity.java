package neeti.contactapp;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditContactActivity extends AppCompatActivity {


    FirebaseUser user;
    String currentContact;
    private StorageReference mStorageRef;

    private static final int GALLERY_INTENT = 2;

    String contactName;
    String contactPhone;
    String contactEmail;
    String contactCompany;
    String contactPhoto;
    String contactAddress;

    List<String> names;


    Double lat, lng;

    String Uid;
    Spinner contactSpinner;
    HashMap <String, String>  ref = new HashMap<>();

    ImageView contactPhotoImg;

    EditText contactNameEdit;
    EditText contactPhoneEdit;
    EditText contactEmailEdit;
    EditText contactCompanyEdit;
    FloatingActionButton selectImg;


    String selectedPlace = null;
    String selectedPlaceAdd = null;

    String city = null;

    double selectLatitude;
    double selectLongitude;


    Uri uri = null;//To store image Uri

    private DatabaseReference contactDatabase;


    /**
     *
     * @param savedInstanceState
     */
    /*
    •	Initialize UI elements.
    •	Set button interactions.
    •	Get selected contact key and other relevant details from intent.
    •	Fill editable UI elements with relevant information.

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        contactNameEdit = (EditText) findViewById(R.id.contact_name);
        contactPhoneEdit = (EditText) findViewById(R.id.contact_phone);
        contactCompanyEdit = (EditText) findViewById(R.id.contact_company);
        contactEmailEdit = (EditText) findViewById(R.id.contact_email);
        contactSpinner = (Spinner) findViewById(R.id.mySpinner);
        contactPhotoImg = (ImageView) findViewById(R.id.contactPhoto);
        contactSpinner = (Spinner) findViewById(R.id.mySpinner);


        user = FirebaseAuth.getInstance().getCurrentUser();

        currentContact = getIntent().getStringExtra("key");
        contactName = getIntent().getStringExtra("name");
        contactEmail = getIntent().getStringExtra("email");
        contactPhone = getIntent().getStringExtra("phone");
        contactCompany = getIntent().getStringExtra("company");
        contactPhoto = getIntent().getStringExtra("photoUri");
        String latitude = getIntent().getStringExtra("lat");
        String longitude = getIntent().getStringExtra("lng");

        getSupportActionBar().setTitle("Edit: "+contactName);

        if(latitude!=null && longitude!=null){
            lat = Double.valueOf(latitude);
            lng = Double.valueOf(longitude);
            contactAddress = getIntent().getStringExtra("address");
        }

        contactNameEdit.setText(contactName);
        contactPhoneEdit.setText(contactPhone);
        contactEmailEdit.setText(contactEmail);

        selectImg = (FloatingActionButton) findViewById(R.id.fab);

        mStorageRef = FirebaseStorage.getInstance().getReference();//Storage Reference variable


        if(contactCompany!=null){
            contactCompanyEdit.setText(contactCompany);
        }

        if (contactPhoto != null) {
            Picasso.with(getApplicationContext())
                    .load(contactPhoto)
                    .transform(new CircleTransform())
                    .into(contactPhotoImg);
        } else {
            Picasso.with(getApplicationContext())
                    .load(R.drawable.ic_default_photo)
                    .transform(new CircleTransform())
                    .into(contactPhotoImg);
        }

        DatabaseReference rDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");
        Query query = rDatabase.orderByChild("lowName");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names = new ArrayList<String>();
                names.add("NONE");
                for (DataSnapshot nameSnapshot : dataSnapshot.getChildren()) {
                    String contactName = nameSnapshot.child("name").getValue(String.class);
                    ref.put(contactName, nameSnapshot.getKey());
                    names.add(contactName);
                }
                ArrayAdapter<String> contactAdapter = new ArrayAdapter<String>(EditContactActivity.this, android.R.layout.simple_spinner_item, names);
                contactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                contactSpinner.setAdapter(contactAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    /*
    •	Load selected image from gallery into imageview.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            contactPhotoImg = (ImageView) findViewById(R.id.contactPhoto);
            uri = data.getData();

            Picasso.with(this)
                    .load(uri)
                    .transform(new CircleTransform())

                    .into(contactPhotoImg);
        }

    }

    /**
     *
     * @param item
     * @return
     */
    /*
    •	Handle action bar item clicks here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        contactDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts").child(currentContact);;
        contactDatabase.keepSynced(true);

        // Creates a new Intent to insert a contact

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        // Sets the MIME type to match the Contacts Provider

        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {


            if (TextUtils.isEmpty(contactNameEdit.getText())) {
                Toast.makeText(getApplication(), "Please enter contact name.", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(contactPhoneEdit.getText())) {
                Toast.makeText(getApplication(), "Please enter phone no", Toast.LENGTH_SHORT).show();
            }


            else {

                contactDatabase.child("name").setValue(contactNameEdit.getText().toString());
                contactDatabase.child("lowName").setValue(contactNameEdit.getText().toString().toLowerCase());
                contactDatabase.child("phone").setValue(contactPhoneEdit.getText().toString());
                if (selectedPlace!=null){
                    contactDatabase.child("selectedPlace").setValue(selectedPlace);
                    contactDatabase.child("selectedPlaceAdd").setValue(selectedPlaceAdd);
                    contactDatabase.child("selectLatitude").setValue(selectLatitude);
                    contactDatabase.child("selectLongitude").setValue(selectLongitude);
                    contactDatabase.child("city").setValue(city);
                    contactDatabase.child("lowCity").setValue(city.toLowerCase());

                }
                if (contactSpinner.getSelectedItem()!="NONE"){
                    contactDatabase.child("reference").setValue(contactSpinner.getSelectedItem().toString());
                    contactDatabase.child("referenceKey").setValue(ref.get(contactSpinner.getSelectedItem().toString()));
                }
                if (contactEmailEdit.getText()!=null && contactEmailEdit.getText().toString()!=contactEmail){
                    contactDatabase.child("email").setValue(contactEmailEdit.getText().toString());
                }
                if (contactCompanyEdit.getText()!=null && contactCompanyEdit.getText().toString()!=contactCompany){
                    contactDatabase.child("company").setValue(contactCompanyEdit.getText().toString());
                }

                user = FirebaseAuth.getInstance().getCurrentUser();

                Uid = user.getUid();

                if(uri!=null){StorageReference filepath = mStorageRef.child("users/"+Uid+"/contacts/"+contactPhoneEdit.getText().toString()+"/contactPhoto");
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            contactDatabase.child("photoUrl").setValue(downloadUrl);
                            Toast.makeText(EditContactActivity.this, "Upload Successful",
                                    Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditContactActivity.this, "Upload unsuccessful",
                                    Toast.LENGTH_LONG).show();
                        }
                    });}

                Intent intentAct = new Intent(this,ContactInfoActivity.class);
                intentAct.putExtra("key", currentContact); //for example
                String intentAc = "edit";
                intentAct.putExtra("intent", intentAc);

                startActivity(intentAct);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param menu
     * @return
     */
    /*
    •	Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
