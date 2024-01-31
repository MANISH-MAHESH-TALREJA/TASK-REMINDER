package net.manish.shopping;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Mylogger;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static boolean isAppVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(realmConfiguration);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        RealmController.with();

        FirebaseApp.initializeApp(this);
        FirebaseRealtimeController.with(this);
        //register activity life cycle callback listeners
        registerActivityLifecycleCallbacks(this);

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        isAppVisible = true;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        isAppVisible = false;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public static boolean isIsAppVisible() {
        return isAppVisible;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // set alarm here
        Mylogger.getInstance().printLog("MyAppp", "onTrimMemory()");
    }
}
