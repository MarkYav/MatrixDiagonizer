package com.example.matrixdiagonizer;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.EditText;

public class EditTextMatrix implements Parcelable {
    EditText[][] editTexts;

    public EditTextMatrix() {
    }

    protected EditTextMatrix(Parcel in) {
    }

    public static final Creator<EditTextMatrix> CREATOR = new Creator<EditTextMatrix>() {
        @Override
        public EditTextMatrix createFromParcel(Parcel in) {
            return new EditTextMatrix(in);
        }

        @Override
        public EditTextMatrix[] newArray(int size) {
            return new EditTextMatrix[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
