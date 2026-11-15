package com.example.myapplication;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {
    @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
    @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
    @Override public void afterTextChanged(Editable s) { onChanged(s.toString()); }
    public abstract void onChanged(String text);
}