package net.manish.shopping.utils;

import android.util.Log;

public class Mylogger {
    private boolean lockLog = false;
    private static final Mylogger ourInstance = new Mylogger();

    public static Mylogger getInstance() {
        return ourInstance;
    }

    public Mylogger() {
    }


    public void printLog(String tagName, String log) {
        if (lockLog)
            Log.d(tagName, log);
    }

    public void setLockLog(boolean lockLog) {
        this.lockLog = lockLog;
    }
}
