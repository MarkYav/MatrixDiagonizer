package com.example.matrixdiagonizer;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.ejml.data.Complex_F64;
import org.ejml.equation.Equation;
import org.ejml.equation.Sequence;
import org.ejml.equation.Variable;
import org.ejml.equation.VariableType;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FillMatrixFragment extends Fragment implements View.OnClickListener {

    private View view;
    private int rowNum;
    private int colNum;
    private EditText[][] editTexts;
    EditTextMatrix editTextMatrix = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fill_matrix, container, false);

        // getting axes to the matrix dimension
        FillMatrixFragmentArgs args = FillMatrixFragmentArgs.fromBundle(getArguments());

        // initialize values
        rowNum = args.getMatrixDimension();
        colNum = args.getMatrixDimension();
        editTexts = new EditText[rowNum][colNum];

        // setup grid layout
        GridLayout gridLayout = view.findViewById(R.id.gridLayout);
        gridLayout.setRowCount(rowNum);
        gridLayout.setColumnCount(colNum);

        // create and set up EditTexts in the grid layout
        if (savedInstanceState != null) {
            editTextMatrix = savedInstanceState.getParcelable("EditTextMatrix");
        }
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                editTexts[i][j] = new EditText(view.getContext());
                setPos(editTexts[i][j], i, j);
                if (editTextMatrix != null) {
                    editTexts[i][j].setText(editTextMatrix.editTexts[i][j].getText());
                }
                gridLayout.addView(editTexts[i][j]);
            }
        }

        // setting onClickListeners to buttons
        view.findViewById(R.id.FindEigenValuesBtn).setOnClickListener(this);
        view.findViewById(R.id.FindEigenVectorsBtn).setOnClickListener(this);
        view.findViewById(R.id.FindJordanBasisBtn).setOnClickListener(this);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {

        // create user's matrix and fill it with values
        SimpleMatrix matrix = new SimpleMatrix(rowNum, colNum);
        for (int i = 0; i < matrix.numRows(); i++) {
            for (int j = 0; j < matrix.numCols(); j++) {
                matrix.set(i, j, getEditTextValue(editTexts[i][j]));
            }
        }

        // saving data
        if (editTextMatrix == null) {
            editTextMatrix = new EditTextMatrix();
        }
        editTextMatrix.editTexts = editTexts;
        getArguments().putParcelable("EditTextMatrix", editTextMatrix);


        StringBuffer sb = new StringBuffer();

        switch (v.getId()) {
            case R.id.FindEigenValuesBtn: {
                // getting eigen values
                Map<Double, Integer> eigenValues = getMapOfEigenValues(getMainMatrix(matrix));

                // filling sb
                sb.append("Власні числа: \n");
                for (Double eigenValue : eigenValues.keySet()) {
                    sb.append("власне число " + eigenValue +
                            " кратності " + eigenValues.get(eigenValue) + "\n");
                }

                break;
            }
            case R.id.FindEigenVectorsBtn: {
                SimpleMatrix nullSpaceOfMatrix = decreaseDiagonal(getMainMatrix(matrix), 0d).svd().nullSpace();

                sb.append("Власні вектори (нормовані): \n\n");
                for (int i = 0; i < nullSpaceOfMatrix.numCols(); i++) {
                    SimpleMatrix vector = nullSpaceOfMatrix.extractVector(false, i);
                    sb.append(getVector(vector) + "\n");
                }
                if (nullSpaceOfMatrix.numCols() == 0) {
                    sb.append("Розмірність ядра оператора\nдорівнює нулю.\n(Система не має вільних змінних\nабо умови системи задовільняє тільки\nтривіальний розв'язок)");
                }

                break;
            }
            case R.id.FindJordanBasisBtn:
                // in Pair: first is basis, second is JordanMatrix
                Pair<SimpleMatrix, SimpleMatrix> pair = getJordanBasis(matrix);
                SimpleMatrix JordanMatrix = pair.second;
                SimpleMatrix JordanBase = pair.first;

                sb.append("Жорданова матриця (J):\n");
                sb.append(printMatrix(JordanMatrix) + "\n");

                sb.append("Жордановий базис (U):\n");
                sb.append(printMatrix(JordanBase) + "\n");

                sb.append("Де J = U^(-1) * A * U");

                break;
            default:
                Toast.makeText(v.getContext(), "Нажата неизвестная кнопка!", Toast.LENGTH_LONG).show();
                return;
        }

        FillMatrixFragmentDirections.ActionFillMatrixFragmentToPrintResultFragment
                action = FillMatrixFragmentDirections
                .actionFillMatrixFragmentToPrintResultFragment(sb.toString());
        Navigation.findNavController(view).navigate(action);
    }

    //putting the edit text according to row and column index
    private void setPos(@NotNull EditText editText, int row, int column) {
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.width = 120;
        param.setGravity(Gravity.CENTER);
        param.rowSpec = GridLayout.spec(row);
        param.columnSpec = GridLayout.spec(column);
        param.setMargins(2,2,2,2);
        editText.setLayoutParams(param);

        editText.setHint("0");
        //editText.setBackgroundColor(Color.parseColor("#000000"));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_SIGNED |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);

    }

    private double getEditTextValue(@NotNull EditText editText) {
        String s = editText.getText().toString();
        if (s.equals("")) {
            return 0;
        } else {
            return Double.parseDouble(s);
        }
    }

    @NotNull
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Map<Double, Integer> getMapOfEigenValues(SimpleMatrix matrix) {

        Map<Double, Integer> eigenValues = new HashMap<>();

        // setting the map with values
        for (Complex_F64 eigenValue : getMainMatrix(matrix).eig().getEigenvalues()) {
            eigenValues.merge(smartRound(eigenValue), 1, Integer::sum);
        }

        return eigenValues;
    }

    private Double smartRound(@NotNull Complex_F64 eigenValue) {
        Double value = eigenValue.getReal();
        Double valueRound = new BigDecimal(value).setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
        if (Math.abs(value - valueRound) < 0.001d) {
            value = valueRound;
        }

        return value;
    }

    private Double smartRound(double eigenValue) {
        Double value = eigenValue;
        Double valueRound = new BigDecimal(value).setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
        if (Math.abs(value - valueRound) < 0.001d) {
            value = valueRound;
        }

        return value;
    }

    private SimpleMatrix getMainMatrix(@NotNull SimpleMatrix matrix) {
        int size = Math.min(matrix.numCols(), matrix.numRows());
        return matrix.extractMatrix(0, size, 0, size);
    }

    @NotNull
    private SimpleMatrix decreaseDiagonal(@NotNull SimpleMatrix m, double eigenValue) {
        SimpleMatrix matrix = new SimpleMatrix(m);
        for (int i = 0; i < matrix.numRows(); i++) {
            matrix.set(i, i, matrix.get(i, i) - eigenValue);
        }
        return matrix;
    }

    @NotNull
    private StringBuffer getVector(@NotNull SimpleMatrix vector) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < vector.numRows(); i++) {
            sb.append(vector.get(i, 0) + "\n");
        }
        return sb;
    }

    @NotNull
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Pair<SimpleMatrix, SimpleMatrix> getJordanBasis(SimpleMatrix matrix) {
        Map<Double, Integer> eigenValues = getMapOfEigenValues(getMainMatrix(matrix));
        Pair<SimpleMatrix, SimpleMatrix> pair = new Pair<>(new SimpleMatrix(matrix.numRows(), 0),
                new SimpleMatrix(matrix.numRows(), matrix.numRows()));

        for (Double eigenValue : eigenValues.keySet()) {
            SimpleMatrix tempMatrix = decreaseDiagonal(matrix, eigenValue);
            pair = getLocalBasis(tempMatrix, pair, tempMatrix, 1, 0, eigenValues.get(eigenValue), eigenValue);
        }

        return pair;
    }

    private Pair<SimpleMatrix, SimpleMatrix> getLocalBasis(
            @NotNull SimpleMatrix tempMatrix, Pair<SimpleMatrix, SimpleMatrix> pair,
            SimpleMatrix startMatrix,
            int height, int currentNumberOfVectors, int targetNumberOfVectors, double eigenValue) {

        SimpleMatrix JordanMatrix = pair.second;
        SimpleMatrix JordanBase = pair.first;

        SimpleMatrix nullSpace = tempMatrix.svd().nullSpace();
        currentNumberOfVectors += nullSpace.numCols();

        if (currentNumberOfVectors >= targetNumberOfVectors) {
            int numberOfWrittenValuesAll = 0;
            // проходим по всем векторам на одном уровне
            for (int i = 0; i < nullSpace.numCols(); i++) {
                int numberOfWrittenValues = 0;
                // проходим по всей высоте на уровне
                for (int j = height; j > 0; j--) {
                    // если можно ещё вписать
                    if (numberOfWrittenValuesAll < targetNumberOfVectors) {
                        // возводим вектор на нужную высоту
                        SimpleMatrix tempVector = nullSpace.extractVector(false, i);
                        if (j > 1) {
                            SimpleMatrix powerMatrix = startMatrix;
                            for (int k = 1; k < j; k++) {
                                powerMatrix = powerMatrix.mult(startMatrix);
                            }
                            tempVector = startMatrix.mult(tempVector);
                            int minPosNotZeroId = 0;
                            while (tempVector.get(minPosNotZeroId, 0) < 0.000001 && minPosNotZeroId < tempVector.numRows() - 2) {
                                minPosNotZeroId++;
                            }
                            for (int p = tempVector.numRows() - 1; p >= 0; p--) {
                                tempVector.set(p, 0, tempVector.get(p, 0) / tempVector.get(0, 0));
                            }
                        }


                        // пишем вектор
                        JordanBase = JordanBase.concatColumns(tempVector);
                        numberOfWrittenValues++;
                        numberOfWrittenValuesAll++;
                    } else {
                        break;
                    }
                }

                if (numberOfWrittenValues > 0) {
                    setJordanSell(JordanMatrix, JordanBase.numCols() - numberOfWrittenValues, numberOfWrittenValues, eigenValue);
                }
            }
        } else {
            tempMatrix = tempMatrix.mult(startMatrix);
            return getLocalBasis(tempMatrix, new Pair<>(JordanBase, JordanMatrix), startMatrix, height + 1, currentNumberOfVectors, targetNumberOfVectors, eigenValue);
        }

        return new Pair<>(JordanBase, JordanMatrix);
    }

    private SimpleMatrix setJordanSell(@NotNull SimpleMatrix JordanMatrix, int startPoint, int cellSize, double eigenValue) {
        JordanMatrix.set(startPoint, startPoint, eigenValue);
        for (int i = 1; i < cellSize; i++) {
            JordanMatrix.set(startPoint + i, startPoint + i, eigenValue);
            JordanMatrix.set(startPoint + i - 1, startPoint + i, 1d);
        }

        return JordanMatrix;
    }

    private StringBuffer printMatrix(SimpleMatrix mat) {
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < mat.numRows(); row++) {
            for (int col = 0; col < mat.numCols(); col++) {
                sb.append(String.format("%1$12.3f", smartRound(mat.get(row, col))));
            }
            sb.append("\n");
        }

        return sb;
    }
}