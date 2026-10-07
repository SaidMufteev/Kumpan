package com.example.kumpan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class TestsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setText("Доступные тесты");
        textView.setTextSize(24);
        textView.setTextColor(0xFF2D3436);
        textView.setGravity(android.view.Gravity.CENTER);
        return textView;
    }
}