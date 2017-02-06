package neeti.contactapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Darshan on 06-02-2017.
 */

public class ContactApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
