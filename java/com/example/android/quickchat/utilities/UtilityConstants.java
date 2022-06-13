package com.example.android.quickchat.utilities;

import android.widget.EditText;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class UtilityConstants {
    private UtilityConstants() {}
    public static final String KEY_COLLECTION_USERS = "registeredUsers";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PREFERENCE_NAME = "quickChatPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String IS_EMAIL_VERIFIED = "isEmailVerified";
    public static final String CHAT_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chatCollection";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATION = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_STATUS = "status";
    public static final String REMOTE_MGS_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MGS_AUTHORIZATION,
                    "key=AAAAEc1r_9A:APA91bFHxvAclyc4NCcrMqkJqOptqyYJ4MKvBlbUrmHEGlWdDVM_QVdiM8TS7B2wrGhOqkJRxyjvaJyQIUDJcR3BZw5h6LVxT8h1MvOKYyiYk1mDLg3PnNFmDjGOYxP8PI6lr8mext1-");
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,
                    "application/json");
        }
        return remoteMsgHeaders;
    }

    public static String toString(EditText view){
        return view.getText().toString().trim();
    }
    public static String getTimestamp(Timestamp date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault());
        return dateFormat.format(date.toDate());
    }
}
