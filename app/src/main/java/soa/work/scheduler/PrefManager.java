package soa.work.scheduler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static soa.work.scheduler.Constants.USER_ACCOUNT;

class PrefManager {

    private static final String PREF_NAME = "work_scheduler";
    private static final String LAST_OPENED_ACTIVITY = "last_opened_activity";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    PrefManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    int getLastOpenedActivity() {
        return pref.getInt(LAST_OPENED_ACTIVITY, /*default value*/USER_ACCOUNT);
    }

    void setLastOpenedActivity(int lastOpenedActivity) {
        editor.putInt(LAST_OPENED_ACTIVITY, lastOpenedActivity);
        editor.apply();
    }
}
