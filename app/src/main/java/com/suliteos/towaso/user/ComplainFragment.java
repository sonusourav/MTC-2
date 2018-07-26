package com.suliteos.towaso.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;

public class ComplainFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private EditText mDescriptionView,mTitleView;
    private Spinner mComplainTypeView;

    private int position;

    // OnRegister click listener
    private boolean mComplainIn;
    private OnComplainListener mComplainListener;

    //private DatabaseReference mDatabaseChildRef;
    private ComplainAdapter mAdapter;
    private String state,district;
    private CollectionReference mUserCollectionReference;
    private ListenerRegistration registration;

    public ComplainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = new Bundle();
        bundle.putLong("timestamp",System.currentTimeMillis());
        assert mUser != null;
        bundle.putString("name",mUser.getDisplayName());
        bundle.putString("fragment","Complain");
        Analytics.logEventFragmentOpened(getContext(),bundle);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
        User user = gson.fromJson(json, User.class);
        state = user.getState();
        district = user.getDistrict();
        FirebaseFirestore mRootFireStore = FirebaseFirestore.getInstance();
        mUserCollectionReference = mRootFireStore.collection("Complain").document(state).collection(district).document("User").collection(mUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_complain, container, false);
        mDescriptionView = rootView.findViewById(R.id.description_edit_text);
        mTitleView = rootView.findViewById(R.id.complain_type);
        mComplainTypeView = rootView.findViewById(R.id.complain_spinner);
        Button mComplainBtn = rootView.findViewById(R.id.complain_btn);

        ArrayAdapter<CharSequence> complainAdapter = ArrayAdapter.createFromResource(getContext(),R.array.complain_type,android.R.layout.simple_spinner_item);
        complainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mComplainTypeView.setAdapter(complainAdapter);
        mComplainTypeView.setOnItemSelectedListener(this);

        mComplainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                complain();
            }
        });



        RecyclerView complainList = rootView.findViewById(R.id.complain_list);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        complainList.setLayoutManager(manager);
        mAdapter = new ComplainAdapter(getContext(), state, district);
        complainList.setAdapter(mAdapter);

        if(rootView.findViewById(R.id.framelayout_2)!=null && getFragmentManager().findFragmentById(R.id.framelayout_2).isInLayout()){


            //ToDo firebase code
        }


        return rootView;


    }

    @Override
    public void onPause() {
        super.onPause();
        if (registration != null){
            registration.remove();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.clear();
        registration = mUserCollectionReference.addSnapshotListener(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (registration != null){
            registration.remove();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i == 0){
            position =0;
            mTitleView.setVisibility(View.GONE);
        }else if (i == 1){
            position =1;
            mTitleView.setVisibility(View.GONE);
        }else if (i == 2){
            position =2;
            mTitleView.setVisibility(View.GONE);
        }else if (i == 3){
            position =3;
            mTitleView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void complain(){
        if (mComplainIn){
            return;
        }

        mDescriptionView.setError(null);

        String desc = mDescriptionView.getText().toString();
        String type = null;
        if (position == 0){
            type = null;
        }else if (position == 1){
            type = mComplainTypeView.getSelectedItem().toString();
        }else if (position == 2){
            type = mComplainTypeView.getSelectedItem().toString();
        }else if (position == 3){
            type = mTitleView.getText().toString();
        }

        boolean cancelComplain = false;
        View focusView = null;

        if (TextUtils.isEmpty(desc)){
            mDescriptionView.setError(getString(R.string.required));
            focusView = mDescriptionView;
            cancelComplain = true;
        }

        if (position == 0){
            cancelComplain = true;
            focusView = mComplainTypeView;
            Toast.makeText(getContext(), "Select Type of Complain", Toast.LENGTH_SHORT).show();
        }else {
            if (TextUtils.isEmpty(type)){
                mTitleView.setError(getString(R.string.required));
                focusView = mTitleView;
                cancelComplain = true;
            }
        }
        if (cancelComplain){
            focusView.requestFocus();
        }else{
            mAdapter.clear();
            mComplainListener.onComplain(type,desc);
            mDescriptionView.getText().clear();
            mComplainIn = true;
        }
    }

    interface OnComplainListener{
        void onComplain(String type, String desc);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mComplainListener = (OnComplainListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mComplainListener = null;
    }
}
