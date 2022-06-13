package com.example.android.quickchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.android.quickchat.adapters.UserAdapter;
import com.example.android.quickchat.databinding.ActivityChooseUserBinding;
import com.example.android.quickchat.user.User;
import com.example.android.quickchat.userListener.MyComparator;
import com.example.android.quickchat.userListener.UserListener;
import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChooseUserActivity extends AppActivity implements UserListener<User> {

    private ActivityChooseUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loading(true);

        setListeners();
        getAllUsers();
    }

    @Override
    public void setOnUserClick(User user){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(UtilityConstants.CHAT_USER, user);
        startActivity(intent);
        finish();
    }

    private void setListeners(){
        binding.chooseUserToolbar.getChildAt(1).setOnClickListener(view -> onBackPressed());
    }

    private void getAllUsers(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(UtilityConstants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> list = new LinkedList<>();
                        SharedPreferenceManager manager = new SharedPreferenceManager(this);
                        String myId = manager.getString(UtilityConstants.KEY_USER_ID);
                        for(QueryDocumentSnapshot snapshot : task.getResult()){
                            if(snapshot.getId().compareTo(myId) == 0){
                                continue;
                            }
                            list.add(new User(
                                    snapshot.getId(),
                                    snapshot.getString(UtilityConstants.KEY_NAME),
                                    snapshot.getString(UtilityConstants.KEY_IMAGE),
                                    snapshot.getString(UtilityConstants.KEY_EMAIL),
                                    snapshot.getString(UtilityConstants.KEY_FCM_TOKEN)));
                        }
                        if(list.size() > 0) {
                            Collections.sort(list, (MyComparator<User>) (object1, object2) -> {
                                int name = object1.name.compareToIgnoreCase(object2.name);
                                int email = object1.email.compareToIgnoreCase(object2.email);
                                return (name == 0) ? email : name;
                            });
                            binding.chooseUserToolbar.setSubtitle(list.size() + " users");
                            UserAdapter adapter = new UserAdapter(list, this);
                            binding.chooseUserView.setAdapter(adapter);
                            binding.chooseUserView.setVisibility(View.VISIBLE);
                        }
                        else{
                            String s = "No user found in the database";
                            binding.textView.setText(s);
                            binding.textView.setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        String s = "" + task.getException();
                        binding.textView.setText(s);
                        binding.textView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loading(boolean flag){
        if(flag){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}





















