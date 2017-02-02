package neeti.contactapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    //Declare UI elements
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView,inputEmail;
    private ProgressBar progressBar;

    //Declare Firebase elements
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Initialize UI elements
        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progress_Bar);
        inputEmail= (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button mEmailRegisterButton = (Button) findViewById(R.id.sign_up_button);
        mEmailRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        mAuth = FirebaseAuth.getInstance();

    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public void registerUser(){

        String email= inputEmail.getText().toString().trim();
        System.out.println(email);
        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplication(), "Please enter a valid email id", Toast.LENGTH_SHORT).show();
            return;
        }

       
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            Toast.makeText(getApplication(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for a valid email address
        // .
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplication(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
            return;

        }
        else if (!isEmailValid(email)) {
            Toast.makeText(getApplication(), "Please enter a valid email id", Toast.LENGTH_SHORT).show();
            return;
        }

       /* if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }*/

        //String email = mEmailView.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                       // Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:"+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        //progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUpActivity.this, "Registration failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUpActivity.this, "Registration Successful.",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUpActivity.this, AddUserInfo.class));
                            finish();
                        }

                        // ...
                    }
                });
    }
}