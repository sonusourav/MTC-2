package com.suliteos.towaso.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        ,ComplainFragment.OnComplainListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mUser;

    private TextView mUserName,mUserEmail;
    private CircleImageView mUserImage;
    private StorageReference mStorageRootRef;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        authWithFireBase(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mUserName = mNavigationView.getHeaderView(0).findViewById(R.id.user_name);
        mUserEmail = mNavigationView.getHeaderView(0).findViewById(R.id.user_email);
        mUserImage = mNavigationView.getHeaderView(0).findViewById(R.id.user_image);

        mStorageRootRef = FirebaseStorage.getInstance().getReference();
    }

    private void authWithFireBase(final Bundle savedInstanceState){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if(mUser == null) {
                    switchToLoginActivity();
                }else {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
                    Gson gson = new Gson();
                    String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
                    User user = gson.fromJson(json, User.class);
                    mUserName.setText(mUser.getDisplayName());
                    mUserEmail.setText(mUser.getEmail());
                    mStorageRootRef.child(user.getImageUrl()).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mUserImage.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });



                    if (savedInstanceState == null) {
                        switchToPaymentFragment();
                    }

                }
            }
        };
    }

    private void switchToPaymentFragment() {
        mNavigationView.setCheckedItem(R.id.nav_payment);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, new PaymentFragment(),"Payment");
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void switchToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_payment) {
            switchToPaymentFragment();
        } else if (id == R.id.nav_complain) {
            switchToComplainFragment();
        } else if (id == R.id.nav_worker) {
            switchToWorkerFragment();
        } else if (id == R.id.nav_profile) {
            switchToProfileFragment();
        } else if (id == R.id.nav_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.data_file_json_key),"");
            editor.apply();
            mAuth.signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchToProfileFragment() {
        mNavigationView.setCheckedItem(R.id.nav_profile);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, new ProfileFragment(),"Profile");
        ft.commit();
    }

    private void switchToWorkerFragment() {
        mNavigationView.setCheckedItem(R.id.nav_worker);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, new WorkerFragment(),"Worker");
        ft.commit();
    }

    private void switchToComplainFragment() {
        mNavigationView.setCheckedItem(R.id.nav_complain);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, new ComplainFragment(),"Complain");
        ft.commit();
    }

    @Override
    public void onComplain(String type, String desc) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
        User user = gson.fromJson(json, User.class);

        Bundle bundle = new Bundle();
        bundle.putString("complain_type",type);
        bundle.putString("ward",String.valueOf(user.getWard()));
        if (TextUtils.equals(type ,"Miss Behave")) {
            Analytics.subscribeToTopic("missBehave");
        }else if (TextUtils.equals(type,"Irregularity")){
            Analytics.subscribeToTopic(type);
        }else{
            Analytics.subscribeToTopic("otherComplain");
        }
        Analytics.logEventComplain(MainActivity.this,bundle);

        Map<String, Object> setComplain = new HashMap<>();
        setComplain.put("desc",desc);
        setComplain.put("email",mUser.getEmail());
        setComplain.put("loc",user.getLoc());
        setComplain.put("holding",user.getHouseHold());
        setComplain.put("phone",user.getPhone());
        setComplain.put("title",type);
        setComplain.put("imageUrl",user.getImageUrl());
        setComplain.put("status","red");
        setComplain.put("timeStamp", FieldValue.serverTimestamp());


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference= db.collection("Complain").document(user.getState()).collection(user.getDistrict());
        DocumentReference cr = collectionReference.document();
        String pushId = cr.getId();

        collectionReference.document("Complains").collection("Complain").document(pushId).set(setComplain);

        Map<String, Object> setPushId = new HashMap<>();
        setPushId.put("solve",false);
        collectionReference.document("User").collection(mUser.getUid()).document(pushId).set(setPushId, SetOptions.merge());
        collectionReference.document("Super").collection(String.valueOf(user.getWard())).document(pushId).set(setPushId,SetOptions.merge());
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

    }
}
