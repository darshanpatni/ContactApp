package neeti.contactapp;

/**
 * Created by Darshan on 26-02-2017.
 */

public class AgendaList {
    private String title;
    private String date;
    private float rating;

    public AgendaList(){}

    public AgendaList(String title, String date, float rating){
        this.title = title;
        this.date = date;
        this.rating = rating;
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


    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
