package net.manish.shopping.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.manish.shopping.activities.MainActivity;

import java.util.Objects;

public class ServiceRestarterBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

}