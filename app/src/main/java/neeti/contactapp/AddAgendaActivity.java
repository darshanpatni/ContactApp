package neeti.contactapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.craft.libraries.firebaseuiaddon.FirebaseSpinnerAdapter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddAgendaActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener{

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
    EditText title;
    EditText description;
    EditText datePick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_agenda);
        datePick = (EditText) findViewById(R.id.datePicker);
        myCalendar = Calendar.getInstance();

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
                for(DataSnapshot nameSnapshot: dataSnapshot.getChildren()){
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

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
             //   Toast.makeText(this, , Toast.LENGTH_LONG).show();
              //  Log.i(TAG, "Place: " + place.getName());
                selectedPlace = place.getName().toString();
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
                DatabaseReference newAgenda = mDatabase.push();
                newAgenda.child("selectedPlace").setValue(selectedPlace);
                newAgenda.child("date").setValue(datePick.getText().toString());
                newAgenda.child("title").setValue(agendaTitle);
                newAgenda.child("description").setValue(agendaDescription);
                newAgenda.child("contacts").setValue(multiSelectionSpinner.getSelectedStrings());

                Intent intent = new Intent(this,HomeActivity.class);
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

}
