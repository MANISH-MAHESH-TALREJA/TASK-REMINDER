package net.manish.shopping.utils;

import static android.content.Context.ALARM_SERVICE;
import static net.manish.shopping.utils.Constants.KEY_ID;
import static net.manish.shopping.utils.Constants.KEY_REPEAT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import net.manish.shopping.R;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.RingtoneModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.service.ForegroundService;
import net.manish.shopping.service.GetTodoAndShowPopupBasedOnIdBR;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class CommonUtils {

    private static final String TAG = "CommonUtils";

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static void scheduleAlarm(Context context, TodoModel todoModel) {

        DateAndTimePicker dateAndTimePicker = new DateAndTimePicker(context);
        Date remindDate = dateAndTimePicker.convertStringToDateTime(todoModel.getRemindTime());

        int unique_id = (int) ((remindDate.getTime() / 1000L) % Integer.MAX_VALUE);

        long diffInMs = remindDate.getTime() - dateAndTimePicker.convertStringToDateTime(dateAndTimePicker.getCurrentDateTime()).getTime();
        int diffInSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(diffInMs);

        if (diffInSeconds >= 0) {

            Bundle b = new Bundle();
            b.putString(KEY_ID, String.valueOf(todoModel.getId()));
            b.putInt(Constants.KEY_UNIQUE_ID, unique_id);
            b.putString(KEY_REPEAT, String.valueOf(todoModel.getRemindTime()));

            Intent intent2 = new Intent(context, GetTodoAndShowPopupBasedOnIdBR.class);
            intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent2.putExtras(b);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), unique_id, intent2, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, remindDate.getTime(), pendingIntent);
        }
    }

    public static String[] getArrayFromString(String assignTo) {

        try {
            JSONArray jsonArray = new JSONArray(assignTo);
            String[] strArr = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {

                strArr[i] = jsonArray.getString(i);
            }

            return strArr;
        } catch (JSONException e) {
            Mylogger.getInstance().printLog(TAG, "onCatch :- " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new String[0];
    }

    public static String getCurrentDateText(String date, String currentDate) {

        Mylogger.getInstance().printLog(TAG, "date : " + date + "\tcurrentDate : " + currentDate);

        if (date.equals(currentDate)) {
            return Constants.TODAY_TEXT;
        } else {
            return date;
        }
    }

    public static List<RingtoneModel> getDefaultRingtoneList(Context context) {

        List<RingtoneModel> list = new ArrayList<>();

        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = manager.getCursor();
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri ringtoneURI = manager.getRingtoneUri(cursor.getPosition());
            // Do something with the title and the URI of ringtone

            list.add(new RingtoneModel(list.size() + 1, title, ringtoneURI, false));
        }
        return list;
    }

    public static JsonObject createJsonObjectFromValues(String userId, String body, String title, String notiFor) {

        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("fromm", userId);
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("msg", body);
            jsonObject.addProperty("notiFor", notiFor);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static String getNameByPhoneNumber(String phone) {

        List<ContactModel> contactList = RealmController.getInstance().getAllRegisteredContacts();
        for (int i = 0; i < contactList.size(); i++) {
            if (phone.equals(Objects.requireNonNull(contactList.get(i)).getNumber())) {
                return Objects.requireNonNull(contactList.get(i)).getName();
            }
        }
        return "";
    }


    public static void startForegroundReminderService(Context context, TodoModel todoModel) {

        Bundle b = new Bundle();
        b.putParcelable(KEY_ID, todoModel);
        b.putString(KEY_REPEAT, String.valueOf(todoModel.getRemindTime()));

        Intent serviceIntent = new Intent(context, ForegroundService.class);
        serviceIntent.putExtras(b);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void hideKeyboard(Activity context) {
        try {
            View view = context.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //For reminders

    public static void handleTodoBasedOnRepeat(Context context, TodoModel todoModel) {

        DateAndTimePicker dateAndTimePicker = new DateAndTimePicker(context);
        SessionManager sessionManager = new SessionManager(context);

        String originalDateTime = todoModel.getDate() + " " + todoModel.getTime();
        String remindDateTime = dateAndTimePicker.getCurrentDate() + " " + todoModel.getTime();

        switch (todoModel.getRepeat()) {
            case Constants.EVERYDAY:

                RealmController.getInstance().updateNewRemindDate(
                        todoModel.getId(),
                        dateAndTimePicker.incrementDateByDay(Constants.ONE, remindDateTime));

                updateReminderOnRealtimeDb(context, todoModel,
                        dateAndTimePicker.incrementDateByDay(Constants.ONE, remindDateTime),
                        todoModel.getStatus());


                break;
            case Constants.WEEKLY:

                RealmController.getInstance().updateNewRemindDate(
                        todoModel.getId(),
                        dateAndTimePicker.incrementDateByDay(
                                Constants.WEEK_DAYS, originalDateTime));

                updateReminderOnRealtimeDb(context, todoModel,
                        dateAndTimePicker.incrementDateByDay(Constants.WEEK_DAYS, originalDateTime),
                        todoModel.getStatus());
                break;
            case Constants.ONCE:
                RealmController.getInstance().updateTodoStatus(
                        todoModel.getId(),
                        Constants.STATUS_FINISHED,
                        sessionManager.getUserId());

                updateReminderOnRealtimeDb(context, todoModel,
                        todoModel.getRemindTime(),
                        Constants.STATUS_FINISHED);
                break;
            case Constants.MONTHLY:
                RealmController.getInstance().updateNewRemindDate(
                        todoModel.getId(),
                        dateAndTimePicker.incrementDateByDay(
                                Constants.MONTH_DAYS, originalDateTime));

                updateReminderOnRealtimeDb(context, todoModel,
                        dateAndTimePicker.incrementDateByDay(Constants.MONTH_DAYS, originalDateTime),
                        todoModel.getStatus());
                break;
            case Constants.YEARLY:
                RealmController.getInstance().updateNewRemindDate(
                        todoModel.getId(),
                        dateAndTimePicker.incrementDateByDay(Constants.YEAR_DAYS, originalDateTime));

                updateReminderOnRealtimeDb(context, todoModel,
                        dateAndTimePicker.incrementDateByDay(Constants.YEAR_DAYS, originalDateTime),
                        todoModel.getStatus());
                break;

        }
    }


    public static void updateReminderOnRealtimeDb(Context context, TodoModel todoModelSent, String newRemindDate, final String status) {

        SessionManager sessionManager = new SessionManager(context);
        TodoModel todoModel = new TodoModel();

        todoModel.setId(todoModelSent.getId());
        todoModel.setFireId(todoModelSent.getFireId());
        todoModel.setTitle(todoModelSent.getTitle());
        todoModel.setNote(todoModelSent.getNote());
        todoModel.setDate(todoModelSent.getDate());
        todoModel.setTime(todoModelSent.getTime());
        todoModel.setRemindTime(newRemindDate);//udpated this field
        todoModel.setRepeat(todoModelSent.getRepeat());
        todoModel.setRepeatDay(todoModelSent.getRepeatDay());
        todoModel.setRepeatDate(todoModelSent.getRepeatDate());
        todoModel.setPriority(todoModelSent.getPriority());
        todoModel.setReminder(todoModelSent.isReminder());

        todoModel.setFollowUpDate(todoModelSent.getFollowUpDate());
        todoModel.setFollowUpTime(todoModelSent.getFollowUpTime());
        todoModel.setCreatedBy(todoModelSent.getCreatedBy());
        todoModel.setStatus(status);//udpated this field
        todoModel.setCreatedDate(todoModelSent.getCreatedDate());
        todoModel.setUpdatedDate(todoModelSent.getUpdatedDate());
        todoModel.setViewType(todoModelSent.getViewType());
        todoModel.setDelivered(todoModelSent.isDelivered());
        todoModel.setAssignTo(todoModelSent.getAssignTo());
        todoModel.setAssignMeAlso(todoModelSent.isAssignMeAlso());
        todoModel.setImages(todoModelSent.getImages());

        if (status.equals(Constants.STATUS_FINISHED)) {
            todoModel.setCompletedBy(sessionManager.getUserId());
        } else {
            todoModel.setCompletedBy(todoModelSent.getCompletedBy());
        }

        if (ConnectionChecker.isInternetAvailable(context)) {
            todoModel.setPendingForSync(false);
            todoModel.setWhatPending("");

            String fireId = todoModelSent.getFireId();
            if (todoModel.isReminder() && status.equals(Constants.STATUS_FINISHED)) {
                FirebaseRealtimeController.getInstance().deleteTodoByUserIdAndFireId(sessionManager.getUserId(), fireId);
            } else {

                FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, sessionManager.getUserId(), fireId).setOnCompleteListener(new OnCallCompleteListener() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onFailed() {

                        //whole onFailed block coded And also todoModel made as Class Field.
                        todoModel.setPendingForSync(true);
                        if (status.equals(Constants.STATUS_FINISHED) && todoModel.isReminder()) {
                            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_DELETE);
                        } else {
                            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_UPDATE);
                        }

                        RealmController.getInstance().addTodoItem(todoModel);

                    }
                });

                if (!todoModel.isReminder() && status.equals(Constants.STATUS_FINISHED)) {
                    finishTaskForAssignedUsers(todoModel, fireId);
                    notifyAssignedUserAboutTaskFinished(context, sessionManager, todoModel);
                }

            }
        } else {
            todoModel.setPendingForSync(true);
            if (status.equals(Constants.STATUS_FINISHED) && todoModel.isReminder()) {
                todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_DELETE);
            } else {
                todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_UPDATE);
            }

            RealmController.getInstance().addTodoItem(todoModel);
        }

        if (todoModel.getStatus().equals(Constants.STATUS_ACTIVE)) {
            CommonUtils.startForegroundReminderService(context, todoModel);
        }
    }

    public static void finishTaskForAssignedUsers(final TodoModel todoModel, String taskId) {
        final String[] arrayUsers = getArrayFromString(todoModel.getAssignTo());

        for (String arrayUser : arrayUsers) {
            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, arrayUser, taskId).setOnCompleteListener(new OnCallCompleteListener() {
                @Override
                public void onComplete() {

                }

                @Override
                public void onFailed() {
                    //whole onFailed block coded For add make this todoItem update in assigned users. It should re insert todoItem in to their lists on firebase
                    todoModel.setPendingForSync(true);

                    todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_INSERT);

                    RealmController.getInstance().addTodoItem(todoModel);
                }
            });
        }

    }

    public static void notifyAssignedUserAboutTaskFinished(Context context, SessionManager sessionManager, TodoModel todoModel) {

        String assignTo = todoModel.getAssignTo();
        String body = todoModel.getTitle();

        if (!assignTo.equals("")) {
            String[] arrayUsers = getArrayFromString(assignTo);
            for (String arrayUser : arrayUsers) {
                if (!arrayUser.equals(sessionManager.getUserId())) {
                    //extract number from here
                    pushNotificationForThisUser(context, arrayUser, body, context.getString(R.string.task_finished_title));
                }
            }

            if (!todoModel.getCreatedBy().equals(sessionManager.getUserId())) {
                pushNotificationForThisUser(context, todoModel.getCreatedBy(), body, context.getString(R.string.task_finished_title));
            }
        }
    }

    public static void pushNotificationForThisUser(Context context, String userId, final String body, final String title) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myDbRef = database.getReference();
        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null && userModel.getUserId() != null) {
                    //registered
                    if (userModel.getFireToken() != null) {
                        String[] fireTokenArray = userModel.getFireToken().split(",");

                        for (String aFireTokenArray : fireTokenArray) { //send to multiple users

                            JsonObject bodyJson = CommonUtils.createJsonObjectFromValues(aFireTokenArray, body, title, "");

                            NetworkConnectivity.pushNotification(context,
                                    aFireTokenArray,
                                    bodyJson);
                        }

                        for (String s : fireTokenArray) {
                            JsonObject bodyJson = CommonUtils.createJsonObjectFromValues(s, body, title, "");
                            NetworkConnectivity.pushNotification(context,
                                    s,
                                    bodyJson);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
