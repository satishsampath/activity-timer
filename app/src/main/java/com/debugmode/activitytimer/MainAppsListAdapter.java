package com.debugmode.activitytimer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainAppsListAdapter extends RecyclerView.Adapter<MainAppsListAdapter.ViewHolder> {

    public interface Listener {
        public void onAppLaunched(Intent intent);
    }

    private Context mContext;
    private Listener mListener;
    private final List<Intent> mApps = new ArrayList<>();

    public MainAppsListAdapter(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
        Set<String> uris = mContext.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
                .getStringSet(Constants.PREFS_SELECTED_APPS, null);
        for (String uri : uris) {
            try {
                Intent intent = Intent.parseUri(uri, 0);
                mApps.add(intent);
            } catch(URISyntaxException e) {
            }
        }
        mApps.add(new Intent(Constants.ACTION_LAUNCH_TIMER));
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_apps_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mTextView;
        private ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTextView = (TextView) view.findViewById(R.id.item_text);
            mImageView = (ImageView) view.findViewById(R.id.item_image);
        }

        public void setItem(final int position) {
            final Intent intent = mApps.get(position);
            if (intent.getAction().equals(Constants.ACTION_LAUNCH_TIMER)) {
                mTextView.setText(R.string.apps_list_timer_title);
                mImageView.setImageDrawable(mView.getResources().getDrawable(R.drawable.timer));
            } else {
                PackageManager pm = mContext.getPackageManager();
                ResolveInfo ri = pm.resolveActivity(mApps.get(position), 0);
                mTextView.setText(pm.getApplicationLabel(ri.activityInfo.applicationInfo));
                mImageView.setImageDrawable(pm.getApplicationIcon(ri.activityInfo.applicationInfo));
            }
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onAppLaunched(intent);
                }
            });
        }
    }
}
