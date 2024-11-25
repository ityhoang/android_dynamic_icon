package com.example.android_dynamic_icon;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** AndDynamicIconPlugin */
public class MethodCallImplementation implements MethodCallHandler {
    private Context context;
    private Activity activity;
    private List<String> classNames;
    private String packageName;
    private boolean iconChanged = false;
    private List<String> args = new ArrayList<>();
    private static final String TAG = "AndroidDynamicIconPlugin";

    public MethodCallImplementation(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "initialize":
                initialize(call);
                result.success("Initialized successfully");
                break;

            case "changeIcon":
                changeIcon(call);
                result.success("Icon change requested");
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    private void initialize(MethodCall call) {
        Map<String, Object> arguments = call.arguments();
        Log.d(TAG, "Initializing AndroidDynamicIconPlugin with arguments: " + arguments);

        if (arguments.containsKey("classNames") && arguments.containsKey("packageName")) {
            classNames = (List<String>) arguments.get("classNames");
            packageName = (String) arguments.get("packageName");

            Log.d(TAG, "Initialization successful with class names: " + classNames + " and package name: " + packageName);
        } else {
            Log.e(TAG, "Initialization failed! Make sure to pass both 'classNames' and 'packageName'.");
        }
    }

    private void changeIcon(MethodCall call) {
        if (classNames == null || classNames.isEmpty()) {
            Log.e(TAG, "Initialization Failed! List all the activity-alias class names in initialize().");
            return;
        }

        args = call.arguments();
        iconChanged = true;
        updateIcon();
    }

    void updateIcon() {
        if (iconChanged) {
            String className = args.get(0);
            PackageManager pm = activity.getPackageManager();
            String currentPackageName = context.getPackageName(); // Sử dụng context để lấy package name hiện tại

            // Kiểm tra package name có khớp với package được cung cấp hay không
            if (!currentPackageName.equals(packageName)) {
                Log.e(TAG, "Package name mismatch! Skipping icon update.");
                return;
            }

            for (String alias : classNames) {
                ComponentName componentName = new ComponentName(currentPackageName, alias);
                int componentState = className.equals(alias)
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

                try {
                    pm.setComponentEnabledSetting(componentName, componentState, PackageManager.DONT_KILL_APP);
                    Log.d(TAG, "Updated component: " + alias + " to state: " + componentState);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to update icon state for " + alias + ": " + e.getMessage());
                }
            }

            iconChanged = false;
            Log.d(TAG, "Icon switched to " + className);
        }
    }
}