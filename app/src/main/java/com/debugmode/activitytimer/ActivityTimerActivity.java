package com.debugmode.activitytimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupMenu;

/**
 * Single activity app to show a webview in a kiosk mode, but with the ability to exit if needed.
 */
public class ActivityTimerActivity
        extends AppCompatActivity
        implements MainAppsListAdapter.Listener,
            SharedPreferences.OnSharedPreferenceChangeListener,
            PopupMenu.OnMenuItemClickListener,
            NumpadView.Listener {

    private void setAppsListLayoutManager() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.apps_list);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        float columnWidthDp = getResources().getDimension(R.dimen.main_apps_list_item_image_size) * 1.5f;
        int columns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        rv.setLayoutManager(new GridLayoutManager(this, columns));
    }

    private void setAppsListContent() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.apps_list);
        rv.setAdapter(new MainAppsListAdapter(this, this));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setAppsListLayoutManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setAppsListLayoutManager();
        setAppsListContent();
        getSharedPreferences(Constants.PREFS, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);

        ((NumpadView) findViewById(R.id.admin_numpad)).setListener(this);
        findViewById(R.id.button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(v.getContext(), v);
                menu.inflate(R.menu.main_menu);
                menu.setOnMenuItemClickListener(ActivityTimerActivity.this);
                menu.show();
            }
        });
        // Initialize the webview.
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.setLongClickable(false);
        webview.setHapticFeedbackEnabled(false);
        webview.setOnLongClickListener(new WebView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.PREFS_SELECTED_APPS)) {
            setAppsListContent();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_apps:
                onChooseAppsClicked();
                break;
            case R.id.set_password:
                onSetPassword();
                break;
            case R.id.close_timer:
                onCloseTimerView();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        findViewById(R.id.button_menu).setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public void onBackPressed() {
        // we are a single page launcher app, so don't quit or go back when pressed.
    }

    private String getPassword() {
        return getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getString(
                Constants.PREFS_PASSWORD, "000");
    }

    private void onSetPassword() {
        new SetPasswordDialog(getPassword(), new SetPasswordDialog.Listener() {
                @Override
                public void onNewPassword(String newPassword) {
                    getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
                            .edit()
                            .putString(Constants.PREFS_PASSWORD, newPassword)
                            .commit();
                }
            })
            .show(getSupportFragmentManager(), "");
    }

    private void onChooseAppsClicked() {
        ChooseAppsDialog dlg = new ChooseAppsDialog(2);
        dlg.show(getSupportFragmentManager(), "");
    }

    private void onCloseTimerView() {
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl("about:blank");
        webView.clearCache(true);
        findViewById(R.id.webview_container).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAppLaunched(Intent intent) {
        if (intent.getAction().equals(Constants.ACTION_LAUNCH_TIMER)) {
            WebView webview = (WebView) findViewById(R.id.webview);
            webview.loadUrl(getResources().getString(R.string.activity_timer_webview_url));
            findViewById(R.id.webview_container).setVisibility(View.VISIBLE);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    @Override
    public void onNumpadString(String str) {
        if (str.equals(getPassword()))
            findViewById(R.id.button_menu).setVisibility(View.VISIBLE);
    }

}
