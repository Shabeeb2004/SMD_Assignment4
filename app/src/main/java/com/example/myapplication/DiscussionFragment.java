package com.example.myapplication;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DiscussionFragment extends Fragment {

    public static DiscussionFragment newInstance(int projectId) {
        DiscussionFragment f = new DiscussionFragment();
        Bundle b = new Bundle();
        b.putInt("project_id", projectId);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        TextView tv = new TextView(requireContext());
        tv.setText("Coming Soon");
        tv.setTextSize(22);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return tv;
    }
}