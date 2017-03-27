package layout;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import neeti.contactapp.ContactList;
import neeti.contactapp.HomeActivity;
import neeti.contactapp.R;
import neeti.contactapp.TabFragment;

/**
 * Created by Darshan on 27-03-2017.
 */
public class Toolbar_ActionMode_Callback implements ActionMode.Callback {

    private Context context;
    private FirebaseAdapter recyclerView_adapter;
    private ArrayList<ContactList> message_models;
    private boolean isListViewFragment;


    public Toolbar_ActionMode_Callback(Context context, FirebaseAdapter recyclerView_adapter, ArrayList<ContactList> message_models) {
        this.context = context;
        this.recyclerView_adapter = recyclerView_adapter;
        this.message_models = message_models;
        this.isListViewFragment = isListViewFragment;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu according to SDK Levels
        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }



    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {



                    Fragment recyclerFragment = new ContactFragment();//Get recycler view fragment
                    if (recyclerFragment != null)
                        //If recycler fragment not null
                        ((ContactFragment) recyclerFragment).deleteRows();//delete selected rows



        return false;
    }



    @Override
    public void onDestroyActionMode(ActionMode mode) {

        //When action mode destroyed remove selected selections and set action mode to null
        //First check current fragment action mode

            recyclerView_adapter.removeSelection();  // remove selection
            Fragment recyclerFragment = new ContactFragment();//Get recycler fragment
            if (recyclerFragment != null)
                ((ContactFragment) recyclerFragment).setNullToActionMode();//Set action mode null

    }
}