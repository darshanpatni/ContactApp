package neeti.contactapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AddUserInfo extends AppCompatActivity {

    //firebase variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressBar progressBar;
    private StorageReference mStorageRef;
    FirebaseUser user;
    boolean doubleBackToExitPressedOnce = false;

    //permission constants
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 2;

    //Variables to reference UI elements
    EditText dName;
    Button setUserInfo;
    Button selectImage;
    ImageView dPhoto;

    Uri uri = null;//To store image Uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_info);

        //initialize UI elements
        dName = (EditText) findViewById(R.id.Name);
        setUserInfo = (Button) findViewById(R.id.save_button);
        selectImage = (Button) findViewById(R.id.imagePick_button);
        dPhoto = (ImageView) findViewById(R.id.DisplayPhoto);

        user = FirebaseAuth.getInstance().getCurrentUser(); //gets current user
        mStorageRef = FirebaseStorage.getInstance().getReference();//Storage Reference variable

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        setUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_Bar);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity

                    startActivity(new Intent(AddUserInfo.this, LoginActivity.class));
                    finish();
                }
            }
        };

        //if SDK or android version marshmello get permission to access external storeage
        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            mayRequestExternalStorage();
        }

        if (user != null) {
            // Name and profile photo Url
            String name = user.getDisplayName();

            Uri photoUrl = user.getPhotoUrl();


            if(name!=null) {
                dName.setText(name, TextView.BufferType.EDITABLE);
            }
            if(photoUrl!=null){

                dPhoto.setImageURI(photoUrl);

            }

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.

        }

    }

    //Help AuthStateListner track when user signs in and out
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

    //Save User Display Name and Display Photo
    public void Save() {

        String NewName = dName.getText().toString();

        if (NewName != null) {

            progressBar.setVisibility(View.VISIBLE);//Set progress bar visible

            if (uri != null) {

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(NewName)
                    .setPhotoUri(Uri.parse(uri.toString()))
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddUserInfo.this, "Registration completed. Please Login!",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                startActivity(new Intent(AddUserInfo.this, LoginActivity.class));
                                finish();
                            }
                        }

                    });
            }
            else{
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(NewName)
                        .build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(AddUserInfo.this, "Registration completed. Please Login!",
                                            Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    startActivity(new Intent(AddUserInfo.this, LoginActivity.class));
                                    finish();
                                }
                            }

                        });
            }
        }
        else{

            Toast.makeText(AddUserInfo.this, "Please enter your name.",
                    Toast.LENGTH_LONG).show();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            user = FirebaseAuth.getInstance().getCurrentUser();
            dPhoto = (ImageView) findViewById(R.id.DisplayPhoto);
            String Uid = user.getUid();
            uri = data.getData();

            Picasso.with(this)
                    .load(uri)
                    .transform(new CircleTransform())
                    .into(dPhoto);
            StorageReference filepath = mStorageRef.child("users/"+Uid+"/profilePhoto");
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(AddUserInfo.this, "Upload Successful",
                            Toast.LENGTH_LONG).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }


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
                Toast.makeText(AddUserInfo.this, "Permission Granted",
                        Toast.LENGTH_LONG).show();
                //File write logic here
                return true;
            }
        } else {

            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
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
