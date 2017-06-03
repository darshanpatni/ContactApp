package layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import neeti.contactapp.AgendaInfoActivity;
import neeti.contactapp.AgendaList;
import neeti.contactapp.ContactList;
import neeti.contactapp.HomeActivity;
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
    LinearLayoutManager linearLayoutManager;

    Menu menu;

    private HashMap<Integer, String> selectedAgenda = new HashMap<Integer, String>();

    List<String> selectedPos = new ArrayList<String>();

    List<String> selectedA = new ArrayList<String>();

    String city = null;

    AlertDialog deleteDialog;

    Boolean ac = false;

    TextView textview[] = new TextView[9999];

    View deleteView;

    ViewGroup parent;

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
        linearLayoutManager = new LinearLayoutManager(getActivity());
        mAgendaList.setLayoutManager(linearLayoutManager);


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
        RelativeLayout relativeLayout;
        RatingBar ratingBar;

        public AgendaListViewHolder(View itemView) {
            super(itemView);
            agenda_title = (TextView) itemView.findViewById(R.id.agendaTitle);
            agenda_date = (TextView) itemView.findViewById(R.id.agendaDate);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            mView = itemView;
        }

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu,inflater);

        this.menu = menu;

        inflater.inflate(R.menu.agenda_fragment_menu, menu);
        menu.setGroupVisible(R.id.sortAgenda, true);

        MenuItem sortItem = menu.findItem(R.id.sortSpinner);
        Spinner sortSpinner = (Spinner) MenuItemCompat.getActionView(sortItem);
        final String[] sortBy = new String[]{"Created","Name", "Rating"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, sortBy);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (sortBy[position] == "Name"){
                    query = rDatabase.orderByChild("lowTitle");
                    ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Contacts", true);
                    ringProgressDialog.show();;
                    //initialize FirebaseRecyclerAdapter
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                    linearLayoutManager.setReverseLayout(false);
                    mAgendaList.setLayoutManager(linearLayoutManager);
                    mAgendaList.setAdapter(firebaseRecyclerAdapter);
                    displayRecyclerView(query);
                }
                else if (sortBy[position] == "Rating"){
                    query = rDatabase.orderByChild("rating");
                    ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Contacts", true);
                    ringProgressDialog.show();
                    //initialize FirebaseRecyclerAdapter
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                    linearLayoutManager.setReverseLayout(true);
                    mAgendaList.setLayoutManager(linearLayoutManager);
                    mAgendaList.setAdapter(firebaseRecyclerAdapter);

                    displayRecyclerView(query);
                }
                else{
                    query = rDatabase;
                    ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Contacts", true);
                    ringProgressDialog.show();;
                    //initialize FirebaseRecyclerAdapter
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                    linearLayoutManager.setReverseLayout(false);
                    mAgendaList.setLayoutManager(linearLayoutManager);
                    mAgendaList.setAdapter(firebaseRecyclerAdapter);
                    displayRecyclerView(query);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        menu.setGroupVisible(R.id.search_group, true);
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

                selectedAgenda.keySet().clear();



                menu.setGroupVisible(R.id.main_menu_group, false);

                menu.setGroupVisible(R.id.search_group, true);

                ((HomeActivity) getActivity()).setActionBarTitle("Home");
                ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));

                ac = false;

                for(int i = 0; i < selectedA.size(); i++){
                    if(textview[i]!=null) {
                        ((ViewGroup) textview[i].getParent()).removeView(textview[i]);
                    }
                }
                selectedA.clear();

                firebaseRecyclerAdapter.notifyDataSetChanged();
                mAgendaList.setAdapter(firebaseRecyclerAdapter);
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
                    deleteView = getActivity().getLayoutInflater().inflate(R.layout.delete_agenda_dialog, null);

                } catch (InflateException e) {
                }
                final LinearLayout linearLayout = (LinearLayout) deleteView.findViewById(R.id.selectedList);

                //List<TextView> textView = new ArrayList<TextView>();

                final TextView textview[] = new TextView[9999];

                if(selectedA.size()>0){
                    for (int i = 0;i < selectedA.size();i++){
                        textview[i] = new TextView(getContext());
                        textview[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        textview[i].setText(selectedA.get(i));
                        linearLayout.addView(textview[i]);
                    }
                }

                Button deleteBtn = (Button) deleteView.findViewById(R.id.action_delete);

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i = 0; i < selectedPos.size(); i++){
                            String position = selectedPos.get(i);
                            String key = selectedAgenda.get(Integer.parseInt(position));
                            rDatabase.getRef().child(key).removeValue();
                        }
                        menu.setGroupVisible(R.id.main_menu_group, false);

                        menu.setGroupVisible(R.id.search_group, true);

                        ((HomeActivity) getActivity()).setActionBarTitle("Home");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#37474F")));


                        ac = false;
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                        mAgendaList.setAdapter(firebaseRecyclerAdapter);
                        selectedPos.clear();
                        selectedAgenda.clear();
                        linearLayout.invalidate();
                        for(int i = 0; i < selectedA.size(); i++){
                            ((ViewGroup)textview[i].getParent()).removeView(textview[i]);
                        }
                        selectedA.clear();
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
                        mAgendaList.setAdapter(firebaseRecyclerAdapter);
                        selectedPos.clear();
                        selectedAgenda.clear();
                        linearLayout.invalidate();
                        for(int i = 0; i < selectedA.size(); i++){
                            ((ViewGroup)textview[i].getParent()).removeView(textview[i]);
                        }
                        selectedA.clear();
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



    }

    public void displayRecyclerView(Query query){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AgendaList, AgendaListViewHolder>(

                AgendaList.class,
                R.layout.agenda_list_row,
                AgendaListViewHolder.class,
                query

        ) {

            @Override
            protected void populateViewHolder(final AgendaListViewHolder viewHolder, final AgendaList model, final int position) {

                viewHolder.agenda_title.setText(model.getTitle());
                viewHolder.agenda_date.setText(model.getDate());
                viewHolder.ratingBar.setRating(model.getRating());

                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                viewHolder.relativeLayout.setBackgroundResource(outValue.resourceId);

                if(selectedPos.contains(String.valueOf(position))){
                    viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightCyan));

                }


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String key = firebaseRecyclerAdapter.getRef(position).getKey();
                        Toast.makeText(getActivity(),  key, Toast.LENGTH_SHORT).show();

                        if(ac){
                            if(selectedPos.contains(String.valueOf(position))){
                                //viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorRed));
                                selectedPos.remove(String.valueOf(position));
                                selectedAgenda.remove(position);
                                selectedA.remove(model.getTitle());
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
                                selectedAgenda.put(position, firebaseRecyclerAdapter.getRef(position).getKey());

                                selectedA.add(model.getTitle());
                                selectedPos.add(String.valueOf(position));
                                viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightCyan));

                                ((HomeActivity) getActivity()).setActionBarTitle(selectedPos.size()+" items selected.");

                                //getActivity().setTitle(selectedPos.size()+" items selected.");37474F
                            }

                        }
                        else {
                            Intent targetIntent = new Intent(getContext(), AgendaInfoActivity.class);
                            targetIntent.putExtra("key",firebaseRecyclerAdapter.getRef(position).getKey());
                            startActivity(targetIntent);
                        }
                    }
                });

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ac = true;
                        //viewHolder.makeCheckBoxVisible(true);
                        selectedAgenda.put(position, firebaseRecyclerAdapter.getRef(position).getKey());
                        selectedPos.add(String.valueOf(position));
                        selectedA.add(model.getTitle());

                        viewHolder.relativeLayout.setBackgroundColor(getResources().getColor(R.color.lightCyan));
                        menu.setGroupVisible(R.id.main_menu_group, true);
                        menu.setGroupVisible(R.id.search_group,false);
                        ((HomeActivity) getActivity()).setActionBarTitle(selectedPos.size()+" items selected.");
                        ((HomeActivity) getActivity()).setBackgroundColor(new ColorDrawable(Color.parseColor("#FFC300")));
                        //getActivity().setTitle(selectedPos.size()+" items selected.");
                        return true;
                    }

                });

            }

        };
        ringProgressDialog.dismiss();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mAgendaList.setAdapter(firebaseRecyclerAdapter);

    }
}
