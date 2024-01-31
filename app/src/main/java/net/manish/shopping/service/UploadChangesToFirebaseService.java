package net.manish.shopping.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UploadChangesToFirebaseService extends Service {

    private static final String TAG = "SyyService";
    public boolean isWorkFinished = false;

    public List<TodoModel> todoPendingSyncList = new ArrayList<>();
    public List<CommentModel> commentPendingSyncList = new ArrayList<>();
    public List<BlockContactModel> blockedNumberList = new ArrayList<>();

    public SessionManager sessionManager;
    public NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    public UploadChangesToFirebaseService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        sessionManager = new SessionManager(this);
        networkConnectivity = new NetworkConnectivity(this);

        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        userId = sessionManager.getUserId();

        syncDatabase();
        return START_STICKY;
    }

    private void syncDatabase() {
        //get here all records which is pending for sync with firebase including (Todos & Comments)
        syncTodoList();
    }

    private void syncTodoList() {

        todoPendingSyncList.clear();
        todoPendingSyncList.addAll(RealmController.getInstance().getAllPendingTodosForSync());

        if (todoPendingSyncList.size() > 0) {

            for (int i = 0; i < todoPendingSyncList.size(); i++) {

                TodoModel todoModel = updatePendingStatusForSyncValues(todoPendingSyncList.get(i));

                switch (todoPendingSyncList.get(i).getWhatPending()) {
                    case Constants.SYNC_PENDING_FOR_INSERT:
                        insetTodos(todoModel);
                        break;
                    case Constants.SYNC_PENDING_FOR_UPDATE:

                        FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, userId, todoModel.getFireId());
                        break;
                    case Constants.SYNC_PENDING_FOR_DELETE:

                        FirebaseRealtimeController.getInstance().deleteTodoByFireId(todoModel.getFireId());

                        if (!todoModel.getAssignTo().equals("")) {//delete from assigned user's list
                            if (todoModel.getCreatedBy().equals(sessionManager.getUserId())) {
                                String fireIdForDelete = todoModel.getFireId();
                                String[] arrayUsers = CommonUtils.getArrayFromString(todoModel.getAssignTo());
                                for (String arrayUser : arrayUsers) {
                                    FirebaseRealtimeController.getInstance().deleteTodoByUserIdAndFireId(arrayUser, fireIdForDelete);
                                }
                            }
                        }

                        break;
                }
            }
        }
        syncCommentList();
    }

    private void insetTodos(TodoModel todoModel) {

        String assignTo = todoModel.getAssignTo();
        if (!todoModel.getAssignTo().equals("")) {
            String[] arrayAssigned = getArrayFromString(todoModel.getAssignTo());

            for (String s : arrayAssigned) {
                todoModel.setAssignTo(assignTo);
                checkIsNumberBlockedOrNot(todoModel, s, todoModel.getFireId());
            }

        }

        if (todoModel.isAssignMeAlso()) {
            if (todoModel.getAssignTo().equals("[]")) {
                todoModel.setAssignTo("");
            } else {
                todoModel.setAssignTo(assignTo);
            }
            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, userId, todoModel.getFireId());
        }
    }

    private void checkIsNumberBlockedOrNot(final TodoModel todoModel, final String userId, final String fireId) {

        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).orderByChild("number").equalTo(sessionManager.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                BlockContactModel post = new BlockContactModel();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    post = postSnapshot.getValue(BlockContactModel.class);
                }

                if (post != null) {
                    if (post.getUserId() != null) {
                        //you are blocked for this user
                        Mylogger.getInstance().printLog(TAG, "sync bloked true " + post.getNumber());

                    } else {
                        Mylogger.getInstance().printLog(TAG, "sync block false ");
                        FirebaseRealtimeController.getInstance()
                                .addOrUpdateTodoInFirebaseForMultipleAssign(todoModel, userId, fireId)
                                .setOnCompleteListener(new OnCallCompleteListener() {
                                    @Override
                                    public void onComplete() {
                                    }

                                    @Override
                                    public void onFailed() {

                                    }
                                });
                        sendNotification(userId, todoModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog("Crr", "Failed to read value : " + error.toException());
            }
        });

    }

    private void sendNotification(String userId, TodoModel todoModel) {

        final String todo;
        if (todoModel.isReminder()) {
            todo = "Reminder";
        } else {
            todo = "Task";
        }

        String title = "Assigned " + "New " + todo;
        String body = todoModel.getTitle();

        networkConnectivity.pushNotificationForThisUser(userId, body, title, Constants.CREATE_NEW_TODO);
    }

    private String[] getArrayFromString(String assignTo) {

        try {
            JSONArray jsonArray = new JSONArray(assignTo);
            String[] strArr = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {

                strArr[i] = jsonArray.getString(i);
            }

            return strArr;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    private TodoModel updatePendingStatusForSyncValues(TodoModel todoManaged) {

        TodoModel todoModel = new TodoModel();

        todoModel.setId(todoManaged.getId());
        todoModel.setFireId(todoManaged.getFireId());
        todoModel.setTitle(todoManaged.getTitle());
        todoModel.setNote(todoManaged.getNote());
        todoModel.setDate(todoManaged.getDate());
        todoModel.setTime(todoManaged.getTime());
        todoModel.setRemindTime(todoManaged.getRemindTime());
        todoModel.setRepeat(todoManaged.getRepeat());
        todoModel.setRepeatDay(todoManaged.getRepeatDay());
        todoModel.setRepeatDate(todoManaged.getRepeatDate());
        todoModel.setPriority(todoManaged.getPriority());
        todoModel.setReminder(todoManaged.isReminder());
        todoModel.setFollowUpDate(todoManaged.getFollowUpDate());
        todoModel.setFollowUpTime(todoManaged.getFollowUpTime());
        todoModel.setCreatedBy(todoManaged.getCreatedBy());
        todoModel.setStatus(todoManaged.getStatus());
        todoModel.setCreatedDate(todoManaged.getCreatedDate());
        todoModel.setUpdatedDate(todoManaged.getUpdatedDate());
        todoModel.setViewType(todoManaged.getViewType());
        todoModel.setDelivered(todoManaged.isDelivered());
        todoModel.setAssignTo(todoManaged.getAssignTo());
        todoModel.setAssignMeAlso(todoManaged.isAssignMeAlso());
        todoModel.setCompletedBy(todoManaged.getCompletedBy());

        todoModel.setPendingForSync(false);//udpate this value
        todoModel.setWhatPending(""); // Update this value


        return todoModel;
    }

    //---For Comments-------
    private void syncCommentList() {
        commentPendingSyncList.clear();
        commentPendingSyncList.addAll(RealmController.getInstance().getAllPendingCommentsForSync());

        if (commentPendingSyncList.size() > 0) {

            for (int i = 0; i < commentPendingSyncList.size(); i++) {

                CommentModel commentModel = getUpdatedPendingStatusForValues(commentPendingSyncList.get(i));

                switch (commentPendingSyncList.get(i).getWhatPending()) {
                    case Constants.SYNC_PENDING_FOR_INSERT:
                    case Constants.SYNC_PENDING_FOR_UPDATE:

                        FirebaseRealtimeController.getInstance().addOrUpdateCommentByTaskId(commentModel, commentModel.getTaskId(), commentModel.getcFireId());
                        break;
                    case Constants.SYNC_PENDING_FOR_DELETE:

                        FirebaseRealtimeController.getInstance().deleteCommentBycFireId(commentModel.getcFireId());
                        break;
                }
            }
        }
        syncBlockedList();
    }

    private CommentModel getUpdatedPendingStatusForValues(CommentModel managedComment) {

        CommentModel commentModel = new CommentModel();
        commentModel.setId(managedComment.getId());
        commentModel.setcFireId(managedComment.getcFireId());
        commentModel.setTaskId(managedComment.getTaskId());
        commentModel.setUserId(sessionManager.getUserId());
        commentModel.setComment(managedComment.getComment());
        commentModel.setCreatedDate(managedComment.getCreatedDate());
        commentModel.setUpdatedDate(managedComment.getUpdatedDate());

        commentModel.setPendingForSync(false);//update this value
        commentModel.setWhatPending("");//update this value

        return commentModel;
    }

    //-----For Blocked Number List---------
    private void syncBlockedList() {
        blockedNumberList.clear();
        blockedNumberList.addAll(RealmController.getInstance().getAllBlockedAndUnblockedNumber());

        if (blockedNumberList.size() > 0) {

            for (int i = 0; i < blockedNumberList.size(); i++) {

                BlockContactModel blockContactModel = getUpdatedPendingStatusForBlockValues(blockedNumberList.get(i));

                switch (blockedNumberList.get(i).getWhatPending()) {
                    case Constants.SYNC_PENDING_FOR_INSERT:
                        FirebaseRealtimeController.getInstance().addOrUpdateBlockUser(blockContactModel, sessionManager.getUserId(), blockContactModel.getUniqueId());
                        RealmController.getInstance().removeBlockedNumberByUniqueId(blockContactModel.getUniqueId());
                        break;
                    case Constants.SYNC_PENDING_FOR_UPDATE:
                        FirebaseRealtimeController.getInstance().addOrUpdateBlockUser(blockContactModel, sessionManager.getUserId(), blockContactModel.getUniqueId());
                        break;
                    case Constants.SYNC_PENDING_FOR_DELETE:

                        FirebaseRealtimeController.getInstance().unblockUserByFireId(sessionManager.getUserId(), blockContactModel.getUniqueId());
                        RealmController.getInstance().removeBlockedNumberByUniqueId(blockContactModel.getUniqueId());
                        break;
                }
            }
        }

        stopService();
    }

    private BlockContactModel getUpdatedPendingStatusForBlockValues(BlockContactModel managedBlockModel) {

        BlockContactModel blockContactModel = new BlockContactModel();
        blockContactModel.setId(managedBlockModel.getId());
        blockContactModel.setUserId(managedBlockModel.getUserId());
        blockContactModel.setUniqueId(managedBlockModel.getUniqueId());
        blockContactModel.setName(managedBlockModel.getName());
        blockContactModel.setNumber(managedBlockModel.getNumber());
        blockContactModel.setBlocked(managedBlockModel.isBlocked());
        blockContactModel.setPendingForSync(false);
        blockContactModel.setWhatPending("");

        return blockContactModel;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!isWorkFinished) {
            Intent broadcastIntent = new Intent(this, InternetStateChangeBR.class);
            sendBroadcast(broadcastIntent);
        }
    }

    private void stopService() {

        isWorkFinished = true;
        this.stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}