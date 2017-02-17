package neeti.contactapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class AddContactActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        TextView textView = (TextView) findViewById(R.id.contact_name);
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(AddContactActivity.this, HomeActivity.class));
        finish();
    }
}
