package com.debugmode.activitytimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class ChooseAppsDialog
        extends DialogFragment {

    private int mColumnCount = 2;
    private Set<String> mSelectedApps;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChooseAppsDialog(int columns) {
        mColumnCount = columns;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mSelectedApps = new ArraySet<>(
                getActivity().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                    .getStringSet(Constants.PREFS_SELECTED_APPS, null));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setTitle(R.string.dialog_choose_apps_title);
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.dialog_choose_apps, null);
        if (mColumnCount <= 1) {
            view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        } else {
            view.setLayoutManager(new GridLayoutManager(view.getContext(), mColumnCount));
        }
        view.setAdapter(new ChooseAppsAdapter(view.getContext().getPackageManager(), mSelectedApps));
        builder.setView(view)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                                .edit()
                                .putStringSet(Constants.PREFS_SELECTED_APPS, mSelectedApps)
                                .commit();
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
