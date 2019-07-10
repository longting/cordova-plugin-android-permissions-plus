package com.android.plugins;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JasonYang on 2016/3/11.
 */
public class Permissions extends CordovaPlugin {

    private static final String ACTION_CHECK_PERMISSION = "checkPermissions";
    private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
    private static final String ACTION_REQUEST_PERMISSIONS = "requestPermissions";

    private static final String ACTION_CHECK_NOTIFICATION= "checkNotification";
    private static final String ACTION_REQUEST_NOTIFICATION = "requestNotification";



    private static final int REQUEST_CODE_ENABLE_PERMISSION = 55433;

    private static final String KEY_ERROR = "error";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_RESULT_PERMISSION = "hasPermission";

    private CallbackContext permissionsCallback;
    private static String TAG = "PermissionPlugin";

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        if (ACTION_CHECK_PERMISSION.equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    checkPermissionAction(callbackContext, args);
                }
            });
            return true;
        } else if (ACTION_REQUEST_PERMISSIONS.equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        requestPermissionAction(callbackContext, args);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject returnObj = new JSONObject();
                        addProperty(returnObj, KEY_ERROR, ACTION_REQUEST_PERMISSION);
                        addProperty(returnObj, KEY_MESSAGE, "Request permission has been denied.");
                        callbackContext.error(returnObj);
                        permissionsCallback = null;
                    }
                }
            });
            return true;
        } else if (ACTION_CHECK_NOTIFICATION.equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    checkNotificationAction(callbackContext);
                }
            });
            return true;
        } else if (ACTION_REQUEST_NOTIFICATION.equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        requestNotificationAction(callbackContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject returnObj = new JSONObject();
                        addProperty(returnObj, KEY_ERROR, ACTION_REQUEST_NOTIFICATION);
                        addProperty(returnObj, KEY_MESSAGE, "Request Notification has been denied.");
                        callbackContext.error(returnObj);
                        permissionsCallback = null;
                    }
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
            throws JSONException {
        if (permissionsCallback == null) {
            return;
        }

        JSONObject returnObj = new JSONObject();
        if (permissions != null && permissions.length > 0) {
            // Call checkPermission again to verify
            boolean hasAllPermissions = hasAllPermissions(permissions);
            addProperty(returnObj, KEY_RESULT_PERMISSION, hasAllPermissions);
            permissionsCallback.success(returnObj);
        } else {
            addProperty(returnObj, KEY_ERROR, ACTION_REQUEST_PERMISSION);
            addProperty(returnObj, KEY_MESSAGE, "Unknown error.");
            permissionsCallback.error(returnObj);
        }
        permissionsCallback = null;
    }

    /**
     * 检查通知栏权限
     * @param callbackContext
     */
    private void checkNotificationAction(CallbackContext callbackContext) {
        boolean has = NotificationManagerCompat.from(cordova.getContext()).areNotificationsEnabled();
        JSONObject returnObj = new JSONObject();
        addProperty(returnObj, KEY_RESULT_PERMISSION,has);
        callbackContext.success(returnObj);
    }

    private void checkPermissionAction(CallbackContext callbackContext, JSONArray permission) {
        if (permission == null || permission.length() == 0) {// 没带参数
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, KEY_ERROR, ACTION_CHECK_PERMISSION);
            addProperty(returnObj, KEY_MESSAGE, "One time one permission only.");
            callbackContext.error(returnObj);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {// SDK < 23
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, KEY_RESULT_PERMISSION, true);
            callbackContext.success(returnObj);
        } else {
            try {
                boolean k = true;
                for (int i = 0; i < permission.length(); i++) {
                    boolean rs = cordova.hasPermission(permission.getString(i));
                    if (!rs) {
                        Log.i(TAG, "NO PRE:" + permission.getString(i));
                        k = rs;
                        break;
                    }
                }
                JSONObject returnObj = new JSONObject();
                addProperty(returnObj, KEY_RESULT_PERMISSION, k);
                callbackContext.success(returnObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestNotificationAction(CallbackContext callbackContext) throws Exception {
        Log.i(TAG, "Build.VERSION:" + Build.VERSION_CODES.M);
        Intent intent = new Intent();
        JSONObject returnObj = new JSONObject();
        String pkgName = cordova.getActivity().getPackageName();
        ApplicationInfo info = cordova.getActivity().getApplicationInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE",pkgName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", pkgName);
            intent.putExtra("app_uid", info.uid);

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" +pkgName));
        } else if (Build.VERSION.SDK_INT >= 15) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package",pkgName, null));
        }

        try {
            cordova.startActivityForResult(this,intent,REQUEST_CODE_ENABLE_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
            intent = new Intent();
            // 出现异常则跳转到应用设置界面
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", pkgName, null);
            intent.setData(uri);
            cordova.startActivityForResult(this,intent,REQUEST_CODE_ENABLE_PERMISSION);
        }
        addProperty(returnObj, KEY_RESULT_PERMISSION, true);
        callbackContext.success(returnObj);
    }

    private void requestPermissionAction(CallbackContext callbackContext, JSONArray permissions) throws Exception {
        if (permissions == null || permissions.length() == 0) {
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, KEY_ERROR, ACTION_REQUEST_PERMISSION);
            addProperty(returnObj, KEY_MESSAGE, "At least one permission.");
            callbackContext.error(returnObj);
        } else if (hasAllPermissions(permissions)) {
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, KEY_RESULT_PERMISSION, true);
            callbackContext.success(returnObj);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, KEY_RESULT_PERMISSION, true);
            callbackContext.success(returnObj);
        } else {
            Log.i(TAG, ">" + Build.VERSION_CODES.M);
            permissionsCallback = callbackContext;
            String[] permissionArray = getPermissions(permissions);
            Log.i(TAG, "ps:" + toString(permissionArray));
            cordova.requestPermissions(this, REQUEST_CODE_ENABLE_PERMISSION, permissionArray);
        }
    }

    private String[] getPermissions(JSONArray permissions) {
        String[] stringArray = new String[permissions.length()];
        for (int i = 0; i < permissions.length(); i++) {
            try {
                stringArray[i] = permissions.getString(i);
            } catch (JSONException ignored) {
                // Believe exception only occurs when adding duplicate keys, so just ignore it
            }
        }
        return stringArray;
    }

    private String toString(String[] ps) {
        String k = "";
        for (String p : ps) {
            k += p + ",";
        }
        return k;
    }

    private boolean hasAllPermissions(JSONArray permissions) throws JSONException {
        return hasAllPermissions(getPermissions(permissions));
    }

    private boolean hasAllPermissions(String[] permissions) throws JSONException {

        for (String permission : permissions) {
            if (!cordova.hasPermission(permission)) {
                return false;
            }
        }

        return true;
    }

    private void addProperty(JSONObject obj, String key, Object value) {
        try {
            if (value == null) {
                obj.put(key, JSONObject.NULL);
            } else {
                obj.put(key, value);
            }
        } catch (JSONException ignored) {
            // Believe exception only occurs when adding duplicate keys, so just ignore it
        }
    }
}
