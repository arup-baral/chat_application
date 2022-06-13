package com.example.android.quickchat.userListener;

import java.util.Comparator;

public interface MyComparator<T> extends Comparator<T> {
    @Override
    int compare(T object1, T object2);
}
