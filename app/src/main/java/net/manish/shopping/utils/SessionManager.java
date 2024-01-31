package net.manish.shopping.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;

import com.google.firebase.auth.FirebaseAuth;
import net.manish.shopping.activities.LoginActivity;
import net.manish.shopping.notification.api.FirebaseUtils;
import net.manish.shopping.realm.RealmController;

public class SessionManager {
    private static SharedPreferences pref;
    private final Editor editor;
    private final Context _context;
    private static final String PREF_NAME = "todo_task";
    private static final String IS_LOGIN = "isLoggedIn";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "name";
    private static final String USER_PHONE = "phone";
    private static final String IS_FIRST_TIME = "isFirstTime";

    private static final String RINGTONE_URI = "ringtoneUri";
    private static final String SNOOZE_TIME = "snoozeTimeInMinut";
    private static final String SNOOZE_TIME_DEFAULT = Constants.SNOOZE_5_MIN;//minutes

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean isFirstTime() {
        return pref.getBoolean(IS_FIRST_TIME, true);
    }

    public void createLogin(String userId, String name, String phoneNumber) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(USER_ID, userId);
        editor.putString(USER_NAME, name);
        editor.putString(USER_PHONE, phoneNumber);

        editor.commit();
    }

    public String getUserId() {
        return pref.getString(USER_ID, "");
    }

    public String getUserName() {
        return pref.getString(USER_NAME, "");
    }

    public String getPhoneNumber() {
        return pref.getString(USER_PHONE, "");
    }

    public void setSnoozeTime(String timeInMinut) {

        editor.putString(SNOOZE_TIME, timeInMinut);
        editor.commit();
    }

    public String getSnoozeTime() {
        return pref.getString(SNOOZE_TIME, SNOOZE_TIME_DEFAULT);//todo : remove comment immedietly always from this line.
    }

    public void setRingtoneUri(String uri) {

        editor.putString(RINGTONE_URI, uri);
        editor.commit();
    }

    public String getRingtoneUri() {
        String defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();
        return pref.getString(RINGTONE_URI, defaultUri);
    }

    public void setDefaultRingtone() {
        String defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();
        editor.putString(RINGTONE_URI, defaultUri);
        editor.commit();
    }

    public String getFirebaseToken() {
        return pref.getString(Constants.KEY_FIREBASE_TOKEN, "");
    }

    public void setFirebaseToken(String b) {
        editor.putString(Constants.KEY_FIREBASE_TOKEN, b);
        editor.commit();
    }

    public void logoutUser() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        FirebaseUtils.newInstance().removeFirebaseToken(getFirebaseToken());
        RealmController.getInstance().clearWholeDatabase();

        editor.putBoolean(IS_LOGIN, false);
        editor.putBoolean(IS_FIRST_TIME, true);
        editor.commit();

        editor.clear();
        editor.commit();

        Intent intent = new Intent(_context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intent);
        ((Activity) _context).finish();

    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

}
