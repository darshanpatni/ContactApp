package neeti.contactapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static android.Manifest.permission.READ_CONTACTS;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Recycler Contact list variables
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    //Firebase Variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

   //Request Contact Constant
    private static final int REQUEST_READ_CONTACTS = 0;

    //Menu button variable
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            mayRequestContacts();
        }



        mStorageRef = FirebaseStorage.getInstance().getReference();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View HeadView =  navigationView.getHeaderView(0);
        TextView uName = (TextView)HeadView.findViewById(R.id.userName);
        ImageView dPhoto = (ImageView)HeadView.findViewById(R.id.imageView);
        TextView mEmail = (TextView)HeadView.findViewById(R.id.uEmail);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();


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
            String name = user.getDisplayName();
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
                        .into(dPhoto);
            }

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.

        }

        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        if (id == R.id.action_settings) {
            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

                        // Here you should write your time consuming task...

                        // Let the progress ring for 10 seconds...
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        String Uid = user.getUid();
                        String UName = user.getDisplayName();

                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null);
                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {

                                //Import Contact ID

                                Long contactId=cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
                                mDatabase.child("users").child(Uid).child(UName).child("contacts").child("ContactID").setValue(contactId);

                                //Import Contact Name
                                String cName = cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME));
                                mDatabase.child("users").child(Uid).child(UName).child("contacts").child(contactId.toString()).child("Name").setValue(cName);


                                //Import Contact photo



                                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

                                Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
                                try {
                                    AssetFileDescriptor fd =
                                            getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                                    // return fd.createInputStream();
                                } catch (IOException e) {
                                    // return null;
                                }

                                StorageReference filepath = mStorageRef.child("users/"+Uid+"/"+contactId+"/contactPhoto");
                                filepath.putFile(displayPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        // System.out.println(photoUri.toString());

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


                                //Import Contact EmailID

                                Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] {contactId.toString()}, null);

                                while (emailCursor.moveToNext())
                                {
                                    String cEmail = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                    int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                    //  String cEmail = (String) ContactsContract.CommonDataKinds.Email.getTypeLabel(getResources(), , "");
                                    mDatabase.child("users").child(Uid).child(UName).child("contacts").child(contactId.toString()).child("Email").setValue(cEmail);
                                }

                                emailCursor.close();

                                //Import Contact Phone No.
                                if (cur.getInt(cur.getColumnIndex(
                                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                    Cursor pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                            new String[]{contactId.toString()}, null);
                                    while (pCur.moveToNext()) {
                                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                            /*Toast.makeText(NativeContentProvider.this, "Name: " + name
                                    + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();*/
                                        mDatabase.child("users").child(Uid).child(UName).child("contacts").child(contactId.toString()).child("Phone").setValue(phoneNo);
                                    }
                                    pCur.close();

                                }
                            }
                        }

                        Thread.sleep(10000);

                    } catch (Exception e) {



                    }

                    ringProgressDialog.dismiss();

                }

            }).start();




        } else if (id == R.id.nav_logout) {

            mAuth.signOut();

            //startActivity(new Intent(HomeActivity.this, LoginActivity.class));

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

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
}
