package com.debugmode.activitytimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

/**
 * Single activity app to show a webview in a kiosk mode, but with the ability to exit if needed.
 */
public class ActivityTimerActivity
        extends AppCompatActivity
        implements MainAppsListAdapter.Listener, SharedPreferences.OnSharedPreferenceChangeListener {

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

        // Initialize the webview.
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.setLongClickable(true);
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

        // Hide webview container when the close button is clicked.
        findViewById(R.id.webview_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseTimerView();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.PREFS_SELECTED_APPS)) {
            setAppsListContent();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.choose_apps:
                onChooseAppsClicked();
                return true;
            case R.id.set_password:
                onSetPassword();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // we are a single page launcher app, so don't quit or go back when pressed.
    }

    private String getPassword() {
        return getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getString(Constants.PREFS_PASSWORD, "");
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
        new VerifyPasswordDialog(new VerifyPasswordDialog.Listener() {
                @Override
                public void onVerify() {
                    ChooseAppsDialog dlg = new ChooseAppsDialog(2);
                    dlg.show(getSupportFragmentManager(), "");
                }
            })
            .show(getSupportFragmentManager(), "");
    }

    private void onCloseTimerView() {
        new VerifyPasswordDialog(new VerifyPasswordDialog.Listener() {
                @Override
                public void onVerify() {
                    findViewById(R.id.webview_container).setVisibility(View.INVISIBLE);
                    WebView webView = (WebView) findViewById(R.id.webview);
                    webView.loadUrl("about:blank");
                    webView.clearCache(true);
                }
            })
            .show(getSupportFragmentManager(), "");

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
}
