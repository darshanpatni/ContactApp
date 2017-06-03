package neeti.contactapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.provider.ContactsContract;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import java.io.IOException;

import layout.AgendaFragment;
import neeti.contactapp.Services.LocationService;

import static android.Manifest.permission.READ_CONTACTS;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    //Firebase Variables
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference rDatabase;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    //Request Contact Constant
    private static final int REQUEST_READ_CONTACTS = 0;

    private Long contactId;
    private Uri downloadUrl = null;
    GoogleSignInOptions gso;
    private static long back_pressed_time;
    private static long PERIOD = 2000;
    boolean doubleBackToExitPressedOnce = false;

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        Intent intent = new Intent(HomeActivity.this, LocationService.class);
        this.startService(intent);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            mayRequestContacts();
        }


       //initialize UI elements
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headView =  navigationView.getHeaderView(0);
        TextView uName = (TextView)headView.findViewById(R.id.userName);
        ImageView dPhoto = (ImageView)headView.findViewById(R.id.imageView);
        ImageView editBtn = (ImageView)headView.findViewById(R.id.edit);
        TextView mEmail = (TextView)headView.findViewById(R.id.uEmail);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, EditUserInfo.class));

            }
        });

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        if(getIntent().getIntExtra("fragmentNumber",0)==1){
            //set the desired fragment as current fragment to fragment pager
            System.out.println("YES!");
            mFragmentTransaction.replace(R.id.containerView,new AgendaFragment()).commit();

        }
        System.out.println("YES!");
        mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();



        //initialize Firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        if (user != null) {
            // Name, email address, and profile photo Url
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    user = mAuth.getCurrentUser();
                }
            });
            rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");
            rDatabase.keepSynced(true);
            Query query = rDatabase.orderByChild("name");
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(user.getUid()).child(user.getDisplayName());
            name = null;
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("displayName").getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            if(name==null) {
                name = user.getDisplayName();
            }
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            System.out.println(name);
            if(name!=null) {
                uName.setText(name);
                mEmail.setText(email);
            }
            if(photoUrl!=null){


                Picasso.with(this)
                        .load(photoUrl)
                        .transform(new CircleTransform())
                        .into(dPhoto);
            }

            else{
                Picasso.with(this)
                        .load(R.drawable.ic_default_photo)
                        .transform(new CircleTransform())
                        .into(dPhoto);
            }

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.

        }


        setSupportActionBar(toolbar);   //instantiate toolbar

        //initialize floating action button
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(HomeActivity.this, fab);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        System.out.println(item.getTitle().toString());
                        if(item.getItemId()==R.id.contact){
                            startActivity(new Intent(HomeActivity.this, AddContactActivity.class));
                            finish();
                            return true;
                        }
                        if(item.getItemId()==R.id.agenda){
                            startActivity(new Intent(HomeActivity.this, AddAgendaActivity.class));
                            finish();
                            return true;
                        }
                        Toast.makeText(HomeActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
                /*Snackbar.make(view, "Replace with add contact or agenda", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

        });


        //initialize floating action button
        final FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(HomeActivity.this, fab1);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu1, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        System.out.println(item.getTitle().toString());
                        if(item.getItemId()==R.id.exploreContact){
                            startActivity(new Intent(HomeActivity.this, ContactMapActivity.class));
                            finish();
                            return true;
                        }
                        if(item.getItemId()==R.id.exploreAgenda){
                            startActivity(new Intent(HomeActivity.this, AgendaMapActivity.class));
                            finish();
                            return true;
                        }
                        Toast.makeText(HomeActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
                /*Snackbar.make(view, "Replace with add contact or agenda", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

        });


        //initialize navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


        navigationView.setNavigationItemSelectedListener(this); //listen if any item selected




    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/



        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    public void setBackgroundColor(Drawable d){
        getSupportActionBar().setBackgroundDrawable(d);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_importContacts) {
            // Handle the Import Contacts Action

            final ProgressDialog ringProgressDialog = ProgressDialog.show(HomeActivity.this, "Please Wait", "Importing Contacts", true);

            ringProgressDialog.setCancelable(true);

            new Thread(new Runnable() {

                @Override

                public void run() {

                    try {


                        //user = FirebaseAuth.getInstance().getCurrentUser(); //Get the currently logged in user information
                        //Uid = user.getUid();    //Get User ID
                        //UName = user.getDisplayName();

                        //Initialize cursor to import contacts
                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null);

                        assert cur != null;
                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {

                                //Get current Contact ID
                                contactId=cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));

                                //Get current Contact Name
                                String cName = cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME));

                                //Get Current Contact URI
                                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

                                //Get Current Contact Photo Uri
                                Uri displayPhotoUri=null;
                                if(Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)!=null){
                                    displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

                                }
                               try {
                                    AssetFileDescriptor fd =
                                            getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");

                                } catch (IOException e) {
                                    // return null;
                                }

                                if(contactId!=null && cName!=null && displayPhotoUri!=null){

                                    boolean flag = true;

                                    Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] {contactId.toString()}, null);

                                    if(cur.getInt(cur.getColumnIndex(
                                            ContactsContract.Contacts.HAS_PHONE_NUMBER))  <=0){
                                        flag = false;
                                    }
                                    else{



                                        //Import Contact Email ID if Any
                                        assert emailCursor != null;
                                        while (emailCursor.moveToNext())
                                        {
                                            String cEmail = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                            int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                            rDatabase.child(contactId.toString()).child("email").setValue(cEmail);
                                        }
                                        emailCursor.close();

                                        //Import Contact Name

                                        rDatabase.child(contactId.toString()).child("city").setValue(null);
                                        rDatabase.child(contactId.toString()).child("name").setValue(cName);
                                        rDatabase.child(contactId.toString()).child("lowName").setValue(cName.toLowerCase());

                                        Cursor pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                            new String[]{contactId.toString()}, null);
                                        int pCount=1;
                                        while (pCur.moveToNext()) {
                                        /*String phoneNo = pCur.getString(pCur.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));*/
                                        //Import Contact Phone No.
                                            int phoneType 		= pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                            //String isStarred 		= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
                                            String phoneNo 	= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                            //you will get all phone numbers according to it's type as below switch case.


                                            switch (phoneType)
                                            {
                                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                                    rDatabase.child(contactId.toString()).child("phone").setValue(phoneNo);
                                                    //Import Contact Photo

                                                    StorageReference filepath = mStorageRef.child("users/"+user.getUid()+"/"+phoneNo+"/contactPhoto");
                                                    filepath.putFile(displayPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {


                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                            // System.out.println(photoUri.toString()); //debug point
                                                            @SuppressWarnings("VisibleForTests")
                                                           String downloadUrl =  taskSnapshot.getDownloadUrl().toString();
                                               /* downloadUrl = taskSnapshot.getDownloadUrl();*/
                                                rDatabase.child(contactId.toString()).child("photoUrl").setValue(downloadUrl);


                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                                    break;
                                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                                    rDatabase.child(contactId.toString()).child("phoneHome").setValue(phoneNo);
                                                    break;
                                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                                    rDatabase.child(contactId.toString()).child("phoneWork").setValue(phoneNo);
                                                    break;
                                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                                                    rDatabase.child(contactId.toString()).child("phoneWorkMobile").setValue(phoneNo);
                                                    break;
                                                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                                    rDatabase.child(contactId.toString()).child("phoneOther").setValue(phoneNo);
                                                    break;
                                                default:
                                                    rDatabase.child(contactId.toString()).child("phone").setValue(phoneNo);
                                                    break;
                                            }


                                            /*if(pCount>1){
                                                rDatabase.child(contactId.toString()).child("phone_"+(pCount)).setValue(phoneNo);
                                            }else{
                                        rDatabase.child(contactId.toString()).child("phone").setValue(phoneNo);
                                            }
                                            pCount = pCount+1;*/
                                        }
                                    pCur.close();

                                }}

                            }
                        }   cur.close();

                        Thread.sleep(10000);

                    } catch (Exception e) {

                        return;

                    }

                    ringProgressDialog.dismiss();

                }

            }).start();




        } else if (id == R.id.nav_logout) {


            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener(){

                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult){

                        }

                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                    .build();
            mGoogleApiClient.connect();
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {

                    FirebaseAuth.getInstance().signOut();
                    if(mGoogleApiClient.isConnected()) {
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });

            /*Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    mAuth.signOut();
                }
            });*/
            mAuth.signOut();    //Log out user method


        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Method to request contacts (Required for API 23 and greater)
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(HomeActivity.this, "Permission Granted",
                        Toast.LENGTH_LONG).show();
                //File write logic here
                return true;
            }
        } else {

            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener); //Listen to current user Login status
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
