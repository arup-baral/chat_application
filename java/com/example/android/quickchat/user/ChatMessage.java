package com.example.android.quickchat.user;

import com.google.firebase.Timestamp;

public class ChatMessage {
    public String senderId, receiverId, message, dateTime;
    public Timestamp timestamp;
    public String chatId, chatName, chatImage, lastMessage;

    public ChatMessage(String senderId, String receiverId, String message, String dateTime, Timestamp timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
    }

    public ChatMessage() {}
}
