package com.example.matrixdiagonizer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PrintResultFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_print_result, container, false);

        PrintResultFragmentArgs args = PrintResultFragmentArgs.fromBundle(getArguments());

        TextView answerTv = view.findViewById(R.id.answerTv);
        answerTv.setText(args.getAnswer());

        return view;
    }
}