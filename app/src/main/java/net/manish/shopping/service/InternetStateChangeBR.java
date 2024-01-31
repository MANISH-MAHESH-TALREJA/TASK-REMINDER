package net.manish.shopping.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.manish.shopping.utils.Mylogger;

public class InternetStateChangeBR extends BroadcastReceiver {

    private static final String TAG = "Inn";
    private Context context;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        Mylogger.getInstance().printLog(TAG, "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                syncLocalDbToFirebaseDb();
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Mylogger.getInstance().printLog(TAG, "There's no network connectivity");
            }
        }
    }

    private void syncLocalDbToFirebaseDb() {
        if (!isMyServiceRunning()) {
            context.startService(new Intent(context, UploadChangesToFirebaseService.class));
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (UploadChangesToFirebaseService.class.getName().equals(service.service.getClassName())) {
                Mylogger.getInstance().printLog("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Mylogger.getInstance().printLog("isMyServiceRunning?", false + "");
        return false;
    }
}