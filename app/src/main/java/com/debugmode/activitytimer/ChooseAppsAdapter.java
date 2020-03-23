package com.debugmode.activitytimer;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link RecyclerView.Adapter} to display a list of apps and allow selecting them.
 * TODO: Replace the implementation with code for your data type.
 */
public class ChooseAppsAdapter extends RecyclerView.Adapter<ChooseAppsAdapter.ViewHolder> {

    private PackageManager mPackageManager;
    private final List<PackageInfo> mApps = new ArrayList<>();
    private final Set<String> mSelected;

    public ChooseAppsAdapter(PackageManager pm, Set<String> selectedApps) {
        mPackageManager = pm;
        for (PackageInfo item : pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)) {
            if (mPackageManager.getLaunchIntentForPackage(item.packageName) != null)
                mApps.add(item);
        }
        mSelected = selectedApps;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_choose_apps_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setItem(position);
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mCheckedView;
        public final TextView mTextView;
        public final ImageView mImageView;

        public void setItem(final int position) {
            PackageInfo item = mApps.get(position);
            Intent intent = mPackageManager.getLaunchIntentForPackage(item.packageName);
            final String itemUri = intent.toUri(0);
            mTextView.setText(mPackageManager.getApplicationLabel(item.applicationInfo));
            mImageView.setImageDrawable(mPackageManager.getDrawable(item.packageName,
                    item.applicationInfo.icon, item.applicationInfo));
            mCheckedView.setTextColor(mView.getResources().getColor(mSelected.contains(itemUri) ?
                    R.color.app_selected : R.color.app_unselected));
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelected.contains(itemUri)) {
                        mSelected.remove(itemUri);
                    } else {
                        mSelected.add(itemUri);
                    }
                    notifyItemChanged(position);
                }
            });
        }

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCheckedView = (TextView) view.findViewById(R.id.item_checked);
            mTextView = (TextView) view.findViewById(R.id.item_text);
            mImageView = (ImageView) view.findViewById(R.id.item_image);
        }
    }
}
