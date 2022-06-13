package com.example.android.quickchat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.quickchat.databinding.ChooseUserItemBinding;
import com.example.android.quickchat.user.User;
import com.example.android.quickchat.userListener.UserListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final UserListener<User> listener;

    public UserAdapter(List<User> list, UserListener<User> ul){
        this.userList = list;
        this.listener = ul;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChooseUserItemBinding binding = ChooseUserItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private final ChooseUserItemBinding binding;
        public UserViewHolder(ChooseUserItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setUserData(User user){
            binding.chooseUserImage.setImageBitmap(getBitmapImage(user.image));
            binding.chooseUserName.setText(user.name);
            binding.chooseUserEmail.setText(user.email);
            binding.getRoot().setOnClickListener(view -> listener.setOnUserClick(user));
        }
    }

    private Bitmap getBitmapImage(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}



























