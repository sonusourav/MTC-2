package com.suliteos.towaso.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserEmailView, mUserPhoneView;
    private boolean mLoggingIn;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mUserEmailView = findViewById(R.id.login_email);
        mUserPhoneView = findViewById(R.id.login_phone);
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });

        db = FirebaseFirestore.getInstance();
    }

    private void checkData() {
        if (mLoggingIn) {
            return;
        }
        mUserEmailView.setError(null);
        mUserPhoneView.setError(null);

        String email = mUserEmailView.getText().toString().toLowerCase().trim();
        String phone = mUserPhoneView.getText().toString().trim();

        boolean cancelLogin = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)){
            mUserEmailView.setError(getString(R.string.field_required));
            focusView = mUserEmailView;
            cancelLogin = true;
        }else if(!isEmailValid(email)){
            mUserEmailView.setError(getString(R.string.invalid_email));
            focusView = mUserEmailView;
            cancelLogin = true;
        }

        if (TextUtils.isEmpty(phone)) {
            mUserPhoneView.setError(getString(R.string.field_required));
            focusView = mUserPhoneView;
            cancelLogin = true;
        }else if (!(isPhoneValid(phone))){
            mUserPhoneView.setError(getString(R.string.invalid_phone));
            focusView = mUserPhoneView;
            cancelLogin = true;
        }

        if (cancelLogin) {
            focusView.requestFocus();
        } else {
            mLoggingIn = true;
            login(email,phone);
            hideKeyboard();
        }
    }

    private void login(String email, String phone) {
        final ShowDialog showDialog = new ShowDialog(this);
        showDialog.setTitle("Logging");
        showDialog.setMessage("Checking Credentials...");
        showDialog.show();
        mAuth.signInWithEmailAndPassword(email,phone).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                showDialog.setMessage("Downloading Data...");
                db.collection("User").document(authResult.getUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.data_file_json_key),json);
                        editor.apply();

                        Analytics.setUserProperty(LoginActivity.this,"ward",String.valueOf(user.getWard()));
                        Analytics.subscribeToTopic(String.valueOf(user.getWard()));
                        Analytics.setUserProperty(LoginActivity.this,"state",user.getState());
                        Analytics.subscribeToTopic(user.getState());
                        Analytics.setUserProperty(LoginActivity.this,"district",user.getDistrict());
                        Analytics.subscribeToTopic(user.getDistrict());
                        Analytics.setUserProperty(LoginActivity.this,"perMonth",String.valueOf(user.getPerMonth()));
                        Analytics.subscribeToTopic(String.valueOf(user.getPerMonth()));
                        showDialog.cancel();
                        switchToMainActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mLoggingIn = false;
                        mAuth.signOut();
                        showDialog.cancel();
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mLoggingIn = false;
                showDialog.cancel();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void switchToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(mUserEmailView.getWindowToken(), 0);
    }

    private boolean isPhoneValid(String phone) {

        return phone.length() == 10 && (phone.startsWith("7") || phone.startsWith("8") || phone.startsWith("9"));
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*" + "@"
                + "\\w+([-.]\\w+)*" + "\\." + "\\w+([-.]\\w+)*");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
