package neeti.contactapp;

import android.util.SparseBooleanArray;

/**
 * Created by Darshan on 02-02-2017.
 */

//Adapter Class for Recycler View
public class ContactList {

    private String name;
    private String phone;
    private boolean isSelected;

    public ContactList(){}

    public ContactList(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.isSelected = isSelected;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

}
