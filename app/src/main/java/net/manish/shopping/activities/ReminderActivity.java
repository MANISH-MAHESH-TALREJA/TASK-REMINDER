package net.manish.shopping.activities;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.manish.shopping.R;
import net.manish.shopping.adapter.ReminderListAdapter;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import at.markushi.ui.CircleButton;

public class ReminderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    public static List<TodoModel> list = new ArrayList<>();

    private boolean isAnyButtonClicked = false;

    private DateAndTimePicker dateAndTimePicker;
    private SessionManager sessionManager;

    private Ringtone r;
    private Vibrator vibrator;

    private String repeatTime;

    public static boolean isReminderActivityOpened = false;

    @Override
    protected void onResume() {
        super.onResume();
        isReminderActivityOpened = true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_reminder);

        repeatTime = getIntent().getStringExtra(Constants.KEY_REPEAT);

        dateAndTimePicker = new DateAndTimePicker(ReminderActivity.this);
        sessionManager = new SessionManager(ReminderActivity.this);

        getListFromLocal();

        findViews();
        setupRecyclerView();
        makeRinging();
    }

    private void getListFromLocal() {
        list.clear();
        list.addAll(RealmController.getInstance().getAllTodosByStatusAndTime(repeatTime));
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ReminderListAdapter adapter = new ReminderListAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    private void findViews() {

        recyclerView = findViewById(R.id.recycler_view);

        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvTime = findViewById(R.id.tv_time);

        CircleButton btnDone = findViewById(R.id.btn_done);
        CircleButton btnSnooz = findViewById(R.id.btn_snooze);

        btnDone.setOnClickListener(v -> {

            isAnyButtonClicked = true;
            r.stop();
            vibrator.cancel();
            finishReminders();
        });

        btnSnooz.setOnClickListener(v -> {
            r.stop();
            vibrator.cancel();
            isAnyButtonClicked = true;
            applySnoozeTimeForReminder();
        });

        //init datetime
        if (list.size() > 0) {
            tvDate.setText(list.get(0).getDate());
            tvTime.setText(list.get(0).getTime());
        }

    }

    private void makeRinging() {

        Uri ringtone = Uri.parse(sessionManager.getRingtoneUri());
        r = RingtoneManager.getRingtone(getApplicationContext(), ringtone);
        r.play();

        //make vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            //deprecated in API 26
            vibrator.vibrate(pattern, 0);
        }

    }

    private void finishReminders() {

        //update remind datetime in database
        for (int i = 0; i < list.size(); i++) {
            CommonUtils.handleTodoBasedOnRepeat(ReminderActivity.this, list.get(i));
        }

        finishActivity();
    }

    private void applySnoozeTimeForReminder() {

        Date currentDate = null;
        if (list.size() > 0) {
            currentDate = dateAndTimePicker.convertStringToDateTime(dateAndTimePicker.getCurrentDateTime());
        }

        Calendar calendar = Calendar.getInstance();
        if (currentDate != null) {
            calendar.setTime(currentDate);
        }
        calendar.add(Calendar.MINUTE, Integer.parseInt(sessionManager.getSnoozeTime()));
        Date newDate = calendar.getTime();

        //update remind datetime in database
        list.clear();
        list.addAll(RealmController.getInstance().getAllTodosByStatusAndTime(repeatTime));
        for (int i = 0; i < list.size(); i++) {
            RealmController.getInstance().updateNewRemindDate(list.get(i).getId(), dateAndTimePicker.getFormatedDate(newDate));
            CommonUtils.updateReminderOnRealtimeDb(ReminderActivity.this, list.get(i),
                    dateAndTimePicker.getFormatedDate(newDate),
                    list.get(i).getStatus());
        }

        finishActivity();
    }

    private void finishActivity() {

        finishAndRemoveTask();
        this.overridePendingTransition(0, 0);

    }

    @Override
    protected void onPause() {
        super.onPause();
        isReminderActivityOpened = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isReminderActivityOpened = false;

        r.stop();
        vibrator.cancel();
        if (!isAnyButtonClicked) {
            applySnoozeTimeForReminder();
        }
    }

}
