package com.example.android.quickchat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppActivity extends AppCompatActivity {

    private DocumentReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferenceManager manager = new SharedPreferenceManager(this);
        reference = FirebaseFirestore.getInstance().collection(UtilityConstants.KEY_COLLECTION_USERS)
                .document(manager.getString(UtilityConstants.KEY_USER_ID));
    }

    @Override
    protected void onResume() {
        super.onResume();
        reference.update(UtilityConstants.KEY_STATUS, 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.update(UtilityConstants.KEY_STATUS, 0);
    }
}
