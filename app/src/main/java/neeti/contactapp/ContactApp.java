package neeti.contactapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Darshan on 06-02-2017.
 */

public class ContactApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        if(!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //FirebaseApp.initializeApp(this);
    }
}
