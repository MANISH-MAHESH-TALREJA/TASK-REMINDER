package net.manish.shopping.fragments;

import static net.manish.shopping.utils.CommonUtils.getArrayFromString;
import static net.manish.shopping.utils.Constants.KEY_ASSIGNTO;
import static net.manish.shopping.utils.Constants.KEY_ASSIGN_ME_ALSO;
import static net.manish.shopping.utils.Constants.KEY_CREATED_BY;
import static net.manish.shopping.utils.Constants.KEY_CREATED_DATE;
import static net.manish.shopping.utils.Constants.KEY_DATE;
import static net.manish.shopping.utils.Constants.KEY_FIRE_ID;
import static net.manish.shopping.utils.Constants.KEY_FOLLOW_UP_DATE;
import static net.manish.shopping.utils.Constants.KEY_FOLLOW_UP_TIME;
import static net.manish.shopping.utils.Constants.KEY_ID;
import static net.manish.shopping.utils.Constants.KEY_IS_DELIVERED;
import static net.manish.shopping.utils.Constants.KEY_IS_REMINDER;
import static net.manish.shopping.utils.Constants.KEY_NOTE;
import static net.manish.shopping.utils.Constants.KEY_PRIORITY;
import static net.manish.shopping.utils.Constants.KEY_REMIND_TIME;
import static net.manish.shopping.utils.Constants.KEY_REPEAT;
import static net.manish.shopping.utils.Constants.KEY_REPEAT_DATE;
import static net.manish.shopping.utils.Constants.KEY_REPEAT_DAY;
import static net.manish.shopping.utils.Constants.KEY_STATUS;
import static net.manish.shopping.utils.Constants.KEY_TIME;
import static net.manish.shopping.utils.Constants.KEY_TITLE;
import static net.manish.shopping.utils.Constants.KEY_UPDATED_DATE;
import static net.manish.shopping.utils.Constants.KEY_VIEW_TYPE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import net.manish.shopping.R;
import net.manish.shopping.activities.TodoDetailsActivity;
import net.manish.shopping.adapter.TodoListAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.helper.RecyclerTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PendingTaskFragment extends Fragment {

    private RecyclerView recyclerview;
    private LinearLayout lnrEmpty;
    private final List<TodoModel> taskList = new ArrayList<>();
    private TodoListAdapter adapter;
    private View view;
    private DateAndTimePicker dateAndTimePicker;
    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;
    private String comment = "";
    private String followUpDate = "";
    private String followUpTime = "";
    private int currentItemPosition;
    private int i = 0;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseRealtimeController.init(getActivity());

        dateAndTimePicker = new DateAndTimePicker(getActivity());
        sessionManager = new SessionManager(getActivity());
        networkConnectivity = new NetworkConnectivity(getActivity());

        setClickListenerForDateTime();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_pending_task, container, false);

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        userId = sessionManager.getUserId();

        findViews();
        getAllTasks();
        setRealTimeDataChangeListener();

        return view;
    }

    private void setClickListenerForDateTime() {
        dateAndTimePicker.setOnDateSelectListener(date -> followUpDate = date);

        dateAndTimePicker.setOnTimeSelectListener(t -> {

            followUpTime = t;
            changeFollowUpDateAndTime();

            followUpTime = "";
            followUpDate = "";
        });
    }

    private void findViews() {
        recyclerview = view.findViewById(R.id.recycler_view);
        lnrEmpty = view.findViewById(R.id.lnr_empty);

        //setup recycler view
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TodoListAdapter(taskList, getActivity());
        recyclerview.setAdapter(adapter);

        recyclerview.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerview, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                currentItemPosition = position;

                LinearLayout lnrRoot = view.findViewById(R.id.lnr_root);
                ImageView imageViewRemove = view.findViewById(R.id.iv_remove);

                if (!taskList.get(position).isReminder()) {
                    popupDialogForNote(view, position);
                }

                imageViewRemove.setOnClickListener(v -> deleteConfirmationAlert(position));

                lnrRoot.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), TodoDetailsActivity.class);
                    i.putExtra(KEY_ID, taskList.get(position).getId());
                    i.putExtra(KEY_FIRE_ID, taskList.get(position).getFireId());
                    i.putExtra(KEY_TITLE, taskList.get(position).getTitle());
                    i.putExtra(KEY_NOTE, taskList.get(position).getNote());
                    i.putExtra(KEY_DATE, taskList.get(position).getDate());
                    i.putExtra(KEY_TIME, taskList.get(position).getTime());
                    i.putExtra(KEY_REMIND_TIME, taskList.get(position).getRemindTime());
                    i.putExtra(KEY_REPEAT, taskList.get(position).getRepeat());
                    i.putExtra(KEY_REPEAT_DAY, taskList.get(position).getRepeatDay());
                    i.putExtra(KEY_REPEAT_DATE, taskList.get(position).getRepeatDate());
                    i.putExtra(KEY_PRIORITY, taskList.get(position).getPriority());
                    i.putExtra(KEY_IS_REMINDER, taskList.get(position).isReminder());

                    i.putExtra(KEY_FOLLOW_UP_DATE, taskList.get(position).getFollowUpDate());
                    i.putExtra(KEY_FOLLOW_UP_TIME, taskList.get(position).getFollowUpTime());
                    i.putExtra(KEY_CREATED_BY, taskList.get(position).getCreatedBy());
                    i.putExtra(KEY_STATUS, taskList.get(position).getStatus());
                    i.putExtra(KEY_CREATED_DATE, taskList.get(position).getCreatedDate());
                    i.putExtra(KEY_UPDATED_DATE, taskList.get(position).getUpdatedDate());
                    i.putExtra(KEY_VIEW_TYPE, taskList.get(position).getViewType());
                    i.putExtra(KEY_IS_DELIVERED, taskList.get(position).isDelivered());
                    i.putExtra(KEY_ASSIGNTO, taskList.get(position).getAssignTo());
                    i.putExtra(KEY_ASSIGN_ME_ALSO, taskList.get(position).isAssignMeAlso());
                    i.putExtra(Constants.KEY_IMAGES, taskList.get(position).getImages());

                    startActivity(i);
                });
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    private void hideRecyclerView() {
        if (taskList.size() == 0) {
            recyclerview.setVisibility(View.GONE);
            lnrEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerview.setVisibility(View.VISIBLE);
            lnrEmpty.setVisibility(View.GONE);
        }
    }

    private void deleteConfirmationAlert(final int position) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.delete_alert))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    //delete from firebase

                    final String fireIdForDelete = taskList.get(position).getFireId();

                    if (ConnectionChecker.isInternetAvailable(requireActivity())) {
                        FirebaseRealtimeController.getInstance().deleteTodoByFireId(fireIdForDelete).setOnCompleteListener(new OnCallCompleteListener() {
                            @Override
                            public void onComplete() {
                                dialog.dismiss();
                                getAllTasks();
                            }

                            @Override
                            public void onFailed() {
                                dialog.dismiss();
                            }
                        });
                    } else {
                        TodoModel todoModel = RealmController.getInstance().getTodoByFireId(taskList.get(position).getFireId());
                        todoModel.setPendingForSync(true);
                        todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_DELETE);
                        todoModel.setStatus(Constants.STATUS_FINISHED);

                        RealmController.getInstance().addTodoItem(todoModel);

                        dialog.dismiss();

                        getAllTasks();
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                    dialog.dismiss();
                }).show();
    }

    private void popupDialogForNote(View view, final int position) {
        final AppCompatCheckBox checkBox = view.findViewById(R.id.checkbox_done);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                DialogFragmentFinishTask dialgFinishTask = new DialogFragmentFinishTask( "MainActivity");
                dialgFinishTask.show(requireActivity().getSupportFragmentManager(), "");
                dialgFinishTask.setOnItemClickedListener(new OnItemClickedListener() {
                    @Override
                    public void onItemClicked(List<ContactModel> selectedContacts) {
                    }

                    @Override
                    public void onTaskFinished(boolean isFinished, String note) {
                        if (isFinished) {
                            finishTask(position, note);
                        } else {
                            checkBox.setChecked(false);
                        }
                    }

                    @Override
                    public void onAddComment(String note) {

                        checkBox.setChecked(false);
                        comment = note;
                        addComment(position);
                    }
                });
            }
        });
    }

    public int getNextId() {
        try {
            Number number = RealmController.getInstance().getRealm().where(CommentModel.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    private void popupAlert() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.alert_add_followup_date))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    dialog.dismiss();

                    requireActivity().runOnUiThread(() -> {
                        dateAndTimePicker = new DateAndTimePicker(getActivity());
                        dateAndTimePicker.timePicker();
                        dateAndTimePicker.datePicker();
                    });
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                    dialog.dismiss();
                }).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllTasks() {

        taskList.clear();
        taskList.addAll(RealmController.getInstance().getAllTasksByStatus(Constants.STATUS_ACTIVE));
        hideRecyclerView();
        adapter.notifyDataSetChanged();

    }

    private void setRealTimeDataChangeListener() {
        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TodoModel post = dataSnapshot.getValue(TodoModel.class);
                RealmController.getInstance().addTodoItem(post);
                getAllTasks();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TodoModel post = dataSnapshot.getValue(TodoModel.class);
                RealmController.getInstance().addTodoItem(post);
                getAllTasks();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                TodoModel post = dataSnapshot.getValue(TodoModel.class);
                RealmController.getInstance().removeTodoByFireId(Objects.requireNonNull(post).getFireId());
                getAllTasks();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // Refresh your fragment here
            if (recyclerview != null) {
                getAllTasks();
            }
        }
    }

    private void finishTaskForAssignedUsers(final TodoModel todoModel, String taskId) {
        final String[] arrayUsers = getArrayFromString(todoModel.getAssignTo());
        for (i = 0; i < arrayUsers.length; i++) {
            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, arrayUsers[i], taskId).setOnCompleteListener(new OnCallCompleteListener() {
                @Override
                public void onComplete() {

                    //after finish task from all assigned user's then refresh tasklist
                    if (i == arrayUsers.length - 1) {
                        getAllTasks();
                    }
                }

                @Override
                public void onFailed() {

                }
            });
        }
    }

    private void notifyAssignedUserAboutTaskFinished(TodoModel todoModel) {

        String assignTo = todoModel.getAssignTo();
        String body = todoModel.getTitle();

        if (!assignTo.equals("")) {
            String[] arrayUsers = getArrayFromString(assignTo);
            for (String arrayUser : arrayUsers) {
                if (!arrayUser.equals(sessionManager.getUserId())) {
                    networkConnectivity.pushNotificationForThisUser(arrayUser, body, getString(R.string.task_finished_title), Constants.FINISH_TASK);
                }
            }

            if (!todoModel.getCreatedBy().equals(sessionManager.getUserId())) {
                networkConnectivity.pushNotificationForThisUser(todoModel.getCreatedBy(), body, getString(R.string.task_finished_title), Constants.FINISH_TASK);
            }
        }
    }

    private void pushNotificationForThisUser(String userId, final String body, final String title) {

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null && userModel.getUserId() != null) {
                    //registered
                    JsonObject bodyJson = CommonUtils.createJsonObjectFromValues(userModel.getUserId(), body, title, "");

                    NetworkConnectivity.pushNotification(getActivity(),
                            userModel.getFireToken(),
                            bodyJson);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //========================Below code is for Tasks&reminders===========
    private void finishTask(int position, String note) {
        TodoModel todoManaged = RealmController.getInstance().getTodoByFireId(taskList.get(currentItemPosition).getFireId());
        TodoModel todoModel = new TodoModel();

        todoModel.setId(todoManaged.getId());
        todoModel.setFireId(todoManaged.getFireId());
        todoModel.setTitle(todoManaged.getTitle());
        if (note.equals("")) {
            todoModel.setNote(todoManaged.getNote());
        } else {
            todoModel.setNote(note);//new added update
        }
        todoModel.setDate(todoManaged.getDate());
        todoModel.setTime(todoManaged.getTime());
        todoModel.setRemindTime(todoManaged.getRemindTime());
        todoModel.setRepeat(todoManaged.getRepeat());
        todoModel.setRepeatDay(todoManaged.getRepeatDay());
        todoModel.setRepeatDate(todoManaged.getRepeatDate());
        todoModel.setPriority(todoManaged.getPriority());
        todoModel.setReminder(todoManaged.isReminder());

        todoModel.setFollowUpDate(followUpDate);
        todoModel.setFollowUpTime(followUpTime);
        todoModel.setCreatedBy(todoManaged.getCreatedBy());
        todoModel.setStatus(Constants.STATUS_FINISHED);
        todoModel.setCompletedBy(sessionManager.getUserId());
        todoModel.setCreatedDate(todoManaged.getCreatedDate());
        todoModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTime());
        todoModel.setViewType(taskList.get(position).getViewType());
        todoModel.setDelivered(taskList.get(position).isDelivered());
        todoModel.setAssignTo(taskList.get(position).getAssignTo());
        todoModel.setAssignMeAlso(taskList.get(position).isAssignMeAlso());
        todoModel.setImages(taskList.get(position).getImages());

        if (ConnectionChecker.isInternetAvailable(requireActivity())) {
            todoModel.setPendingForSync(false);
            todoModel.setWhatPending("");

            String fireId = taskList.get(position).getFireId();
            String createdBy = taskList.get(position).getCreatedBy();

            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, createdBy, fireId);//finish task for self
            if (!todoManaged.getAssignTo().equals("")) {
                //finish task from assigned users
                finishTaskForAssignedUsers(todoModel, fireId);
                notifyAssignedUserAboutTaskFinished(todoModel);
            }

        } else {
            todoModel.setPendingForSync(true);
            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_UPDATE);

            RealmController.getInstance().addTodoItem(todoModel);
        }

        getAllTasks();

    }

    private void changeFollowUpDateAndTime() {

        String fireId = taskList.get(currentItemPosition).getFireId();

        TodoModel todoManaged = RealmController.getInstance().getTodoByFireId(fireId);
        TodoModel todoModel = new TodoModel();

        todoModel.setId(todoManaged.getId());
        todoModel.setFireId(todoManaged.getFireId());
        todoModel.setTitle(todoManaged.getTitle());
        todoModel.setNote(todoManaged.getNote());
        todoModel.setDate(todoManaged.getDate());
        todoModel.setTime(followUpTime);
        todoModel.setRemindTime(followUpDate + " " + followUpTime);//new added update
        todoModel.setRepeat(todoManaged.getRepeat());
        todoModel.setRepeatDay(todoManaged.getRepeatDay());
        todoModel.setRepeatDate(todoManaged.getRepeatDate());
        todoModel.setPriority(todoManaged.getPriority());
        todoModel.setReminder(todoManaged.isReminder());

        todoModel.setFollowUpDate(followUpDate);
        todoModel.setFollowUpTime(followUpTime);
        todoModel.setCreatedBy(todoManaged.getCreatedBy());
        todoModel.setStatus(Constants.STATUS_ACTIVE);//udpated this field
        todoModel.setCreatedDate(todoManaged.getCreatedDate());
        todoModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTime());//updated here
        todoModel.setViewType(todoManaged.getViewType());
        todoModel.setDelivered(todoManaged.isDelivered());
        todoModel.setAssignTo(todoManaged.getAssignTo());
        todoModel.setAssignMeAlso(todoManaged.isAssignMeAlso());
        todoModel.setImages(todoManaged.getImages());

        if (todoModel.getStatus().equals(Constants.STATUS_FINISHED)) {
            todoModel.setCompletedBy(sessionManager.getUserId());
        } else {
            todoModel.setCompletedBy(todoManaged.getCompletedBy());
        }

        if (ConnectionChecker.isInternetAvailable(requireActivity())) {
            todoModel.setPendingForSync(false);
            todoModel.setWhatPending("");

            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, sessionManager.getUserId(), fireId).setOnCompleteListener(new OnCallCompleteListener() {
                @Override
                public void onComplete() {
                    followUpTime = "";
                    followUpDate = "";
                }

                @Override
                public void onFailed() {
                    followUpTime = "";
                    followUpDate = "";
                }
            });
        } else {
            todoModel.setPendingForSync(true);
            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_UPDATE);

            RealmController.getInstance().addTodoItem(todoModel);
        }

    }

    //========================Below code is for Comments===============
    private void addComment(final int position) {

        String taskFireId = taskList.get(position).getFireId();
        int id = getNextId();
        String cFireId = myDbRef.child(Constants.TABLE_COMMENTS).child(taskFireId).push().getKey();

        CommentModel commentModel = new CommentModel();
        commentModel.setId(id);
        commentModel.setcFireId(cFireId);
        commentModel.setTaskId(taskFireId);
        commentModel.setUserId(sessionManager.getUserId());
        commentModel.setPhone(sessionManager.getPhoneNumber());
        commentModel.setSenderName(sessionManager.getUserName());
        commentModel.setComment(comment);
        commentModel.setCreatedDate(dateAndTimePicker.getCurrentDateTime());
        commentModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTimeWithSecond());

        if (ConnectionChecker.isInternetAvailable(requireActivity())) {
            commentModel.setPendingForSync(false);
            commentModel.setWhatPending("");

            FirebaseRealtimeController.getInstance().addOrUpdateCommentByTaskId(commentModel, taskFireId, cFireId).setOnCompleteListener(new OnCallCompleteListener() {
                @Override
                public void onComplete() {

                    String assignTo = taskList.get(position).getAssignTo();
                    String title = taskList.get(position).getTitle();

                    if (!assignTo.equals("")) {
                        String[] arrayUsers = getArrayFromString(assignTo);
                        for (String arrayUser : arrayUsers) {
                            if (!arrayUser.equals(sessionManager.getUserId())) {
                                pushNotificationForThisUser(arrayUser, comment, title);
                                networkConnectivity.pushNotificationForThisUser(arrayUser, comment, title, Constants.CREATE_NEW_COMMENT);
                            }
                        }

                        if (!taskList.get(position).getCreatedBy().equals(sessionManager.getUserId())) {
                            networkConnectivity.pushNotificationForThisUser(taskList.get(position).getCreatedBy(), comment, title, Constants.CREATE_NEW_COMMENT);
                        }
                    }
                }

                @Override
                public void onFailed() {

                }
            });
        } else {
            commentModel.setPendingForSync(true);
            commentModel.setWhatPending(Constants.SYNC_PENDING_FOR_INSERT);

            RealmController.getInstance().addComment(commentModel);
        }

        //ask to user by popup for add next follow up date
        popupAlert();
    }

}