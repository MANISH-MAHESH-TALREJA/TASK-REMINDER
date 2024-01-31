package net.manish.shopping.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.manish.shopping.activities.ReminderActivity;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.Mylogger;

import java.util.ArrayList;
import java.util.List;

public class TimeMatchingBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "TimeMatchingg";
    private Context context;
    public List<TodoModel> todoList = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {

        DateAndTimePicker dateAndTimePicker = new DateAndTimePicker(context);
        Mylogger.getInstance().printLog(TAG, "onReceive() + current date time : " + dateAndTimePicker.getCurrentDateTime());

        this.context = context;

        todoList.clear();
        todoList.addAll(RealmController.getInstance().getAllTodosByStatusAndTime(dateAndTimePicker.getCurrentDateTime()));

        if (todoList.size() > 0) {
            makeRingAndPopupScreen();
        }
    }

    private void makeRingAndPopupScreen() {

        if (!ReminderActivity.isReminderActivityOpened) {
            ReminderActivity.list.clear();
            ReminderActivity.list.addAll(todoList);

            Intent i = new Intent(context, ReminderActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}