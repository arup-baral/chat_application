package com.example.android.quickchat.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.quickchat.R;
import com.example.android.quickchat.databinding.ActivitySignUpBinding;
import com.example.android.quickchat.helper.NonEmptyChecker;
import com.example.android.quickchat.utilities.SharedPreferenceManager;
import com.example.android.quickchat.utilities.UtilityConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private static final String ERROR_MSG = "This field cannot be empty ! ";

    private ActivitySignUpBinding activitySignUpBinding;
    private SharedPreferenceManager sharedPreferenceManager;

    private FirebaseAuth auth;

    private String encodedImage;
    private String[] userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(activitySignUpBinding.getRoot());

        sharedPreferenceManager = new SharedPreferenceManager(this);
        auth = FirebaseAuth.getInstance();

        //Checking for all listeners
        setListener();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            activitySignUpBinding.aliasImage.setImageBitmap(bitmap);
                            activitySignUpBinding.iconCamera.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void loading(boolean flag) {
        if (flag) {
            activitySignUpBinding.signupProgress.setVisibility(View.VISIBLE);
            activitySignUpBinding.signupButton.setVisibility(View.INVISIBLE);
            activitySignUpBinding.signupButton.setEnabled(false);
        } else {
            activitySignUpBinding.signupProgress.setVisibility(View.INVISIBLE);
            activitySignUpBinding.signupButton.setVisibility(View.VISIBLE);
            activitySignUpBinding.signupButton.setEnabled(true);
        }
    }

    private void setListener() {
        //Checking for non-empty of edittext field
        activitySignUpBinding.signupFirstName.addTextChangedListener(new NonEmptyChecker(activitySignUpBinding.signupFirstName));
        activitySignUpBinding.signupLastName.addTextChangedListener(new NonEmptyChecker(activitySignUpBinding.signupLastName));

        //Checking for validity of email
        activitySignUpBinding.signupEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void afterTextChanged(Editable editable) {
                Drawable check_status;
                if (UtilityConstants.toString(activitySignUpBinding.signupEmail).length() != 0) {
                    if (Patterns.EMAIL_ADDRESS.matcher(UtilityConstants.toString(activitySignUpBinding.signupEmail)).matches()) {
                        check_status = activitySignUpBinding.signupEmail.getContext().getResources().getDrawable(R.drawable.ic_baseline_check_circle_24);
                    } else {
                        check_status = activitySignUpBinding.signupEmail.getContext().getResources().getDrawable(R.drawable.ic_baseline_cancel_24);
                    }
                } else {
                    check_status = null;
                }
                activitySignUpBinding.signupEmail.setCompoundDrawablesWithIntrinsicBounds(null, null, check_status, null);
            }
        });

        // Check validity of password
        activitySignUpBinding.signupPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isPasswordValid(activitySignUpBinding.signupPassword.getText().toString().trim())) {
                    activitySignUpBinding.signupPassword.setCompoundDrawablesWithIntrinsicBounds
                            (0, 0, R.drawable.ic_baseline_check_circle_24, 0);
                } else {
                    activitySignUpBinding.signupPassword.setCompoundDrawablesWithIntrinsicBounds
                            (0, 0, 0, 0);
                }
            }
        });

        activitySignUpBinding.signupConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (activitySignUpBinding.signupConfirmPassword.getText().toString().trim()
                        .compareTo(activitySignUpBinding.signupPassword.getText().toString().trim()) == 0) {
                    activitySignUpBinding.signupConfirmPassword.setCompoundDrawablesWithIntrinsicBounds
                            (0, 0, R.drawable.ic_baseline_check_circle_24, 0);
                } else {
                    activitySignUpBinding.signupConfirmPassword.setCompoundDrawablesWithIntrinsicBounds
                            (0, 0, 0, 0);
                }
            }
        });

        activitySignUpBinding.signInPageNavigator.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            super.finish();
        });

        activitySignUpBinding.signupButton.setOnClickListener(view -> {
            if (checkValidityOfSignUpDetails()) {
                loading(true);
                userData = new String[]{
                        encodedImage,
                        UtilityConstants.toString(activitySignUpBinding.signupFirstName).concat(" ")
                                .concat(UtilityConstants.toString(activitySignUpBinding.signupLastName)),
                        UtilityConstants.toString(activitySignUpBinding.signupEmail),
                        UtilityConstants.toString(activitySignUpBinding.signupPassword)
                };
                auth.fetchSignInMethodsForEmail(userData[2])
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean flag = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getSignInMethods()).isEmpty();
                                if (!flag) {
                                    activitySignUpBinding.signupEmail.setError(
                                            "The mail is associated with another account. If it is not you change your email password ! ");
                                    loading(false);
                                    activitySignUpBinding.signupEmail.requestFocus();
                                } else {
                                    registerUser();
                                }
                            }
                        });
            }
        });

        activitySignUpBinding.aliasImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImageLauncher.launch(intent);
        });
    }

    private void registerUser() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> map = new HashMap<>();
        map.put(UtilityConstants.KEY_IMAGE, userData[0]);
        map.put(UtilityConstants.KEY_NAME, userData[1]);
        map.put(UtilityConstants.KEY_EMAIL, userData[2]);

        auth.createUserWithEmailAndPassword(userData[2], userData[3])
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        database.collection(UtilityConstants.KEY_COLLECTION_USERS)
                                .add(map)
                                .addOnSuccessListener(documentReference -> {
                                    sharedPreferenceManager.putBoolean(UtilityConstants.KEY_IS_SIGNED_IN, true);
                                    sharedPreferenceManager.putString(UtilityConstants.KEY_USER_ID, documentReference.getId());
                                    sharedPreferenceManager.putString(UtilityConstants.KEY_NAME, userData[1]);
                                    sharedPreferenceManager.putString(UtilityConstants.KEY_IMAGE, userData[0]);
                                    sharedPreferenceManager.putString(UtilityConstants.KEY_EMAIL, userData[2]);
                                    sharedPreferenceManager.putBoolean(UtilityConstants.IS_EMAIL_VERIFIED, false);

                                    loading(false);

                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(
                                            this, "Failed to register: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    loading(false);
                                });
                    } else {
                        Toast.makeText(this, "" + task.getException(), Toast.LENGTH_LONG).show();
                        loading(false);
                    }
                });
    }

    private String encodeImage(Bitmap bitmap) {
        int width = 150;
        int height = width * bitmap.getHeight() / bitmap.getWidth();
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private boolean checkValidityOfSignUpDetails() {
        if (encodedImage == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Important ! ")
                    .setMessage("Select a Profile Image ")
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();
            return false;
        }
        if (UtilityConstants.toString(activitySignUpBinding.signupFirstName).isEmpty()) {
            activitySignUpBinding.signupFirstName.setError(ERROR_MSG);
            return false;
        }
        if (UtilityConstants.toString(activitySignUpBinding.signupLastName).isEmpty()) {
            activitySignUpBinding.signupLastName.setError(ERROR_MSG);
            return false;
        }
        if (UtilityConstants.toString(activitySignUpBinding.signupEmail).isEmpty()) {
            activitySignUpBinding.signupEmail.setError(ERROR_MSG);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(UtilityConstants.toString(activitySignUpBinding.signupEmail)).matches()) {
            activitySignUpBinding.signupEmail.setError("Email address is not valid ! ");
            return false;
        }
        String password = UtilityConstants.toString(activitySignUpBinding.signupPassword);
        if (password.length() < 8 || password.length() > 16) {
            activitySignUpBinding.signupPassword.setError("Password length should be between 8 and 16 ");
            return false;
        } else {
            if (!isPasswordValid(password)) {
                activitySignUpBinding.signupPassword.setError("Password should contain -> A-Z, a-z, 0-9 and special character ");
                return false;
            }
        }
        if (UtilityConstants.toString(activitySignUpBinding.signupPassword).compareTo(password) != 0) {
            activitySignUpBinding.signupConfirmPassword.setError("Password is not matched ! ");
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String pw) {
        if (pw.length() < 8 || pw.length() > 16) {
            return false;
        }
        boolean caps = false, small = false, digit = false, spc = false;
        for (char c : pw.toCharArray()) {
            int ch = c;
            if (c == (char) 32) {
                Toast.makeText(this, "Password should not contain space !", Toast.LENGTH_SHORT).show();
                return false;
            } else if (c >= 'A' && c <= 'Z') {
                caps = true;
            } else if (c >= 'a' && c <= 'z') {
                small = true;
            } else if (c >= '0' && c <= '9') {
                digit = true;
            } else if ((ch >= 33 && ch <= 47) || (ch >= 58 && ch <= 64) || (ch >= 91 && ch <= 96) || (ch >= 123 && ch <= 126)) {
                spc = true;
            }
            if (caps && small && digit && spc) {
                return true;
            }
        }
        return false;
    }
}