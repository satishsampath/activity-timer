package com.debugmode.activitytimer;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;

public class ActivityTimerDeviceAdminReceiver extends DeviceAdminReceiver {
    public static ComponentName getComponentName(@NonNull Context context) {
        return new ComponentName(context.getApplicationContext(), ActivityTimerDeviceAdminReceiver.class);
    }
}
