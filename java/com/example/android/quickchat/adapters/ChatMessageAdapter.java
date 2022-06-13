package com.example.android.quickchat.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.quickchat.databinding.ReceivedMsgLayoutBinding;
import com.example.android.quickchat.databinding.SentMsgLayoutBinding;
import com.example.android.quickchat.user.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messageList;
    private final String senderId;

    private static final int SENT_VIEW_TYPE = -1;
    private static final int RECEIVE_VIEW_TYPE = 1;

    public ChatMessageAdapter(List<ChatMessage> messageList, String id){
        this.messageList = messageList;
        this.senderId = id;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == SENT_VIEW_TYPE){
            SentMsgLayoutBinding binding = SentMsgLayoutBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SentMessageViewHolder(binding);
        }
        return new ReceivedMessageViewHolder(ReceivedMsgLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == SENT_VIEW_TYPE){
            ((SentMessageViewHolder) holder).setMessage(messageList.get(position));
        }
        else{
            ((ReceivedMessageViewHolder) holder).setMessage(messageList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (messageList.get(position).senderId.compareTo(senderId) == 0) ? SENT_VIEW_TYPE : RECEIVE_VIEW_TYPE;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final SentMsgLayoutBinding binding;
        public SentMessageViewHolder(SentMsgLayoutBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setMessage(ChatMessage message) {
            binding.sentChatMsg.setText(message.message);
            binding.dateTimeChat.setText(message.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ReceivedMsgLayoutBinding binding;
        public ReceivedMessageViewHolder(ReceivedMsgLayoutBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setMessage(ChatMessage message) {
            binding.receivedChatMsg.setText(message.message);
            binding.dateTimeChat.setText(message.dateTime);
        }
    }
}
