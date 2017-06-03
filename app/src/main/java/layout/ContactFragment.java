package layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import neeti.contactapp.CircleTransform;
import neeti.contactapp.ContactInfoActivity;
import neeti.contactapp.ContactList;
import neeti.contactapp.HomeActivity;
import neeti.contactapp.NotificationViewActivity;
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

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;


    public static String contactName;
    ImageButton call;

    ProgressDialog ringProgressDialog;

    private HashMap<Integer, String> selectedContact = new HashMap<Integer, String>();

    List<String> selectedPos = new ArrayList<String>();

    List<String> selectedCon = new ArrayList<String>();

    String city = null;

    AlertDialog deleteDialog;

    TextView textview[] = new TextView[9999];

    LinearLayout linearLayout;

    Boolean flag = true;

    String search;

    private OnFragmentInteractionListener mListener;

    int c = 0;

    //Recycler Contact list variables
    private RecyclerView mContactList;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    //Firebase Variables
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference rDatabase;
    private DatabaseReference dDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String searchQuery = "";
    FirebaseUser user;
    FirebaseRecyclerAdapter<ContactList, ContactListViewHolder> firebaseRecyclerAdapter;
    Query query = null;

    String conKey;

    TextView phone;
    //Request Contact Constant
    private static final int REQUEST_READ_CONTACTS = 0;

    public boolean ac = false;
    private Long contactId;
    private Uri downloadUrl = null;
    GoogleSignInOptions gso;

    View mView;
    View deleteView;
    ViewGroup parent;

    String selectedPlace;
    String selectedPlaceAdd;

    AlertDialog dialog;
    LatLng selectedPlaceLatLng;

    Double selectLatitude;
    Double selectLongitude;

    String refKey;

    private ActionMode acMode;

    String agendaKey;
    String key;

    Menu menu = null;
    private ActionMode.Callback modeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.select_contact, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

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

        //sectionTitleIndicator = (SectionTitleIndicator) rootView.findViewById(R.id.fast_scroller_section_title_indicator);
        query = null;
        //initialize Firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("contacts");
            rDatabase.keepSynced(true);
            query = rDatabase.orderByChild("lowName");

            ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Contacts", true);

            ringProgressDialog.show();
            //initialize FirebaseRecyclerAdapter
            displayRecyclerView(query);

            if(mContactList.getLayoutManager()!=null){
                //VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) rootView.findViewById(R.id.fast_scroller);



                //fastScroller.setRecyclerView(mContactList);
                //  fastScroller.setSectionIndicator(sectionTitleIndicator);
                //mContactList.setOnScrollListener(fastScroller.getOnScrollListener());
                setRecyclerViewLayoutManager(mContactList);
            }

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
        searchQuery = input.toLowerCase();

        query = rDatabase.orderByChild("lowName").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mContactList.setAdapter(firebaseRecyclerAdapter);
        displayRecyclerView(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText)){
            searchQuery = null;
            query = rDatabase.orderByChild("lowName");

        }
        else if (search == "Name") {
            searchQuery = newText.toLowerCase();
            query = rDatabase.orderByChild("lowName").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
        }
        else if (search == "City") {
            searchQuery = newText.toLowerCase();
            query = rDatabase.orderByChild("lowCity").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
        }
        else if (search == "Phone") {
            searchQuery = newText.toLowerCase();
            query = rDatabase.orderByChild("phone").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
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

        void makeCheckBoxVisible(Boolean value){
            checkBox.setVisibility(View.VISIBLE);
        }


        private ContactListViewHolder.ClickListener mClickListener;

        @Override
        public String getSectionTitle(int position) {
            return contact_Name.getText().toString().substring(0, 1);
        }

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
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu,inflater);

        this.menu = menu;


        inflater.inflate(R.menu.fragment_search, menu);
        menu.setGroupVisible(R.id.sortAgenda, false);
        menu.setGroupVisible(R.id.search_group, true);
        menu.setGroupVisible(R.id.searchSpinner, true);

        final MenuItem searchType = menu.findItem(R.id.paraSpinner);
        Spinner searchSpinner = (Spinner) MenuItemCompat.getActionView(searchType);
        final String[] searchBy = new String[]{"Name","Phone", "City"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, searchBy);
        searchSpinner.setAdapter(adapter);

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                search = searchBy[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        //MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        //MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(this);

        searchView.setQueryHint("Search");

        super.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
        MenuItem itemClose = menu.findItem(R.id.action_close);

        itemClose.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override   
            public boolean onMenuItemClick(MenuItem item) {

                selectedPos.clear();

                selectedContact.keySet().clear();

                flag = false;

                menu.setGroupVisible(R.id.main_menu_group, false);

                menu.setGroupVisible(R.id.search_group, true);

                ((HomeActivity) getActivity()).setActionBarTitle("Home");
                ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));

                ac = false;

                for(int i = 0; i < selectedCon.size(); i++){
                    if(textview[i]!=null) {
                        ((ViewGroup) textview[i].getParent()).removeView(textview[i]);
                    }
                }
                selectedCon.clear();
                firebaseRecyclerAdapter.notifyDataSetChanged();
                mContactList.setAdapter(firebaseRecyclerAdapter);
                return false;
            }
        });

        final MenuItem itemDelete = menu.findItem(R.id.action_delete);

        itemDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder nBuilder = new AlertDialog.Builder(getActivity());
                if(deleteView!=null) {
                    parent = (ViewGroup) deleteView.getParent();
                }
                if(parent!=null) {
                    parent.removeView(deleteView);
                }
                try {
                    deleteView = getActivity().getLayoutInflater().inflate(R.layout.delete_contact_dialog, null);

                } catch (InflateException e) {
                }
                linearLayout = (LinearLayout) deleteView.findViewById(R.id.selectedList);

                //List<TextView> textView = new ArrayList<TextView>();

                final TextView textview[] = new TextView[9999];

                if(selectedCon.size()>0){
                    for (int i = 0;i < selectedCon.size();i++){
                        textview[i] = new TextView(getContext());
                        textview[i].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT));
                        textview[i].setText(selectedCon.get(i));
                        linearLayout.addView(textview[i]);
                    }
                }

                Button deleteBtn = (Button) deleteView.findViewById(R.id.action_delete);

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i = 0; i < selectedPos.size(); i++){
                            String position = selectedPos.get(i);
                            key = selectedContact.get(Integer.parseInt(position));




                            /*Query referQuery = rDatabase.child(key);

                            referQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    refKey = dataSnapshot.child("referenceKey").getValue(String.class);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            Query deleteRef = rDatabase.child(refKey).child("referenceList").orderByChild("key").equalTo(key);

                            deleteRef.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    conKey = dataSnapshot.getKey();

                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            rDatabase.getRef().child(refKey).child("referenceList").child(conKey).removeValue();*/

                            rDatabase.getRef().child(key).removeValue();
                        }
                        menu.setGroupVisible(R.id.main_menu_group, false);

                        menu.setGroupVisible(R.id.search_group, true);

                        ((HomeActivity) getActivity()).setActionBarTitle("Home");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));


                        ac = false;
                        query = rDatabase.orderByChild("lowName");

                        ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Contacts", true);

                        ringProgressDialog.show();
                        //initialize FirebaseRecyclerAdapter
                        displayRecyclerView(query);
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                        mContactList.setAdapter(firebaseRecyclerAdapter);
                        selectedPos.clear();
                        selectedContact.clear();
                        linearLayout.invalidate();
                        for(int i = 0; i < selectedCon.size(); i++){
                            ((ViewGroup)textview[i].getParent()).removeView(textview[i]);
                        }
                        selectedCon.clear();
                        deleteDialog.dismiss();
                        deleteView.destroyDrawingCache();
                    }
                });

                Button cancelBtn = (Button) deleteView.findViewById(R.id.cancel_action);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu.setGroupVisible(R.id.main_menu_group, false);

                        menu.setGroupVisible(R.id.search_group, true);

                        ((HomeActivity) getActivity()).setActionBarTitle("Home");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));


                        ac = false;
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                        mContactList.setAdapter(firebaseRecyclerAdapter);
                        selectedPos.clear();
                        selectedContact.clear();
                        linearLayout.invalidate();
                        for(int i = 0; i < selectedCon.size(); i++){
                            ((ViewGroup)textview[i].getParent()).removeView(textview[i]);
                        }
                        selectedCon.clear();
                        deleteDialog.dismiss();
                        deleteView.destroyDrawingCache();
                    }
                });
                nBuilder.setView(deleteView);
                deleteDialog = nBuilder.create();
                deleteDialog.show();
                return  false;
            }
        });

        MenuItem itemAssign = menu.findItem(R.id.action_assign_location);

        flag =true;
        itemAssign.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                if(mView!=null) {
                    parent = (ViewGroup) mView.getParent();
                }
                if(parent!=null) {
                    parent.removeView(mView);
                }
                try {
                    mView = getActivity().getLayoutInflater().inflate(R.layout.assign_location_dialog, null);

                } catch (InflateException e) {
                }
                final LinearLayout linearLayout = (LinearLayout) mView.findViewById(R.id.selectedList);

                //List<TextView> textView = new ArrayList<TextView>();



                if(selectedCon.size()>0){
                    for(int i = 0; i < selectedCon.size(); i++){
                        if(textview[i]!=null) {
                            try {
                                ((ViewGroup) textview[i].getParent()).removeView(textview[i]);
                            }
                            catch (NullPointerException e){

                            }
                        }
                    }
                    for (int i = 0;i < selectedCon.size();i++){
                        textview[i] = new TextView(getContext());
                        textview[i].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT));
                        textview[i].setText(selectedCon.get(i));
                        linearLayout.addView(textview[i]);
                    }
                }
                final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                        getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete);
                Button assignLocation = (Button) mView.findViewById(R.id.action_assign_location);
                Button cancelBtn = (Button) mView.findViewById(R.id.cancel_action);



                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        // TODO: Get info about the selected place.
                        //   Toast.makeText(this, , Toast.LENGTH_LONG).show();
                        //  Log.i(TAG, "Place: " + place.getName());
                        selectedPlace = place.getName().toString();
                        selectedPlaceAdd = place.getAddress().toString();


                        selectedPlaceLatLng = place.getLatLng();

                        selectLatitude = selectedPlaceLatLng.latitude;
                        selectLongitude = selectedPlaceLatLng.longitude;



                        String link = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+selectLatitude+","+selectLongitude+"&sensor=true";
                        RequestQueue queue = Volley.newRequestQueue(getContext());

                        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, link,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONArray jObj = new JSONObject(response).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                                            for (int i = 0; i < jObj.length(); i++) {
                                                String componentName = new JSONObject(jObj.getString(i)).getJSONArray("types").getString(0);
                                                if (componentName.equals("locality")) {
                                                    city = new JSONObject(jObj.getString(i)).getString("long_name");
                                                }
                                                if (city==null){
                                                    if(componentName.equals("administrative_area_level_2")){
                                                        city = new JSONObject(jObj.getString(i)).getString("long_name");
                                                    }
                                                }
                                            }


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                int x = 1;
                            }
                        });

                        queue.add(stringRequest);
                        //GetLocationDownloadTask getLocation = new GetLocationDownloadTask();
                        //getLocation.execute(link);
                    }

                    @Override
                    public void onError(Status status) {
                        // TODO: Handle the error.
                        //  Log.i(TAG, "An error occurred: " + status);
                    }
                });

                assignLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i = 0; i < selectedPos.size(); i++){
                            String position = selectedPos.get(i);
                            String key = selectedContact.get(Integer.parseInt(position));
                            rDatabase.getRef().child(key).child("city").setValue(city);
                            rDatabase.getRef().child(key).child("lowCity").setValue(city.toLowerCase());
                            rDatabase.getRef().child(key).child("selectedPlace").setValue(selectedPlace);
                            rDatabase.getRef().child(key).child("selectedPlaceAdd").setValue(selectedPlaceAdd);
                            rDatabase.getRef().child(key).child("selectLatitude").setValue(selectLatitude);
                            rDatabase.getRef().child(key).child("selectLongitude").setValue(selectLongitude);
                        }

                        autocompleteFragment.onDestroy();
                        menu.setGroupVisible(R.id.main_menu_group, false);

                        menu.setGroupVisible(R.id.search_group, true);

                        ((HomeActivity) getActivity()).setActionBarTitle("Home");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));

                        ac = false;
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                        mContactList.setAdapter(firebaseRecyclerAdapter);
                        selectedPos.clear();
                        selectedContact.clear();
                        linearLayout.invalidate();
                        for(int i = 0; i < selectedCon.size(); i++){
                            ((ViewGroup)textview[i].getParent()).removeView(textview[i]);
                        }
                        selectedCon.clear();
                        dialog.dismiss();

                        mView.destroyDrawingCache();                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu.setGroupVisible(R.id.main_menu_group, false);

                        menu.setGroupVisible(R.id.search_group, true);

                        ((HomeActivity) getActivity()).setActionBarTitle("Home");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));

                        autocompleteFragment.onDestroy();

                        flag = false;
                        ac = false;
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                        mContactList.setAdapter(firebaseRecyclerAdapter);
                        selectedPos.clear();
                        selectedContact.clear();
                        linearLayout.invalidate();
                        for(int i = 0; i < selectedCon.size(); i++){
                            ((ViewGroup)textview[i].getParent()).removeView(textview[i]);
                        }
                        selectedCon.clear();
                        dialog.dismiss();
                        mView.destroyDrawingCache();
                    }
                });

                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
                return false;
            }
        });
    }

    public void displayRecyclerView(Query query){

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ContactList, ContactListViewHolder>(

                ContactList.class,
                R.layout.contact_list_row,
                ContactListViewHolder.class,
                query

        ) {

            @Override
            protected void populateViewHolder(final ContactListViewHolder viewHolder, final ContactList model, final int position) {

                viewHolder.contact_Name.setText(model.getName());
                viewHolder.contact_Phone.setText(model.getPhone());
                firebaseRecyclerAdapter.getRef(position).getKey();
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                viewHolder.relativeLayout.setBackgroundResource(outValue.resourceId);

                if(selectedPos.contains(String.valueOf(position))){
                    viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightCyan));

                }

                contactName = model.getName();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


//                        Toast.makeText(getActivity(), "Position: " + position + " Key: " + firebaseRecyclerAdapter.getRef(position).getKey()
  //              , Toast.LENGTH_SHORT).show();

                        if(ac){
                            if(selectedPos.contains(String.valueOf(position))){
                                //viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorRed));
                                selectedPos.remove(String.valueOf(position));
                                selectedContact.remove(position);
                                selectedCon.remove(model.getName());
                                TypedValue outValue = new TypedValue();
                                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                                viewHolder.relativeLayout.setBackgroundResource(outValue.resourceId);

                                ((HomeActivity) getActivity()).setActionBarTitle(selectedPos.size()+" items selected.");
                                //getActivity().setTitle(selectedPos.size()+" items selected.");


                                if(selectedPos.size()==0){
                                    menu.setGroupVisible(R.id.main_menu_group, false);
                                    menu.setGroupVisible(R.id.search_group, true);
                                    ((HomeActivity) getActivity()).setActionBarTitle("Home");
                                    ac=false;
                                    ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));


                                }
                            }
                            else {
                                selectedContact.put(position, firebaseRecyclerAdapter.getRef(position).getKey());

                                selectedCon.add(model.getName());
                                selectedPos.add(String.valueOf(position));
                                viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightCyan));

                                ((HomeActivity) getActivity()).setActionBarTitle(selectedPos.size()+" items selected.");

                                //getActivity().setTitle(selectedPos.size()+" items selected.");37474F
                            }
                        }

                        else{
                            Intent targetIntent = new Intent(getContext(), ContactInfoActivity.class);
                            targetIntent.putExtra("key",firebaseRecyclerAdapter.getRef(position).getKey());
                            targetIntent.putExtra("intent", "ContactFragment");
                            startActivity(targetIntent);
                        }
                    }
                });


                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ac = true;
                        //viewHolder.makeCheckBoxVisible(true);
                        selectedContact.put(position, firebaseRecyclerAdapter.getRef(position).getKey());
                        selectedPos.add(String.valueOf(position));
                        selectedCon.add(model.getName());

                        viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightCyan));
                        menu.setGroupVisible(R.id.main_menu_group, true);
                        menu.setGroupVisible(R.id.search_group,false);
                        ((HomeActivity) getActivity()).setActionBarTitle(selectedPos.size()+" items selected.");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#FFC300")));
                        //getActivity().setTitle(selectedPos.size()+" items selected.");
                        return true;
                    }

                });
               viewHolder.getSectionTitle(position);

                viewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + model.getPhone()));
                        startActivity(intent);
                    }
                });
                if(model.getPhotoUrl()!=null){
                    new Picasso.Builder(getContext())
                            .downloader(new OkHttpDownloader(getContext(), Integer.MAX_VALUE))
                            .build()
                            .load(model.getPhotoUrl())
                            .placeholder(R.drawable.ic_contact_photo)
                            .transform(new CircleTransform())
                            .into(viewHolder.contactImage);
                    /*Picasso.with(getContext())
                            .load(model.getPhotoUrl())
                            .transform(new CircleTransform())
                            .into(viewHolder.contactImage);*/

                }
                else{
                    Picasso.with(getContext())
                            .load(R.drawable.ic_contact_photo)
                            .transform(new CircleTransform())
                            .into(viewHolder.contactImage);
                }

            }


        };
        ringProgressDialog.dismiss();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mContactList.setAdapter(firebaseRecyclerAdapter);
        //set adapter for recycler view
    }
    /**
     * Set RecyclerView's LayoutManager
     */
    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition =
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    }

