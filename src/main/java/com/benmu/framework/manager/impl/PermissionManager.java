package com.benmu.framework.manager.impl;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.benmu.framework.manager.Manager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carry on 2017/8/21.
 */

public class PermissionManager extends Manager {
    public static final int RUNTIME_PERMISSION = 23;

    public interface PermissionListener {

        void onPermissionsGranted(List<String> perms);

        void onPermissionsDenied(List<String> perms);

        void onPermissionRequestRejected();

    }

    public boolean hasPermissions(Context context, String... perms) {
        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) == PackageManager
                    .PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    public void requestPermissions(Object object, PermissionListener listener, String rationale,
                                   final String... perms) {
        requestPermissions(object, listener, rationale, android.R.string.ok, android.R.string
                .cancel, perms);
    }

    public void requestPermissions(final Object object, final PermissionListener listener, String
            rationale,
                                   @StringRes int positiveButton,
                                   @StringRes int negativeButton, final String... perms) {

        checkCallingObjectSuitability(object);

        boolean shouldShowRationale = false;
        for (String perm : perms) {
            shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale
                    (object, perm);
        }

        if (shouldShowRationale && !TextUtils.isEmpty(rationale)) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity(object))
                    .setMessage(rationale)
                    .setCancelable(false)
                    .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executePermissionsRequest(object, perms, RUNTIME_PERMISSION);
                        }
                    })
                    .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing, user does not want to request
                            listener.onPermissionRequestRejected();
                        }
                    }).create();
            dialog.show();
        } else {
            executePermissionsRequest(object, perms, RUNTIME_PERMISSION);
        }
    }

    public void onRequestPermissionsResult(Object object, PermissionListener callbacks, int
            requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == RUNTIME_PERMISSION) {
            checkCallingObjectSuitability(object);

            // Make a collection of granted and denied permissions from the request.
            ArrayList<String> granted = new ArrayList<>();
            ArrayList<String> denied = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                String perm = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(perm);
                } else {
                    denied.add(perm);
                }
            }

            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                // Notify callbacks
                callbacks.onPermissionsGranted(granted);
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                callbacks.onPermissionsDenied(denied);
            }
        }
    }

    private boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    private void executePermissionsRequest(Object object, String[] perms, int requestCode) {
        checkCallingObjectSuitability(object);

        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    private Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    private void checkCallingObjectSuitability(Object object) {
        // Make sure Object is an Activity or Fragment
        if (!((object instanceof Fragment) || (object instanceof Activity))) {
            throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
        }
    }
}
