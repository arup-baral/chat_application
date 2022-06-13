package com.example.android.quickchat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.quickchat.databinding.ItemUserBinding;
import com.example.android.quickchat.user.ChatMessage;
import com.example.android.quickchat.user.User;
import com.example.android.quickchat.userListener.UserListener;

import java.util.List;

public class RecentUserAdapter extends RecyclerView.Adapter<RecentUserAdapter.RecentUserViewHolder> {

    private final List<ChatMessage> userList;
    private final UserListener<User> listener;

    public RecentUserAdapter(List<ChatMessage> userList, UserListener<User> listener){
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecentUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentUserViewHolder(ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentUserViewHolder holder, int position) {
        holder.setRecentUser(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class RecentUserViewHolder extends RecyclerView.ViewHolder {

        private final ItemUserBinding binding;

        public RecentUserViewHolder(ItemUserBinding b){
            super(b.getRoot());
            this.binding = b;
        }

        public void setRecentUser(ChatMessage user) {
            binding.listUserImage.setImageBitmap(getBitmapImage(user.chatImage));
            binding.listUserName.setText(user.chatName);
            binding.listUserMessage.setText(user.lastMessage);
            binding.getRoot().setOnClickListener(view -> {
                User user1 = new User();
                user1.id = user.chatId;
                user1.name = user.chatName;
                user1.image = user.chatImage;
                listener.setOnUserClick(user1);
            });
        }

        private Bitmap getBitmapImage(String encodedString){
            byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
