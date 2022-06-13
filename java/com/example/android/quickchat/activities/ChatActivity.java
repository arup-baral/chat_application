package com.example.android.quickchat.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.android.quickchat.adapters.ChatMessageAdapter;
import com.example.android.quickchat.clientNetwork.ApiClient;
import com.example.android.quickchat.clientNetwork.ApiService;
import com.example.android.quickchat.databinding.ActivityChatBinding;
import com.example.android.quickchat.user.ChatMessage;
import com.example.android.quickchat.user.User;
import com.example.android.quickchat.userListener.MyComparator;
import com.example.android.quickchat.utilities.NetworkUtility;
import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppActivity {

    private ActivityChatBinding binding;
    private User user;

    private SharedPreferenceManager sharedPreferenceManager;
    private FirebaseFirestore database;
    private List<ChatMessage> messageList;
    private ChatMessageAdapter adapter;
    private String chatId = null;
    private boolean userStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);

        user = (User) getIntent().getSerializableExtra(UtilityConstants.CHAT_USER);
        sharedPreferenceManager = new SharedPreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        messageList = new LinkedList<>();
        adapter = new ChatMessageAdapter(messageList, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(adapter);

        loadUserDetails();

        messageListener();

        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        statusListener();
    }

    private void setListeners() {
        binding.chatToolbar.getChildAt(0).setOnClickListener(view -> onBackPressed());
        binding.chatMsgSendButton.setOnClickListener(view -> {
            if (!UtilityConstants.toString(binding.chatMessageEdittext).isEmpty() &&
                    NetworkUtility.isNetworkAvailable(this)
            ) {
                sendMessage(UtilityConstants.toString(binding.chatMessageEdittext));
            }
        });
    }

    private void statusListener() {
        database.collection(UtilityConstants.KEY_COLLECTION_USERS)
                .document(user.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if(error != null){
                        return;
                    }
                    if(value != null){
                        if(value.getLong(UtilityConstants.KEY_STATUS) != null){
                            int flag = Objects.requireNonNull(value.getLong(UtilityConstants.KEY_STATUS)).intValue();
                            userStatus = (flag == 1);
                        }
                        if(userStatus) {
                            binding.chatUserStatus.setVisibility(View.VISIBLE);
                        }
                        else{
                            binding.chatUserStatus.setVisibility(View.GONE);
                        }
                        user.token = value.getString(UtilityConstants.KEY_FCM_TOKEN);
                    }
                });
    }

    private void messageListener(){
        database.collection(UtilityConstants.KEY_COLLECTION_CHAT)
                .whereEqualTo(UtilityConstants.KEY_SENDER_ID, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID))
                .whereEqualTo(UtilityConstants.KEY_RECEIVER_ID, user.id)
                .addSnapshotListener(eventListener);
        database.collection(UtilityConstants.KEY_COLLECTION_CHAT)
                .whereEqualTo(UtilityConstants.KEY_SENDER_ID, user.id)
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
                    messageList.add(new ChatMessage(
                            documentChange.getDocument().getString(UtilityConstants.KEY_SENDER_ID),
                            documentChange.getDocument().getString(UtilityConstants.KEY_RECEIVER_ID),
                            documentChange.getDocument().getString(UtilityConstants.KEY_MESSAGE),
                            UtilityConstants.getTimestamp(Objects.requireNonNull(
                                    documentChange.getDocument().getTimestamp(UtilityConstants.KEY_TIMESTAMP))),
                            documentChange.getDocument().getTimestamp(UtilityConstants.KEY_TIMESTAMP)
                    ));
                }
            }
            int count = messageList.size();
            if(count != 0){
                Collections.sort(messageList, (MyComparator<ChatMessage>) (object1, object2) ->
                        object1.timestamp.compareTo(object2.timestamp));
                adapter.notifyItemRangeInserted(count, count);
                binding.chatRecyclerView.smoothScrollToPosition(count - 1);
            }
            else{
                adapter.notifyDataSetChanged();
            }
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.chatRecyclerView.setVisibility(View.VISIBLE);
        if(chatId == null){
            checkForChatListener();
        }
    };

    private void checkForChatListener() {
        if(messageList.size() != 0) {
            checkForChatListener(sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID), user.id);
            checkForChatListener(user.id, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
        }
    }

    private void checkForChatListener(String senderId, String receiverId) {
        database.collection(UtilityConstants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(UtilityConstants.KEY_SENDER_ID, senderId)
                .whereEqualTo(UtilityConstants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(querySnapshotOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> querySnapshotOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
            chatId = snapshot.getId();
        }
    };

    private void addConversation(Map<String, Object> map) {
        database.collection(UtilityConstants.KEY_COLLECTION_CONVERSATION)
                .add(map)
                .addOnSuccessListener(documentReference -> chatId = documentReference.getId());
    }

    private void updateConversation(String msg) {
        DocumentReference reference = database.collection(UtilityConstants.KEY_COLLECTION_CONVERSATION).document(chatId);
        Map<String, Object> map = new HashMap<>();
        map.put(UtilityConstants.KEY_LAST_MESSAGE, msg);
        map.put(UtilityConstants.KEY_TIMESTAMP, new Timestamp(new Date()));
        reference.update(map);
    }

    private void loadUserDetails() {
        binding.chatImage.setImageBitmap(getBitmapImage(user.image));
        binding.chatUserName.setText(user.name);
    }

    private Bitmap getBitmapImage(String encodedString) {
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void sendMessage(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put(UtilityConstants.KEY_SENDER_ID, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
        map.put(UtilityConstants.KEY_RECEIVER_ID, user.id);
        map.put(UtilityConstants.KEY_MESSAGE, message);
        map.put(UtilityConstants.KEY_TIMESTAMP, new Timestamp(new Date()));
        database.collection(UtilityConstants.KEY_COLLECTION_CHAT).add(map);
        if(chatId != null){
            updateConversation(message);
        }
        else{
            Map<String, Object> map2 = new HashMap<>();
            map2.put(UtilityConstants.KEY_SENDER_ID, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
            map2.put(UtilityConstants.KEY_SENDER_NAME, sharedPreferenceManager.getString(UtilityConstants.KEY_NAME));
            map2.put(UtilityConstants.KEY_SENDER_IMAGE, sharedPreferenceManager.getString(UtilityConstants.KEY_IMAGE));
            map2.put(UtilityConstants.KEY_RECEIVER_ID, user.id);
            map2.put(UtilityConstants.KEY_RECEIVER_NAME, user.name);
            map2.put(UtilityConstants.KEY_RECEIVER_IMAGE, user.image);
            map2.put(UtilityConstants.KEY_LAST_MESSAGE, message);
            map2.put(UtilityConstants.KEY_TIMESTAMP, new Timestamp(new Date()));
            addConversation(map2);
        }
        if(!userStatus) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(user.token);

                JSONObject data = new JSONObject();
                data.put(UtilityConstants.KEY_USER_ID, sharedPreferenceManager.getString(UtilityConstants.KEY_USER_ID));
                data.put(UtilityConstants.KEY_NAME, sharedPreferenceManager.getString(UtilityConstants.KEY_NAME));
                data.put(UtilityConstants.KEY_FCM_TOKEN, sharedPreferenceManager.getString(UtilityConstants.KEY_FCM_TOKEN));
                data.put(UtilityConstants.KEY_MESSAGE, message);

                JSONObject body = new JSONObject();
                body.put(UtilityConstants.REMOTE_MSG_DATA, data);
                body.put(UtilityConstants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            }
            catch(JSONException e){
                showToast(e.getMessage());
            }
        }
        binding.chatMessageEdittext.setText(null);
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                UtilityConstants.getRemoteMsgHeaders(),
                messageBody

        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body() != null) {
                            JSONObject root = new JSONObject(response.body());
                            JSONArray results = root.getJSONArray("results");
                            if(root.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                return;
                            }
                        }
                    }
                    catch(JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }
                else{
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void showToast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}