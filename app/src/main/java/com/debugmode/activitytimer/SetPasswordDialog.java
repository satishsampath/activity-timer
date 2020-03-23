package com.debugmode.activitytimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Set;

/**
 * Dialog fragment to set a new password for the kiosk admin functions. Asks for current password
 * and verifies it before calling the listener to save the new password.
 */
public class SetPasswordDialog extends DialogFragment {
    public interface Listener {
        public void onNewPassword(String newPassword);
    }

    private Listener mListener;
    private String mOldPassword;

    public SetPasswordDialog(String oldPassword, Listener listener) {
        mListener = listener;
        mOldPassword = oldPassword;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setTitle(R.string.dialog_set_password_title);
        builder.setView(inflater.inflate(R.layout.dialog_set_password, null))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String oldPassword =
                                ((EditText) SetPasswordDialog.this.getDialog().findViewById (R.id.old_password))
                                        .getText().toString();
                        String newPassword =
                                ((EditText) SetPasswordDialog.this.getDialog().findViewById (R.id.new_password))
                                        .getText().toString();
                        if (mOldPassword.length() > 0) {
                            if (!oldPassword.equals(mOldPassword)) {
                                Toast.makeText(SetPasswordDialog.this.getContext(),
                                        R.string.old_password_mismatch, Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }
                            if (newPassword.length() == 0) {
                                Toast.makeText(SetPasswordDialog.this.getContext(),
                                        R.string.new_password_invalid, Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }
                        }
                        mListener.onNewPassword(newPassword);
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
