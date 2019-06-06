package neeti.contactapp;

/**
 * Created by Darshan on 02-02-2017.
 */

//Adapter Class for Recycler View
public class ContactList {

    private String name;
    private String phone;
    private String photoUrl;
    public ContactList(){}

    public ContactList(String name, String phone, String photoUrl) {
        this.name = name;
        this.phone = phone;
        this.photoUrl = photoUrl;
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

    public String findName(String searchQuery){
        if(searchQuery.equalsIgnoreCase(name)){
            return name;
        }
        else
            return null;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
