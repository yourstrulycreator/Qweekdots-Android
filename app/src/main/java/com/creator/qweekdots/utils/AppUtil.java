package com.creator.qweekdots.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AppUtil {

    public static ProgressDialog getProgressDialog(Context c, String message) {
        //ProgressDialog progressDialog = ProgressDialog.show(c, "", "Loading..");
        //progressDialog.dismiss();

        final ProgressDialog progressDialog = new ProgressDialog(c);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static boolean aquireUserPermission(Context context, final String permission, int REQUEST_CODE) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity)context,
                    permission)) {

            } else {
                ActivityCompat.requestPermissions((AppCompatActivity)context,
                        new String[]{permission},
                        REQUEST_CODE);
            }

            return false;
        }

        return true;

    }
}
