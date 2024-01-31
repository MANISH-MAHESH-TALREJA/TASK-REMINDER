package net.manish.shopping.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.manish.shopping.activities.ReminderActivity;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class GetTodoAndShowPopupBasedOnIdBR extends BroadcastReceiver {

    private Context context;
    public List<TodoModel> todoList = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        String repeatTime = intent.getStringExtra(Constants.KEY_REPEAT);

        todoList.clear();
        todoList.addAll(RealmController.getInstance().getAllTodosByStatusAndTime(repeatTime));

        if (todoList.size() > 0) {
            makeRingAndPopupScreen(repeatTime);
        }
    }

    private void makeRingAndPopupScreen(String repeatTime) {

        if (!ReminderActivity.isReminderActivityOpened) {
            ReminderActivity.list.clear();
            ReminderActivity.list.addAll(todoList);

            Intent i = new Intent(context, ReminderActivity.class);
            i.putExtra(Constants.KEY_REPEAT, repeatTime);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        }
    }

}