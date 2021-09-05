package com.example.matrixdiagonizer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

public class FindEigenVectorsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_eigen_vectors, container, false);

        FindEigenVectorsFragmentArgs args = FindEigenVectorsFragmentArgs.fromBundle(getArguments());
        SimpleMatrix matrix = args.getMatrix();

        SimpleMatrix mainMatrix = getMainMatrix(matrix);

        TextView rezultTv = view.findViewById(R.id.GetEigenVectorsTv);
        StringBuffer sb = new StringBuffer();
        sb.append("Власні вектори (нормовані): \n");

        for (int i = 0; i < mainMatrix.eig().getNumberOfEigenvalues(); i++) {
            SimpleMatrix vector = mainMatrix.eig().getEigenVector(i);
            sb.append(getVector(vector) + "\n");
        }

        SimpleMatrix anotherMatrix = mainMatrix.svd().nullSpace();

        rezultTv.setText(sb);

        return view;
    }

    private SimpleMatrix getMainMatrix(@NotNull SimpleMatrix matrix) {
        int size = Math.min(matrix.numCols(), matrix.numRows());
        return matrix.extractMatrix(0, size, 0, size);
    }

    @NotNull
    private StringBuffer getVector(@NotNull SimpleMatrix vector) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < vector.numRows(); i++) {
            sb.append(vector.get(i, 0) + "\n");
        }
        return sb;
    }
}