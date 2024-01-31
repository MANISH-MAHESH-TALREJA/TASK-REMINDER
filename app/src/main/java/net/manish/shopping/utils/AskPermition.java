package net.manish.shopping.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AskPermition {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static final AskPermition ourInstance = new AskPermition();

    public static AskPermition getInstance(Context context) {
        mContext = context;
        return ourInstance;
    }

    private AskPermition() {
    }

    public boolean canReadContacts() {
        boolean b;

        int hasPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS);

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            b = true;
        } else {
            b = false;
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_CONTACTS)) {
                showAlert();
            }
        }
        return b;
    }

    public boolean isPermitted() {
        boolean b;
        int hasPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int hasPermission2 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasPermission3 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);


        if (hasPermission == PackageManager.PERMISSION_GRANTED && hasPermission2 == PackageManager.PERMISSION_GRANTED && hasPermission3 == PackageManager.PERMISSION_GRANTED) {
            b = true;
        } else {
            b = false;
            if (!(ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_EXTERNAL_STORAGE))) {
                showAlert();
            }
        }
        return b;
    }


    private void showAlert() {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Permission Required");
        alert.setMessage("Please click on Permissions and click \"on\" all permission to work Todo app work as intended");

        alert.setCancelable(false);
        alert.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
            intent.setData(uri);
            mContext.startActivity(intent);
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

}
