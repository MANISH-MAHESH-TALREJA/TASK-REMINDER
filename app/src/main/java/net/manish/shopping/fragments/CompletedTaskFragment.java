package net.manish.shopping.fragments;

import static net.manish.shopping.utils.Constants.KEY_ASSIGNTO;
import static net.manish.shopping.utils.Constants.KEY_ASSIGN_ME_ALSO;
import static net.manish.shopping.utils.Constants.KEY_COMPLETED_BY;
import static net.manish.shopping.utils.Constants.KEY_CREATED_BY;
import static net.manish.shopping.utils.Constants.KEY_CREATED_DATE;
import static net.manish.shopping.utils.Constants.KEY_DATE;
import static net.manish.shopping.utils.Constants.KEY_FIRE_ID;
import static net.manish.shopping.utils.Constants.KEY_FOLLOW_UP_DATE;
import static net.manish.shopping.utils.Constants.KEY_FOLLOW_UP_TIME;
import static net.manish.shopping.utils.Constants.KEY_ID;
import static net.manish.shopping.utils.Constants.KEY_IMAGES;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import net.manish.shopping.R;
import net.manish.shopping.activities.TodoDetailsActivity;
import net.manish.shopping.adapter.TodoListAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.helper.RecyclerTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CompletedTaskFragment extends Fragment {

    private RecyclerView recyclerview;
    private LinearLayout lnrEmpty;
    private final List<TodoModel> taskList = new ArrayList<>();
    private TodoListAdapter adapter;
    private View view;
    private SessionManager sessionManager;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseRealtimeController.init(getActivity());

        sessionManager = new SessionManager(getActivity());

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

                LinearLayout lnrRoot = view.findViewById(R.id.lnr_root);
                ImageView imageViewRemove = view.findViewById(R.id.iv_remove);

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
                    i.putExtra(KEY_COMPLETED_BY, taskList.get(position).getCompletedBy());
                    i.putExtra(KEY_IMAGES, taskList.get(position).getImages());

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

    @SuppressLint("NotifyDataSetChanged")
    private void getAllTasks() {

        taskList.clear();
        taskList.addAll(RealmController.getInstance().getAllTasksByStatus(Constants.STATUS_FINISHED));
        hideRecyclerView();
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // Refresh your fragment here
            getAllTasks();
        }
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
                if (post != null) {
                    RealmController.getInstance().removeTodoByFireId(post.getFireId());
                }
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
}