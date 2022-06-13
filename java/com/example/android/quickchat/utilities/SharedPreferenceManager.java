package com.example.android.quickchat.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(UtilityConstants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void putString(String key, String value){
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key){
        return sharedPreferences.getString(key, "");
    }

    public void putBoolean(String key, boolean value){
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }
}
