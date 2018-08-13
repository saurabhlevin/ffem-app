package org.akvo.caddisfly.sensor.chamber;

import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;

import java.util.Locale;

public class CalibrationResultDialog extends DialogFragment {

    private Calibration calibration;
    private int decimalPlaces;
    private String unit;

    /**
     * Instance of dialog.
     *
     * @param calibration   the result
     * @param decimalPlaces decimal places
     * @return the dialog
     */
    public static DialogFragment newInstance(Calibration calibration, int decimalPlaces, String unit) {
        CalibrationResultDialog fragment = new CalibrationResultDialog();
        Bundle args = new Bundle();
        fragment.decimalPlaces = decimalPlaces;
        fragment.calibration = calibration;
        fragment.unit = unit;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.dialog_calibration_result, container, false);

        getDialog().setTitle("Calibrated");

        Button buttonColorExtract = view.findViewById(R.id.buttonColorExtract);
        TextView textValue = view.findViewById(R.id.textValue);
        TextView textUnit = view.findViewById(R.id.textUnit);

        buttonColorExtract.setBackgroundColor(calibration.color);

        String format = "%." + decimalPlaces + "f";
        textValue.setText(String.format(Locale.getDefault(), format, calibration.value, ""));
        textUnit.setText(unit);

        Button buttonOk = view.findViewById(R.id.buttonOk);
        buttonOk.setVisibility(View.VISIBLE);
        buttonOk.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    getActivity().stopLockTask();
                } catch (Exception ignored) {
                }
            }
            this.dismiss();
        });

        return view;
    }
}
