package net.manish.shopping.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;

public class BackgroundService extends Service {

    //for connection status change
    private InternetStateChangeBR mNetworkReceiver;

    public BackgroundService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        registerNetworkStateChangeListeners();//For sync todos from local to firebase when internet available

        return START_STICKY;
    }

    private void registerNetworkStateChangeListeners() {
        //Set Connection state change listener
        mNetworkReceiver = new InternetStateChangeBR();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.CONNECTIVITY_ACTION);

        registerReceiver(mNetworkReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {

        try {
            unregisterReceiver(mNetworkReceiver);//for unregister network state change callback
        }catch (Exception e){
            Mylogger.getInstance().printLog("Bg", "onCatch destroy() ");
        }
        super.onDestroy();

        Intent broadcastIntent = new Intent(this, ServiceRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Mylogger.getInstance().printLog("Bg","onTaskRemoved() : " );
    }
}