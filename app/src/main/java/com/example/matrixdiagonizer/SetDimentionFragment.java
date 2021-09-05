package com.example.matrixdiagonizer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class SetDimentionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_dimention, container, false);

        EditText matrixDimentionEt = view.findViewById(R.id.matrixDimentionEt);
        Button button = view.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer dimention = Integer.parseInt(matrixDimentionEt.getText().toString());

                NavController navController = Navigation.findNavController(view);
                SetDimentionFragmentDirections
                        .ActionSetDimentionFragmentToFillMatrixFragment
                        action = SetDimentionFragmentDirections
                                 .actionSetDimentionFragmentToFillMatrixFragment(dimention);
                Navigation.findNavController(view).navigate(action);
            }
        });

        return view;
    }
}