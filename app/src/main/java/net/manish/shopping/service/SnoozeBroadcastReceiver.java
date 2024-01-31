package net.manish.shopping.service;

import static net.manish.shopping.utils.Constants.KEY_ID;
import static net.manish.shopping.utils.Constants.KEY_REPEAT;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import net.manish.shopping.model.TodoModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;

import java.util.Objects;

public class SnoozeBroadcastReceiver extends IntentService {

    public SnoozeBroadcastReceiver(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        int id = Objects.requireNonNull(intent).getIntExtra(Constants.KEY_ID, 0);

        TodoModel todoModel = RealmController.getInstance().getTodoById(id);

        Bundle b = new Bundle();
        b.putString(KEY_ID, String.valueOf(todoModel.getId()));
        b.putInt(Constants.KEY_UNIQUE_ID, 0);
        b.putString(KEY_REPEAT, String.valueOf(todoModel.getRemindTime()));

    }
}