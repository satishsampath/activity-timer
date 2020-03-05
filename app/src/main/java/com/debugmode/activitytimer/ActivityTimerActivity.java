package com.debugmode.activitytimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Single activity app to show a webview in a kiosk mode, but with the ability to exit if needed.
 */
public class ActivityTimerActivity extends AppCompatActivity {
    private static String PREFS_FILE = "prefs";
    private static String PREFS_PASSWORD = "password";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_activity_timer);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = ActivityTimerDeviceAdminReceiver.getComponentName(this);
        if (!mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            // We need to be in device owner mode for lock-task to really work in kiosk mode.
            Toast.makeText(this, R.string.error_not_device_owner,
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Set up kiosk mode.
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[] { getPackageName() });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startLockTask();

        // Load the content in a webview.
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.setLongClickable(true);
        webview.setHapticFeedbackEnabled(false);
        webview.setOnLongClickListener(new WebView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webview.loadUrl(getResources().getString(R.string.activity_timer_webview_url));
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
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
            case R.id.exit:
                onExitClicked();
                return true;
            case R.id.set_password:
                onSetPassword();
                return true;
            case R.id.clear_device_owner:
                onClearDeviceOwner();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getPassword() {
        return getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getString(PREFS_PASSWORD, "");
    }

    private void cleanupAndExit() {
        stopLockTask();
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[] { });
        finish();
    }

    private void onClearDeviceOwner() {
        new VerifyPasswordDialog()
                .setPassword(getPassword())
                .setListener(new VerifyPasswordDialog.Listener() {
                    @Override
                    public void onVerify() {
                        mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
                        cleanupAndExit();
                    }
                })
                .show(getSupportFragmentManager(), "");
    }

    private void onSetPassword() {
        new SetPasswordDialog()
                .setOldPassword(getPassword())
                .setListener(new SetPasswordDialog.Listener() {
                    @Override
                    public void onNewPassword(String newPassword) {
                        getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
                                .edit()
                                .putString(PREFS_PASSWORD, newPassword)
                                .commit();
                    }
                })
                .show(getSupportFragmentManager(), "");
    }

    private void onExitClicked() {
        new VerifyPasswordDialog()
                .setPassword(getPassword())
                .setListener(new VerifyPasswordDialog.Listener() {
                    @Override
                    public void onVerify() {
                        cleanupAndExit();
                    }
                })
                .show(getSupportFragmentManager(), "");
    }
}
