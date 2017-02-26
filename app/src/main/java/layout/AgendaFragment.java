package layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class AgendaFragment extends Fragment {
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

        //initialize Recycler view
        mAgendaList = (RecyclerView) rootView.findViewById(R.id.agenda_list);
        mAgendaList.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = null;
        //initialize Firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            rDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child(user.getDisplayName()).child("agenda");
            rDatabase.keepSynced(true);
            query = rDatabase;

            final ProgressDialog ringProgressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Loading Agendas", true);

            ringProgressDialog.show();
            //initialize FirebaseRecyclerAdapter

            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AgendaList, AgendaListViewHolder>(

                    AgendaList.class,
                    R.layout.agenda_list_row,
                    AgendaListViewHolder.class,
                    query

            ) {

                @Override
                protected void populateViewHolder(AgendaListViewHolder viewHolder, AgendaList model, int position) {

                    viewHolder.agenda_title.setText(model.getTitle());
                    viewHolder.agenda_date.setText(model.getDate());

                }

            };
            ringProgressDialog.dismiss();
            firebaseRecyclerAdapter.notifyDataSetChanged();
            mAgendaList.setAdapter(firebaseRecyclerAdapter);        //set adapter for recycler view

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

   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
*/
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
}
