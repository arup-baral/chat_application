package com.example.android.quickchat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.android.quickchat.R;
import com.example.android.quickchat.databinding.ActivityProfileBinding;
import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppActivity {

    private ActivityProfileBinding binding;
    private SharedPreferenceManager manager;
    private FirebaseFirestore database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        manager = new SharedPreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        auth.addAuthStateListener(onCompleteListener);

        loadUserDetails();

        setListeners();
    }

    @Override
    protected void onResume(){
        super.onResume();
        auth.addAuthStateListener(onCompleteListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(onCompleteListener);
    }

    private final FirebaseAuth.AuthStateListener onCompleteListener = task -> {

        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            if(user.isEmailVerified()){
                manager.putBoolean(UtilityConstants.IS_EMAIL_VERIFIED, true);
                binding.verificationEmailTextView.setText(R.string.email_is_verified);
                binding.verificationEmailTextView.setTextColor(getResources().getColor(R.color.green_700));
            }
            else{
                binding.verificationEmailTextView.setText(R.string.email_is_not_verified);
                binding.verificationEmailTextView.setTextColor(getResources().getColor(R.color.red_900));
            }
        }

    };

    private void setListeners() {
        binding.refreshButton.setOnClickListener(v -> refreshUserDetails());
        binding.verificationEmailTextView.setOnClickListener(view -> {
            if (!manager.getBoolean(UtilityConstants.IS_EMAIL_VERIFIED)) {
                FirebaseUser user = auth.getCurrentUser();
                while (user == null) {
                    user = auth.getCurrentUser();
                }
                user.sendEmailVerification()
                        .addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(),
                                "Verification link is sent to email", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                                e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void refreshUserDetails(){
        auth.addAuthStateListener(onCompleteListener);
    }

    private void loadUserDetails() {
        binding.profileNameTextView.setText(manager.getString(UtilityConstants.KEY_NAME));
        binding.profileEmailTextView.setText(manager.getString(UtilityConstants.KEY_EMAIL));
        binding.profileImageView.setImageBitmap(getBitmapImage(manager.getString(UtilityConstants.KEY_IMAGE)));
    }

    private Bitmap getBitmapImage(String encodedString) {
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}