package com.debugmode.activitytimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog fragment to ask for kiosk admin password before performing any admin functions. Validates
 * the entered password before calling the listener to perform said admin function.
 */
public class VerifyPasswordDialog extends DialogFragment {
    public interface Listener {
        public void onVerify();
    }

    private Listener mListener;

    public VerifyPasswordDialog(Listener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setTitle(R.string.dialog_verify_password_title);
        builder.setView(inflater.inflate(R.layout.dialog_verify_password, null))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String password =
                                ((EditText) VerifyPasswordDialog.this.getDialog().findViewById (R.id.password))
                                        .getText().toString();
                        String actualPassword =
                                getContext().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                                        .getString(Constants.PREFS_PASSWORD, "");
                        if (actualPassword.length() > 0) {
                            if (!password.equals(actualPassword)) {
                                Toast.makeText(VerifyPasswordDialog.this.getContext(),
                                        R.string.password_mismatch, Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }
                        }
                        mListener.onVerify();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
