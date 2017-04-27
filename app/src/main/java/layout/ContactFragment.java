package layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import neeti.contactapp.ContactList;
import neeti.contactapp.HomeActivity;
import neeti.contactapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFragment extends Fragment implements SearchView.OnQueryTextListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ProgressDialog ringProgressDialog;

    private HashMap<Integer, String> hashMapContact;

    private OnFragmentInteractionListener mListener;

    //Recycler Contact list variables
    private RecyclerView mContactList;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    //Firebase Variables
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference rDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String searchQuery = "";
    FirebaseUser user;
    FirebaseRecyclerAdapter<ContactList, ContactListViewHolder> firebaseRecyclerAdapter;
    Query query = null;
    //Request Contact Constant
    private static final int REQUEST_READ_CONTACTS = 0;

    private Long contactId;
    private Uri downloadUrl = null;
    GoogleSignInOptions gso;


    public ContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        setHasOptionsMenu(true);
        //initialize Recycler view
        mContactList = (RecyclerView) rootView.findViewById(R.id.contact_list);
        //mContactList.setHasFixedSize(true);
        mContactList.setLayoutManager(new LinearLayoutManager(getActivity()));

        query = null;
        //initialize Firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");
            rDatabase.keepSynced(true);
            query = rDatabase.orderByChild("name");

            ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Contacts", true);

            ringProgressDialog.show();
            //initialize FirebaseRecyclerAdapter

            displayRecyclerView(query);


        }
        mAuth = FirebaseAuth.getInstance();



        return  rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        searchQuery = input;
        query = rDatabase.orderByChild("lowName").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mContactList.setAdapter(firebaseRecyclerAdapter);
        displayRecyclerView(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText)){
            searchQuery = "";
            query = rDatabase.orderByChild("lowName");

        }
        else{
            searchQuery = newText;
            query = rDatabase.orderByChild("lowName").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        }
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mContactList.setAdapter(firebaseRecyclerAdapter);
        displayRecyclerView(query);
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    //ViewHolder for ContactList Recycler View
    public static class ContactListViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView contact_Name;
        TextView contact_Phone;



        public ContactListViewHolder(View itemView) {
            super(itemView);
            contact_Name = (TextView) itemView.findViewById(R.id.ContactName);
            contact_Phone = (TextView) itemView.findViewById(R.id.ContactPhone);
            mView = itemView;


        }

        private ContactListViewHolder.ClickListener mClickListener;

        //Interface to send callbacks...
        public interface ClickListener{
            public void onItemClick(View view, int position);
            public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(ContactListViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu,inflater);

        inflater.inflate(R.menu.fragment_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        //MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        //MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(this);

        searchView.setQueryHint("Search");

        super.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void displayRecyclerView(Query query){

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ContactList, ContactListViewHolder>(

                ContactList.class,
                R.layout.contact_list_row,
                ContactListViewHolder.class,
                query

        ) {

            @Override
            protected void populateViewHolder(ContactListViewHolder viewHolder, ContactList model, final int position) {

                viewHolder.contact_Name.setText(model.getName());
                viewHolder.contact_Phone.setText(model.getPhone());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String key = firebaseRecyclerAdapter.getRef(position).getKey();
                        Toast.makeText(getActivity(), "Position: " + position + " Key: " + key, Toast.LENGTH_SHORT).show();
                    }
                });
            }


        };
        ringProgressDialog.dismiss();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mContactList.setAdapter(firebaseRecyclerAdapter);
        //set adapter for recycler view
    }
    }

