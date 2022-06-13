package com.example.android.quickchat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.quickchat.databinding.ActivitySignInBinding;
import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferenceManager = new SharedPreferenceManager(this);

        if(sharedPreferenceManager.getBoolean(UtilityConstants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            super.finish();
        }

        setListener();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    private void setListener(){
        binding.createNewAccountText.setOnClickListener(view -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            super.finish();
        });

        binding.loginButton.setOnClickListener(view -> {
            if(checkForValidSignInDetails()){
                loading(true);
                signIn();
            }
        });

        binding.forgotPasswordText.setOnClickListener(view -> {
            EditText emailEdit = new EditText(this);
            emailEdit.setHint("Email");
            emailEdit.requestFocus();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Forgot Password")
                    .setMessage("Enter your Email Address")
                    .setView(emailEdit)
                    .setPositiveButton("Send", (dialogInterface, i) -> {

                        if(UtilityConstants.toString(emailEdit).isEmpty()){
                            Toast.makeText(this, "Provide your Email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(UtilityConstants.toString(emailEdit))
                                .addOnSuccessListener(signInMethodQueryResult -> {
                                    boolean flag = Objects.requireNonNull(signInMethodQueryResult.getSignInMethods()).isEmpty();
                                    if (!flag) {
                                        FirebaseAuth.getInstance().sendPasswordResetEmail(UtilityConstants.toString(emailEdit))
                                                .addOnSuccessListener(unused ->
                                                        Toast.makeText(this, "Email has been sent", Toast.LENGTH_LONG)
                                                                .show());
                                    }
                                    else {
                                        Toast.makeText(this, "Email is not associated with any account", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());

                        dialogInterface.cancel();
                    })
                    .setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .setCancelable(false)
                    .create()
                    .show();
        });
    }

    private void loading(boolean flag){
        if(flag){
            binding.loginButton.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.loginButton.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void signIn(){
       FirebaseFirestore database = FirebaseFirestore.getInstance();
       database.collection(UtilityConstants.KEY_COLLECTION_USERS)
               .whereEqualTo(UtilityConstants.KEY_EMAIL, UtilityConstants.toString(binding.loginEmail))
               .get()
               .addOnCompleteListener(task -> {
                   loading(false);
                  if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                      DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                      FirebaseAuth.getInstance().signInWithEmailAndPassword(UtilityConstants.toString(binding.loginEmail),
                              UtilityConstants.toString(binding.loginPassword))
                              .addOnSuccessListener(authResult -> {
                                  sharedPreferenceManager.putBoolean(UtilityConstants.KEY_IS_SIGNED_IN, true);

                                  sharedPreferenceManager.putString(UtilityConstants.KEY_USER_ID, snapshot.getId());

                                  sharedPreferenceManager.putString(UtilityConstants.KEY_NAME,
                                          snapshot.getString(UtilityConstants.KEY_NAME));

                                  sharedPreferenceManager.putString(UtilityConstants.KEY_IMAGE,
                                          snapshot.getString(UtilityConstants.KEY_IMAGE));

                                  sharedPreferenceManager.putString(UtilityConstants.KEY_EMAIL,
                                          snapshot.getString(UtilityConstants.KEY_EMAIL));

                                  Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                  startActivity(intent);
                                  super.finish();
                              })
                              .addOnFailureListener(e -> Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show());

                  }
                  else{
                      Toast.makeText(this, "Check your credentials ! ", Toast.LENGTH_LONG).show();
                  }
               });
    }

    private boolean checkForValidSignInDetails(){
        if(binding.loginEmail.getText().toString().trim().length() == 0){
            binding.loginEmail.setError("Email cannot be empty ! ");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(binding.loginEmail.getText().toString()).matches()){
            binding.loginEmail.setError("Enter valid Email Address");
            return false;
        }
        if(binding.loginPassword.getText().toString().trim().length() == 0){
            binding.loginPassword.setError("Enter password");
            return false;
        }
        return true;
    }

}