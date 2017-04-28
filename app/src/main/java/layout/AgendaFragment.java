package layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import neeti.contactapp.AgendaList;
import neeti.contactapp.ContactList;
import neeti.contactapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AgendaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AgendaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AgendaFragment extends Fragment implements SearchView.OnQueryTextListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Recycler Contact list variables
    private RecyclerView mAgendaList;

    //Firebase Variables
    private DatabaseReference rDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    FirebaseUser user;
    FirebaseRecyclerAdapter<AgendaList, AgendaFragment.AgendaListViewHolder> firebaseRecyclerAdapter;

    ProgressDialog ringProgressDialog;
    String searchQuery = "";
    Query query = null;
    public AgendaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AgendaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AgendaFragment newInstance(String param1, String param2) {
        AgendaFragment fragment = new AgendaFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_agenda, container, false);
        setHasOptionsMenu(true);
        //initialize Recycler view
        mAgendaList = (RecyclerView) rootView.findViewById(R.id.agenda_list);
        mAgendaList.setLayoutManager(new LinearLayoutManager(getActivity()));


        //initialize Firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("agenda");
            rDatabase.keepSynced(true);
            query = rDatabase;

            ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Agendas", true);

            ringProgressDialog.show();
            displayRecyclerView(query);
            //initialize FirebaseRecyclerAdapter

      //set adapter for recycler view

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


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        searchQuery = input.toLowerCase();
        query = rDatabase.orderByChild("lowTitle").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mAgendaList.setAdapter(firebaseRecyclerAdapter);
        displayRecyclerView(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText)){
            searchQuery = "";
            query = rDatabase.orderByChild("lowTitle");

        }
        else{
            searchQuery = newText.toLowerCase();
            query = rDatabase.orderByChild("lowTitle").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        }
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mAgendaList.setAdapter(firebaseRecyclerAdapter);
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
    public static class AgendaListViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView agenda_title;
        TextView agenda_date;

        public AgendaListViewHolder(View itemView) {
            super(itemView);
            agenda_title = (TextView) itemView.findViewById(R.id.agendaTitle);
            agenda_date = (TextView) itemView.findViewById(R.id.agendaDate);
            mView = itemView;
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
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AgendaList, AgendaListViewHolder>(

                AgendaList.class,
                R.layout.agenda_list_row,
                AgendaListViewHolder.class,
                query

        ) {

            @Override
            protected void populateViewHolder(AgendaListViewHolder viewHolder, AgendaList model, final int position) {

                viewHolder.agenda_title.setText(model.getTitle());
                viewHolder.agenda_date.setText(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String key = firebaseRecyclerAdapter.getRef(position).getKey();
                        Toast.makeText(getActivity(),  key, Toast.LENGTH_SHORT).show();
                    }
                });

            }

        };
        ringProgressDialog.dismiss();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mAgendaList.setAdapter(firebaseRecyclerAdapter);

    }
}
