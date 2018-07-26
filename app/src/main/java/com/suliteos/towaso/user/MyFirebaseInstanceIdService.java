package com.suliteos.towaso.user;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
        User user = gson.fromJson(json, User.class);
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("token",token);
            tokenMap.put("name",mUser.getDisplayName());
            tokenMap.put("email",mUser.getEmail());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Tokens").document("User").collection("Token").document(mUser.getUid()).set(tokenMap, SetOptions.merge());
            db.collection("Tokens").document("User").collection(user.getState()).document(user.getDistrict()).collection(String.valueOf(user.getWard())).document(user.getArea()).collection("Token").document(mUser.getUid()).set(tokenMap,SetOptions.merge());
            db.collection("User").document(mUser.getUid()).set(tokenMap,SetOptions.merge());
        }
    }
}
