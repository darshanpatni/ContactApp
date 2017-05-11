package neeti.contactapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
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
import com.squareup.okhttp.Request;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class AddContactActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private DatabaseReference mDatabase;
    private DatabaseReference rDatabase;
    private StorageReference mStorageRef;
    FirebaseUser user;
    List<String> names;

    String Uid;
    MultiSelectionSpinner multiSelectionSpinner;
    Spinner contactSpinner;
    HashMap <String, String>  ref = new HashMap<>();
    EditText contactName;
    EditText contactPhone;
    EditText contactEmail;
    EditText contactCompany;
    ImageButton call;
    //permission constants
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 2;

    Button selectImage;
    ImageView contactPhoto;

    String selectedPlace = null;
    String selectedPlaceAdd = null;

    String city = null;

    double selectLatitude;
    double selectLongitude;

    private double MyLat;
    private double MyLong;

    FloatingActionButton fab;
    Uri uri = null;//To store image Uri
    String uriUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        contactName = (EditText) findViewById(R.id.contact_name);
        contactPhone = (EditText) findViewById(R.id.contact_phone);
        contactCompany = (EditText) findViewById(R.id.contact_company);
        contactEmail = (EditText) findViewById(R.id.contact_email);
        contactSpinner = (Spinner) findViewById(R.id.mySpinner);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        user = FirebaseAuth.getInstance().getCurrentUser();
        rDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");
        Query query = rDatabase.orderByChild("lowName");

        mStorageRef = FirebaseStorage.getInstance().getReference();//Storage Reference variable

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName()).child("contacts");

        // multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.mySpinner);

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
                //GetLocationDownloadTask getLocation = new GetLocationDownloadTask();
                //getLocation.execute(link);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //  Log.i(TAG, "An error occurred: " + status);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            mayRequestExternalStorage();
        }

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

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            contactPhoto = (ImageView) findViewById(R.id.contactPhoto);
            uri = data.getData();

            Picasso.with(this)
                    .load(uri)
                    .transform(new CircleTransform())

                    .into(contactPhoto);
        }
//.resize(60,60)
// .centerCrop()

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
                Toast.makeText(getApplication(), "Please enter contact name.", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(contactPhone.getText())) {
                Toast.makeText(getApplication(), "Please enter phone no", Toast.LENGTH_SHORT).show();
            }


            else {
                final DatabaseReference newContact = mDatabase.push();

                newContact.child("name").setValue(contactName.getText().toString());
                newContact.child("lowName").setValue(contactName.getText().toString().toLowerCase());
                newContact.child("phone").setValue(contactPhone.getText().toString());
                if (selectedPlace!=null){
                    newContact.child("selectedPlace").setValue(selectedPlace);
                    newContact.child("selectedPlaceAdd").setValue(selectedPlaceAdd);
                    newContact.child("selectLatitude").setValue(selectLatitude);
                    newContact.child("selectLongitude").setValue(selectLongitude);
                    newContact.child("city").setValue(city);
                    newContact.child("lowCity").setValue(city.toLowerCase());

                }
                if (contactSpinner.getSelectedItem()!="NONE"){
                    newContact.child("reference").setValue(contactSpinner.getSelectedItem().toString());
                    newContact.child("referenceKey").setValue(ref.get(contactSpinner.getSelectedItem().toString()));
                    //DatabaseReference refCon = rDatabase.child(ref.get(contactSpinner.getSelectedItem().toString())).child("referenceList").push();
                    //refCon.child("refName").setValue(contactName.getText().toString());
                   // refCon.child("key").setValue(newContact.getKey());
                }
                if (contactEmail.getText()!=null){
                    newContact.child("email").setValue(contactEmail.getText().toString());
                }
                if (contactCompany.getText()!=null){
                    newContact.child("company").setValue(contactCompany.getText().toString());
                }

                user = FirebaseAuth.getInstance().getCurrentUser();

                Uid = user.getUid();

                //String newPath = decodeFile(uriPath);

               // uri = Uri.parse(newPath);
                //uri  = Uri.parse(uriUpload);
                if(uri!=null){StorageReference filepath = mStorageRef.child("users/"+Uid+"/contacts/"+contactPhone.getText().toString()+"/contactPhoto");
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // System.out.println(photoUri.toString());
                            @SuppressWarnings("VisibleForTests")
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            newContact.child("photoUrl").setValue(downloadUrl);
                            Toast.makeText(AddContactActivity.this, "Upload Successful",
                                    Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddContactActivity.this, "Upload unsuccessful",
                                    Toast.LENGTH_LONG).show();
                        }
                    });}


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

    //Request permission to access External Storage (Required for API 23 or greater)
    private boolean mayRequestExternalStorage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddContactActivity.this, "Permission Granted",
                        Toast.LENGTH_LONG).show();
                //File write logic here
                return true;
            }
        } else {

            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    /*private String decodeFile(String path) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, 800, 800, ScalingUtilities.ScalingLogic.CROP);

            if (!(unscaledBitmap.getWidth() <= 800 && unscaledBitmap.getHeight() <= 800)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, 800, 800, ScalingUtilities.ScalingLogic.CROP);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file

            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/myTmpDir");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = "tmp.png";

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 70, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }*/

}
