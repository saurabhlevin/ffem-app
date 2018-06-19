package io.ffem.experiment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.akvo.caddisfly.R;

public class DiagnosticSendDialogFragment extends DialogFragment {

    private OnDetailsSavedListener mListener;

    public DiagnosticSendDialogFragment() {
        // Required empty public constructor
    }

    public static DiagnosticSendDialogFragment newInstance() {
        DiagnosticSendDialogFragment fragment = new DiagnosticSendDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity activity = getActivity();
        LayoutInflater i = activity.getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = i.inflate(R.layout.fragment_diagnostic_send_dialog, null);

        TextView textError = view.findViewById(R.id.textError);

        Spinner spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.cuvettes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        EditText comment = view.findViewById(R.id.comment);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Details")
                .setPositiveButton(R.string.sendData, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {

                textError.setError(null);
                if (spinner.getSelectedItemPosition() > 0) {
                    mListener.onDetailsSaved(spinner.getSelectedItemPosition(), comment.getText().toString());
                    dismiss();
                } else {
                    textError.requestFocus();
                    textError.setError("Select cuvette type");

                }
            });
        });
        dialog.show();

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDetailsSavedListener) {
            mListener = (OnDetailsSavedListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnDetailsSavedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnDetailsSavedListener {
        void onDetailsSaved(int i, String s);
    }

}
