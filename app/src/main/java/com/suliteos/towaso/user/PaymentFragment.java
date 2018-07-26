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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;

public class PaymentFragment extends Fragment {

    private FirebaseUser mUser;
    private TextView mCustomerNameView,mPerMonthView,mDuePaymentView,mLastPaymentView;
    private User user;
    private String key;
    private PaymentAdapter adapter;
    private FirebaseFirestore mRootFireStore;
    private ListenerRegistration registration;

    public PaymentFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = new Bundle();
        bundle.putLong("timestamp",System.currentTimeMillis());
        assert mUser != null;
        bundle.putString("name",mUser.getDisplayName());
        bundle.putString("fragment",PaymentFragment.class.getSimpleName());
        Analytics.logEventFragmentOpened(getContext(),bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_payment, container, false);

        mCustomerNameView = rootView.findViewById(R.id.person_name);
        mPerMonthView = rootView.findViewById(R.id.per_month);
        mDuePaymentView = rootView.findViewById(R.id.due_payment);
        mLastPaymentView = rootView.findViewById(R.id.last_payment);

        setHasOptionsMenu(true);

        RecyclerView paymentList = rootView.findViewById(R.id.payment_list);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        paymentList.setLayoutManager(manager);
        adapter = new PaymentAdapter(getActivity());
        paymentList.setAdapter(adapter);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
        user = gson.fromJson(json, User.class);
        mRootFireStore = FirebaseFirestore.getInstance();

        mRootFireStore.collection("Customer").document(user.getState()).collection(user.getDistrict()).document(user.getType()).collection(user.getSubType()).document(mUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(e == null) {
                    key = documentSnapshot.getData().keySet().toString();
                    key = key.substring(1, key.length() - 1);
                    updateDisplay();
                }else {
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });


        if(rootView.findViewById(R.id.framelayout_4)!=null && getFragmentManager().findFragmentById(R.id.framelayout_4).isInLayout()){


            //ToDo firebase code
        }


        return rootView;
    }

    private void updateDisplay() {
        mPerMonthView.setText(String.format("%s per month", String.valueOf(user.getPerMonth())));
        mCustomerNameView.setText(mUser.getDisplayName());
        mRootFireStore.collection("Details").document(user.getState()).collection(user.getDistrict()).document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e == null) {
                    String duePayment = documentSnapshot.get("duePayment").toString();
                    String mLastPayment = documentSnapshot.get("lastPayment").toString();
                    mDuePaymentView.setText(String.format("%s due Payment", duePayment));
                    if(TextUtils.equals(mLastPayment,"0")){
                        mLastPayment = "No payment made";
                    }
                    mLastPaymentView.setText(mLastPayment);
                }else {
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        CollectionReference mPerPaymentFireStore = mRootFireStore.collection("Details").document(user.getState()).collection(user.getDistrict()).document(key).collection("payment_per_month_status");
        registration = mPerPaymentFireStore.addSnapshotListener(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (registration != null) {
            registration.remove();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.clear();
    }

}
