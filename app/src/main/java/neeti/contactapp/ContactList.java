package neeti.contactapp;

/**
 * Created by Darshan on 02-02-2017.
 */

//Adapter Class for Recycler View
public class ContactList {

    private String Name;
    private String Phone;

    public ContactList(){}

    public ContactList(String name, String phone) {
        Name = name;
        Phone = phone;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }



}
