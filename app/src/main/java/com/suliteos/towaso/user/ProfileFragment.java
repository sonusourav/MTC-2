package com.suliteos.towaso.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

public class ProfileFragment extends Fragment {

    private TextView mNameView,mEmailView,mTypeView,mSubTypeView,mAddressView;
    private FirebaseUser mUser;
    private StorageReference mStorage;
    private ImageView mProfileImageView;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorage = storage.getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = new Bundle();
        bundle.putLong("timestamp",System.currentTimeMillis());
        assert mUser != null;
        bundle.putString("name",mUser.getDisplayName());
        bundle.putString("fragment",ProfileFragment.class.getSimpleName());
        Analytics.logEventFragmentOpened(getContext(),bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile,container,false);
        mNameView = rootView.findViewById(R.id.profile_name);
        mEmailView = rootView.findViewById(R.id.profile_email);
        mTypeView = rootView.findViewById(R.id.profile_type);
        mSubTypeView = rootView.findViewById(R.id.profile_subType);
        mAddressView = rootView.findViewById(R.id.profile_address);
        mProfileImageView = rootView.findViewById(R.id.profile_image_view);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNameView.setText(mUser.getDisplayName());
        mEmailView.setText(mUser.getEmail());
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
        User user = gson.fromJson(json, User.class);
        mTypeView.setText(user.getType());
        mSubTypeView.setText(user.getSubType());
        String ward = String.valueOf(user.getWard());
        String district = user.getDistrict();
        String state = user.getState();
        String address = "Ward " + ward + "  " + district + "  " + state;
        mAddressView.setText(address);
        String picLocation = user.getImageUrl();
        Log.d("Image url",user.getImageUrl());
       /* final long ONE_MEGABYTE = 1024 * 1024;
        mStorage.child(picLocation).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mProfileImageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/

        Glide.with(getActivity())
                .load(picLocation)
                .apply(new RequestOptions().fitCenter())
                .into(mProfileImageView);


    }
}
