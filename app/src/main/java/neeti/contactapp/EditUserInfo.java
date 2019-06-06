package neeti.contactapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class EditUserInfo extends AppCompatActivity {

    EditText userName;
    EditText userEmail;
    EditText newPassword;
    EditText newPasswordAgain;
    ImageView userPhoto;
    Button saveBtn;
    Button cancelBtn;

    String currentName;
    String name;
    String email;
    String newPass;
    String newPassAgain;
    String uID;
    Uri photoUrl;
    Uri uri;

    FirebaseUser user;
    private StorageReference mStorageRef;
    private ProgressBar progressBar;
    DatabaseReference fromPath;
    DatabaseReference toPath;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //permission constants
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 2;


    /**
     *
     * @param savedInstanceState
     */
    /*
    •	Initialize UI elements.
    •	Initialize Firebase variables (User, Database and Authentication State).
    •	Set button interactions.

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);
        getSupportActionBar().setTitle("Edit Profile");
        userName = (EditText) findViewById(R.id.name);
        newPassword = (EditText) findViewById(R.id.newPassword);
        newPasswordAgain = (EditText) findViewById(R.id.newPasswordAgain);
        userPhoto = (ImageView) findViewById(R.id.contactPhoto);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        progressBar = (ProgressBar) findViewById(R.id.progress_Bar);
        userEmail = (EditText) findViewById(R.id.email);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity

                    startActivity(new Intent(EditUserInfo.this, LoginActivity.class));
                    finish();
                }
            }
        };

        uID = user.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference();//Storage Reference variable
        fromPath = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid()).child(user.getDisplayName());

        toPath = FirebaseDatabase.getInstance().getReference().child("users")
                .child(user.getUid());

        currentName = user.getDisplayName();
        name = user.getDisplayName();
        photoUrl = user.getPhotoUrl();
        uri = photoUrl;

        email = user.getEmail();

        userName.setText(name);
        userEmail.setText(email);

        if(photoUrl!=null){


            Picasso.with(this)
                    .load(photoUrl)
                    .transform(new CircleTransform())
                    .into(userPhoto);
        }

        else{
            Picasso.with(this)
                    .load(R.drawable.ic_default_photo)
                    .transform(new CircleTransform())
                    .into(userPhoto);
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPass = newPassword.getText().toString();
                newPassAgain = newPasswordAgain.getText().toString();


                if(!newPass.isEmpty() || !newPassAgain.isEmpty()){

                    if (newPass==newPassAgain) {
                        String newPassword = newPassAgain;

                        user.updatePassword(newPassword)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplication(), "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    else {
                        Toast.makeText(getApplication(), "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }

                }

                if (photoUrl!=uri) {
                    StorageReference filepath = mStorageRef.child("users/" + uID + "/profilePhoto");
                    filepath.putFile(photoUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // System.out.println(photoUri.toString());
                            Toast.makeText(EditUserInfo.this, "Upload Successful",
                                    Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()

                            .setPhotoUri(Uri.parse(uri.toString()))
                            .build();
                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {


                                        Toast.makeText(EditUserInfo.this, "Success!",
                                                Toast.LENGTH_LONG).show();

                                        startActivity(new Intent(EditUserInfo.this, HomeActivity.class));
                                        finish();
                                    }
                                }

                            });

                    if (userName.getText().toString()!=currentName){

                        profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName.getText().toString())
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            moveFirebaseRecords();


                                        }
                                    }

                                });

                    }

                    user.updateEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        mAuth.signOut();
                                        startActivity(new Intent(EditUserInfo.this, HomeActivity.class));
                                        finish();
                                    }
                                }
                            });
                }
                else{
                    if (userName.getText().toString()!=currentName) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName.getText().toString())
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            moveFirebaseRecords();

                                        }
                                    }

                                });
                    }
                    user.updateEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(EditUserInfo.this, "Success",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                        startActivity(new Intent(EditUserInfo.this, HomeActivity.class));
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            mayRequestExternalStorage();
        }
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditUserInfo.this, HomeActivity.class));
                finish();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            user = FirebaseAuth.getInstance().getCurrentUser();
            uri = data.getData();

            Picasso.with(this)
                    .load(uri)
                    .transform(new CircleTransform())
                    .into(userPhoto);

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
                Toast.makeText(EditUserInfo.this, "Permission Granted",
                        Toast.LENGTH_LONG).show();
                //File write logic here
                return true;
            }
        } else {

            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    /*Move data to new node (This node depends on Display name).
     */
    public  void  moveFirebaseRecords(){
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.child(userName.getText().toString()).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null)
                        {
                            Toast.makeText(EditUserInfo.this, "Copy Failed",
                                    Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                            Toast.makeText(EditUserInfo.this, "Copy Success",
                                    Toast.LENGTH_LONG).show();
                            fromPath.removeValue();

                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Copy failed");
            }
        });

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
}
