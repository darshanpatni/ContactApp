package neeti.contactapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;


public class NotificationViewActivity extends AppCompatActivity {

    private RecyclerView mContactList;
    private DatabaseReference rDatabase;
    FirebaseUser user;
    FirebaseRecyclerAdapter<ContactList, ContactListViewHolder> firebaseRecyclerAdapter;
    Query query = null;

    String currentCity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_view);


        user = FirebaseAuth.getInstance().getCurrentUser();

        mContactList = (RecyclerView) findViewById(R.id.contact_list);
        //mContactList.setHasFixedSize(true);
        mContactList.setHasFixedSize(false);
        mContactList.setLayoutManager(new LinearLayoutManager(this));

        rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");
        rDatabase.keepSynced(true);

        currentCity = getIntent().getStringExtra("city");

        displayRecyclerView(currentCity);

        this.getSupportActionBar().setTitle("Contacts in " + currentCity);
    }


    public static class ContactListViewHolder extends RecyclerView.ViewHolder implements SectionTitleProvider {

        View mView;
        TextView contact_Name;
        TextView contact_Phone;
        ImageView contactImage;
        ImageButton callBtn;
        CheckBox checkBox;
        RelativeLayout relativeLayout;

        public ContactListViewHolder(View itemView) {
            super(itemView);
            contact_Name = (TextView) itemView.findViewById(R.id.ContactName);
            contact_Phone = (TextView) itemView.findViewById(R.id.ContactPhone);
            contactImage = (ImageView) itemView.findViewById(R.id.ContactPhoto);
            callBtn = (ImageButton) itemView.findViewById(R.id.call);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            mView = itemView;


        }

        void makeCheckBoxVisible(Boolean value) {
            checkBox.setVisibility(View.VISIBLE);
        }


        private ContactListViewHolder.ClickListener mClickListener;

        @Override
        public String getSectionTitle(int position) {
            return contact_Name.getText().toString().substring(0, 1);
        }

        //Interface to send callbacks...
        public interface ClickListener {
            public void onItemClick(View view, int position);

            public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(ContactListViewHolder.ClickListener clickListener) {
            mClickListener = clickListener;
        }
    }

    void displayRecyclerView(String currentCity) {


        query = rDatabase.orderByChild("city").equalTo(currentCity);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ContactList, ContactListViewHolder>
                (ContactList.class, R.layout.contact_list_row, ContactListViewHolder.class, query) {


            @Override
            protected void populateViewHolder(ContactListViewHolder viewHolder, final ContactList model, final int position) {
                viewHolder.contact_Name.setText(model.getName());
                viewHolder.contact_Phone.setText(model.getPhone());

                if (model.getPhotoUrl() != null) {
                    new Picasso.Builder(getApplicationContext())
                            .downloader(new OkHttpDownloader(getApplicationContext(), Integer.MAX_VALUE))
                            .build()
                            .load(model.getPhotoUrl())
                            .placeholder(R.drawable.ic_contact_photo)
                            .transform(new CircleTransform())
                            .into(viewHolder.contactImage);

                } else {
                    Picasso.with(getApplicationContext())
                            .load(R.drawable.ic_contact_photo)
                            .transform(new CircleTransform())
                            .into(viewHolder.contactImage);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = firebaseRecyclerAdapter.getRef(position).getKey();
                        Intent targetIntent = new Intent(NotificationViewActivity.this, ContactInfoActivity.class);
                        targetIntent.putExtra("key", key);
                        startActivity(targetIntent);
                    }
                });
                viewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + model.getPhone()));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        startActivity(intent);
                    }
                });
            }
        };
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mContactList.setAdapter(firebaseRecyclerAdapter);
    }
}
