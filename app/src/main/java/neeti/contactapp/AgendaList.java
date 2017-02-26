package neeti.contactapp;

/**
 * Created by Darshan on 26-02-2017.
 */

public class AgendaList {
    private String title;
    private String date;

    public AgendaList(){}

    public AgendaList(String title, String date){
        this.title = title;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
