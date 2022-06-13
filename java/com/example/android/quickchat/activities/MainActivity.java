package com.example.android.quickchat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.android.quickchat.R;
import com.example.android.quickchat.adapters.RecentUserAdapter;
import com.example.android.quickchat.databinding.ActivityMainBinding;
import com.example.android.quickchat.user.ChatMessage;
import com.example.android.quickchat.user.User;
import com.example.android.quickchat.userListener.MyComparator;
import com.example.android.quickchat.userListener.UserListener;
import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppActivity implements UserListener<User> {

    private ActivityMainBinding binding;
    private SharedPreferenceManager sharedPreferenceManager;

    private List<ChatMessage> recentUsersList;
    private RecentUserAdapter adapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferenceManager = new SharedPreferenceManager(this);
        recentUsersList = new LinkedList<>();
        adapter = new RecentUserAdapter(recentUsersList, this);
        binding.listRecentUsers.setAdapter(adapter);
        database = FirebaseFirestore.getInstance();

        listenMessage();

        try {
            setListeners();
        } catch (Exception e) {
            System.out.println("Message: " + e.getMessage() + "\n");
            System.out.println("StackTrace: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void setOnUserClick(User user){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(UtilityConstants.CHAT_USER, user);
        startActivity(intent);
    }

    private void setListeners() {
        //when users sign in
        getToken();

        //Sign Out option item
        binding.toolbarView.getMenu().getItem(1).setOnMenuItemClickListener(
                item -> {
                    View view = LayoutInflater.from(getApplicationContext())
                            .inflate(R.layout.bottom_sheet, findViewById(R.id.bottom_sheet_layout));
                    BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
                    dialog.setContentView(view);
                    dialog.show();
                    view.findViewById(R.id.sign_out_text_button).setOnClickListener(v -> {
                        signOut();
                        Intent intent = new Intent(this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        super.finish();
                    });
                    view.findViewById(R.id.cancel_text_button).setOnClickListener(v ->
                            dialog.dismiss()
                    );
                    return true;
                }
        );

        binding.toolbarView.getMenu().getItem(0).setOnMenuItemClickListener(
                item -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
        );

        binding.addUserFloatingButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChooseUserActivity.class);
            startActivity(intent);
        });
    }

    private void listenMessage(){
        database.collection(UtilityConstants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(UtilityConstants.KEY_SENDER_ID, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(UtilityConstants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(UtilityConstants.KEY_RECEIVER_ID, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(UtilityConstants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(UtilityConstants.KEY_RECEIVER_ID);
                    ChatMessage user = new ChatMessage();
                    user.senderId = senderId;
                    user.receiverId = receiverId;
                    if(sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID).equals(senderId)){
                        user.chatName = documentChange.getDocument().getString(UtilityConstants.KEY_RECEIVER_NAME);
                        user.chatImage = documentChange.getDocument().getString(UtilityConstants.KEY_RECEIVER_IMAGE);
                        user.chatId = documentChange.getDocument().getString(UtilityConstants.KEY_RECEIVER_ID);
                    }
                    else{
                        user.chatId = documentChange.getDocument().getString(UtilityConstants.KEY_SENDER_ID);
                        user.chatName = documentChange.getDocument().getString(UtilityConstants.KEY_SENDER_NAME);
                        user.chatImage = documentChange.getDocument().getString(UtilityConstants.KEY_SENDER_IMAGE);
                    }
                    user.lastMessage = documentChange.getDocument().getString(UtilityConstants.KEY_LAST_MESSAGE);
                    user.timestamp = documentChange.getDocument().getTimestamp(UtilityConstants.KEY_TIMESTAMP);
                    recentUsersList.add(user);
                }
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i=0; i<recentUsersList.size(); i++){
                        String senderId = documentChange.getDocument().getString(UtilityConstants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(UtilityConstants.KEY_RECEIVER_ID);
                        if(recentUsersList.get(i).senderId.equals(senderId) && recentUsersList.get(i).receiverId.equals(receiverId)){
                            recentUsersList.get(i).lastMessage = documentChange.getDocument().getString(UtilityConstants.KEY_LAST_MESSAGE);
                            recentUsersList.get(i).timestamp = documentChange.getDocument().getTimestamp(UtilityConstants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(recentUsersList, (MyComparator<ChatMessage>) (a, b) -> b.timestamp.compareTo(a.timestamp));
            adapter.notifyDataSetChanged();
            binding.listRecentUsers.smoothScrollToPosition(0);
            binding.listRecentUsers.setVisibility(View.VISIBLE);
        }
    };

    private void updateToken(String token) {
        sharedPreferenceManager.putString(UtilityConstants.KEY_FCM_TOKEN, token);
        DocumentReference reference = FirebaseFirestore.getInstance().collection(UtilityConstants.KEY_COLLECTION_USERS)
                .document(sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
        reference.update(UtilityConstants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update token\n" + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection(UtilityConstants.KEY_COLLECTION_USERS)
                .document(sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
        HashMap<String, Object> map = new HashMap<>();
        map.put(UtilityConstants.KEY_FCM_TOKEN, FieldValue.delete());
        reference.update(map)
                .addOnFailureListener(e -> Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show());

        FirebaseAuth.getInstance().signOut();
        sharedPreferenceManager.putBoolean(UtilityConstants.KEY_IS_SIGNED_IN, false);
    }
}