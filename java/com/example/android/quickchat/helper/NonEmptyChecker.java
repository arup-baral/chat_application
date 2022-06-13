package com.example.android.quickchat.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.android.quickchat.R;

public class NonEmptyChecker implements TextWatcher {

    private final EditText view;

    public NonEmptyChecker(EditText view){
        this.view = view;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}


    @Override
    public void afterTextChanged(Editable editable) {
        if(view.getText().toString().trim().length() != 0){
            view.setCompoundDrawablesWithIntrinsicBounds
                    (0, 0, R.drawable.ic_baseline_check_circle_24, 0);
        }
        else{
            view.setCompoundDrawablesWithIntrinsicBounds
                    (0, 0, 0, 0);
        }
    }
}
