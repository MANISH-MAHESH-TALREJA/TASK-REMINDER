package net.manish.shopping.activities;

import static net.manish.shopping.utils.CommonUtils.getArrayFromString;
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
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import net.manish.shopping.R;
import net.manish.shopping.adapter.TodoListAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.fragments.DialogFragmentFinishTask;
import net.manish.shopping.fragments.FilterDialogFragmentBottomSheet;
import net.manish.shopping.fragments.SnoozeDialogFragmentBottomSheet;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.listeners.OnFilterClickedListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.notification.api.FirebaseUtils;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.service.ForegroundService;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private LinearLayout lnrDateFilter, lnrEmpty;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView tvDayTitle;
    private TextView tvTotalTodo;
    private RecyclerView mRecyclerView;
    private TodoListAdapter adapter;
    private final List<TodoModel> mainList = new ArrayList<>();
    private final List<TodoModel> list = new ArrayList<>();

    private SessionManager sessionManager;
    private DateAndTimePicker dateAndTimePicker;

    private String selectedTime = "", comment = "", followUpDate = "", followUpTime = "";
    private int currentItemPosition, i;

    private NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;
    private String userId; //for authentication
    private boolean isCheckedByCurrentDevice = false;
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Firebase utils
        FirebaseUtils.init(this);
        FirebaseRealtimeController.init(this);

        networkConnectivity = new NetworkConnectivity(this);
        sessionManager = new SessionManager(this);
        dateAndTimePicker = new DateAndTimePicker(MainActivity.this);

        //It is used to prevent from the misbehave while user logged out from the app already and tap on foreground notification.
        if (!sessionManager.isLoggedIn()) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        userId = sessionManager.getUserId();

        setClickListenerForDateTime();

        generateFirebaseToken();
        findViews();
        setupToolbarAndDrawer();
        setupRecyclerView();
        setOnRealDataChangeListeners();
        askForDrawOverAppPermissionForOrioAndAbove();

    }

    private void askForDrawOverAppPermissionForOrioAndAbove() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            //noinspection deprecation
            startActivityForResult(intent, Constants.REQUEST_CODE_OVERLAY_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_OVERLAY_PERMISSION) {
                if (!Settings.canDrawOverlays(this)) {
                    askForDrawOverAppPermissionForOrioAndAbove();
                }
            }
        }
    }

    private void generateFirebaseToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String token = task.getResult();
                        FirebaseUtils.newInstance().saveFirebaseToken(token);
                        sessionManager.setFirebaseToken(token);
                    }
                });
    }

    private void findViews() {

        //findViews
        toolbar = findViewById(R.id.toolbar);
        TextView tvName = findViewById(R.id.tv_name);
        tvDayTitle = findViewById(R.id.tv_day_title);
        TextView tvDate = findViewById(R.id.tv_date);
        tvTotalTodo = findViewById(R.id.tv_total_todo);
        fab = findViewById(R.id.fab);
        lnrDateFilter = findViewById(R.id.lnr_day_filter);
        lnrEmpty = findViewById(R.id.lnr_empty);
        mRecyclerView = findViewById(R.id.rv);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        ImageView ivDayNight = findViewById(R.id.tv_day_night);
        Glide.with(this)
                .load(R.drawable.sun_gif_blue_transparent)
                .into(ivDayNight);

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {

            if (verticalOffset <= -200) {
                fab.hide();
            } else {
                fab.show();
            }
        });


        //init values
        tvDate.setText(dateAndTimePicker.getCurrentDate());
        tvName.setText(String.format("%s %s", getString(R.string.hello), sessionManager.getUserName()));

        clickListeners();

    }

    private void clickListeners() {
        fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CreateNewTodoActivity.class)));

        lnrDateFilter.setOnClickListener(v -> {
            FilterDialogFragmentBottomSheet filterDialog = new FilterDialogFragmentBottomSheet(selectedTime);
            filterDialog.show(getSupportFragmentManager(), "");
            FilterDialogFragmentBottomSheet.setOnFilterClickedListener(new OnFilterClickedListener() {
                @Override
                public void onItemClicked(String time) {
                    selectedTime = time;
                    tvDayTitle.setText(time);

                    filterList(time);
                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onFilterCleared() {
                    tvDayTitle.setText(getResources().getString(R.string.today));
                    list.clear();
                    list.addAll(mainList);
                    adapter.notifyDataSetChanged();
                    hideRecyclerView();
                }
            });
        });

    }

    private void setupToolbarAndDrawer() {

        //setup toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.dashboard));

        //setup drawer nav layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //setup drawer header values
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tv_name_header);
        TextView tvPhone = headerView.findViewById(R.id.tv_phone_header);

        tvName.setText(sessionManager.getUserName());
        tvPhone.setText(sessionManager.getPhoneNumber());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshListFromLocal() {

        list.clear();
        mainList.clear();
        adapter.notifyDataSetChanged();

        mainList.addAll(RealmController.getInstance().getAllActiveTodos(dateAndTimePicker.getCurrentDate()));
        list.addAll(mainList);
        adapter.notifyDataSetChanged();
        hideRecyclerView();

        tvTotalTodo.setText(String.valueOf(list.size()));

        scheduleAllJobsWhichNeedsToBeDone();
    }

    private void scheduleAllJobsWhichNeedsToBeDone() {

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getStatus().equals(Constants.STATUS_ACTIVE)) {

                //Now, check that if there is any un-prompted reminder/task available?
                Date remindTime = dateAndTimePicker.convertStringToDateTime(list.get(i).getRemindTime());
                Date currentTime = Calendar.getInstance().getTime();

                if (remindTime.getTime() < currentTime.getTime()) {
                    //Now, check that if there is any 'un-prompted' reminder/task available?
                    //if yes, then re-schedule it here.
                    CommonUtils.handleTodoBasedOnRepeat(MainActivity.this, list.get(i));
                } else {
                    CommonUtils.startForegroundReminderService(this, list.get(i));
                }
            }
        }
    }

    private void hideRecyclerView() {

        if (list.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            lnrEmpty.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            lnrEmpty.setVisibility(View.GONE);
        }

        sortList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortList() {
        list.sort(Comparator.comparing(o -> dateAndTimePicker.convertStringToDateTime(o.getRemindTime())));
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterList(String time) {

        String destinationDate;
        String currentDate = dateAndTimePicker.getCurrentDate();
        Date curDate = dateAndTimePicker.convertStringToDate(currentDate);
        Date destiDate;

        switch (time) {
            case Constants.FILTER_TOMORROW:

                destinationDate = dateAndTimePicker.incrementDateInDateFormate(1, currentDate);

                list.clear();
                for (int i = 0; i < mainList.size(); i++) {

                    if (destinationDate.equals(mainList.get(i).getDate())) {
                        list.add(mainList.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
                hideRecyclerView();
                break;
            case Constants.FILTER_NEXT_WEEK:

                destinationDate = dateAndTimePicker.incrementDateInDateFormate(Constants.WEEK_DAYS, currentDate);
                destiDate = dateAndTimePicker.convertStringToDate(destinationDate);

                filterListByTwoDate(curDate, destiDate);
                break;
            case Constants.FILTER_NEXT_MONTH:

                destinationDate = dateAndTimePicker.incrementDateInDateFormate(Constants.MONTH_DAYS, currentDate);
                destiDate = dateAndTimePicker.convertStringToDate(destinationDate);

                filterListByTwoDate(curDate, destiDate);
                break;
            case Constants.FILTER_NEXT_YEAR:

                destinationDate = dateAndTimePicker.incrementDateInDateFormate(Constants.YEAR_DAYS, currentDate);
                destiDate = dateAndTimePicker.convertStringToDate(destinationDate);

                filterListByTwoDate(curDate, destiDate);
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterListByTwoDate(Date curDate, Date destiDate) {

        list.clear();
        for (int i = 0; i < mainList.size(); i++) {

            Date itemDate = dateAndTimePicker.convertStringToDate(mainList.get(i).getDate());
            int flag = itemDate.compareTo(curDate);
            int flag2 = itemDate.compareTo(destiDate);

            if (flag == 0 || flag2 == 0) {
                list.add(mainList.get(i));
            } else if (flag > 0 && flag2 < 0) {
                list.add(mainList.get(i));
            }
        }

        adapter.notifyDataSetChanged();
        hideRecyclerView();
    }

    private void setClickListenerForDateTime() {
        dateAndTimePicker.setOnDateSelectListener(date -> followUpDate = date);

        dateAndTimePicker.setOnTimeSelectListener(t -> {

            followUpTime = t;
            changeFollowUpDateAndTime();

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListFromLocal();
    }

    protected void setupRecyclerView() {

        //mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new TodoListAdapter(list, this);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(MainActivity.this, mRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, final int position) {

                currentItemPosition = position;
                isCheckedByCurrentDevice = true;

                LinearLayout lnrRoot = view.findViewById(R.id.lnr_root);
                ImageView imageViewRemove = view.findViewById(R.id.iv_remove);

                if (!list.get(position).isReminder()) {
                    popupDialogForNote(view, position);
                }

                imageViewRemove.setOnClickListener(v -> deleteConfirmationAlert(position));

                lnrRoot.setOnClickListener(v -> {

                    Intent i = new Intent(MainActivity.this, TodoDetailsActivity.class);
                    i.putExtra(KEY_ID, list.get(position).getId());
                    i.putExtra(KEY_FIRE_ID, list.get(position).getFireId());
                    i.putExtra(KEY_TITLE, list.get(position).getTitle());
                    i.putExtra(KEY_NOTE, list.get(position).getNote());
                    i.putExtra(KEY_DATE, list.get(position).getDate());
                    i.putExtra(KEY_TIME, list.get(position).getTime());
                    i.putExtra(KEY_REMIND_TIME, list.get(position).getRemindTime());
                    i.putExtra(KEY_REPEAT, list.get(position).getRepeat());
                    i.putExtra(KEY_REPEAT_DAY, list.get(position).getRepeatDay());
                    i.putExtra(KEY_REPEAT_DATE, list.get(position).getRepeatDate());
                    i.putExtra(KEY_PRIORITY, list.get(position).getPriority());
                    i.putExtra(KEY_IS_REMINDER, list.get(position).isReminder());

                    i.putExtra(KEY_FOLLOW_UP_DATE, list.get(position).getFollowUpDate());
                    i.putExtra(KEY_FOLLOW_UP_TIME, list.get(position).getFollowUpTime());
                    i.putExtra(KEY_CREATED_BY, list.get(position).getCreatedBy());
                    i.putExtra(KEY_STATUS, list.get(position).getStatus());
                    i.putExtra(KEY_CREATED_DATE, list.get(position).getCreatedDate());
                    i.putExtra(KEY_UPDATED_DATE, list.get(position).getUpdatedDate());
                    i.putExtra(KEY_VIEW_TYPE, list.get(position).getViewType());
                    i.putExtra(KEY_IS_DELIVERED, list.get(position).isDelivered());
                    i.putExtra(KEY_ASSIGNTO, list.get(position).getAssignTo());
                    i.putExtra(KEY_ASSIGN_ME_ALSO, list.get(position).isAssignMeAlso());
                    i.putExtra(KEY_COMPLETED_BY, list.get(position).getCompletedBy());
                    i.putExtra(Constants.KEY_IMAGES, list.get(position).getImages());

                    startActivity(i);

                });
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    private void logoutAlert() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.logout_msg))
                .setPositiveButton(R.string.yes, (dialog, which) -> {

                    AssignToActivity.contactList.clear();
                    AssignToActivity.selectedList.clear();
                    sessionManager.logoutUser();
                    ForegroundService.clearNotification(MainActivity.this);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss()).show();
    }

    private void deleteConfirmationAlert(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.delete_alert))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {


                    final String fireIdForDelete = list.get(position).getFireId();

                    if (ConnectionChecker.isInternetAvailable(MainActivity.this)) {

                        deleteTodo(fireIdForDelete, position, dialog);//delete from self list

                        if (!list.get(position).getAssignTo().equals("")) {//delete from assigned user's list
                            if (list.get(position).getCreatedBy().equals(sessionManager.getUserId())) {
                                String[] arrayUsers = getArrayFromString(list.get(position).getAssignTo());
                                for (String arrayUser : arrayUsers) {
                                    FirebaseRealtimeController.getInstance().deleteTodoByUserIdAndFireId(arrayUser, fireIdForDelete);
                                }
                            }
                        }

                    } else {

                        TodoModel todoModel = RealmController.getInstance().getTodoByFireId(list.get(position).getFireId());

                        todoModel.setPendingForSync(true);
                        todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_DELETE);
                        todoModel.setStatus(Constants.STATUS_FINISHED);

                        RealmController.getInstance().addTodoItem(todoModel);

                        dialog.dismiss();

                        tvTotalTodo.setText(String.valueOf(list.size()));//update total todos on appbar
                        refreshListFromLocal();
                    }


                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss()).show();
    }

    private void deleteTodo(String fireIdForDelete, final int position, final DialogInterface dialog) {

        FirebaseRealtimeController.getInstance().deleteTodoByFireId(fireIdForDelete).setOnCompleteListener(new OnCallCompleteListener() {
            @Override
            public void onComplete() {

                dialog.dismiss();

                tvTotalTodo.setText(String.valueOf(list.size()));//update total todos on appbar
                refreshListFromLocal();
            }

            @Override
            public void onFailed() {

                TodoModel todoModel = RealmController.getInstance().getTodoByFireId(list.get(position).getFireId());

                todoModel.setPendingForSync(true);
                todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_DELETE);
                todoModel.setStatus(Constants.STATUS_FINISHED);

                RealmController.getInstance().addTodoItem(todoModel);

                dialog.dismiss();
            }
        });
    }

    private void popupDialogForNote(View view, final int position) {
        final AppCompatCheckBox checkBox = view.findViewById(R.id.checkbox_done);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                DialogFragmentFinishTask dialogFinishTask = new DialogFragmentFinishTask("MainActivity");
                if (isCheckedByCurrentDevice) {
                    dialogFinishTask.show(getSupportFragmentManager(), "");
                    isCheckedByCurrentDevice = false;
                }
                dialogFinishTask.setOnItemClickedListener(new OnItemClickedListener() {
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

    private void popupAlert() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.alert_add_followup_date))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    dialog.dismiss();

                    runOnUiThread(() -> {
                        dateAndTimePicker = new DateAndTimePicker(MainActivity.this);
                        dateAndTimePicker.timePicker();
                        dateAndTimePicker.datePicker();
                    });
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
                this.doubleBackToExitPressedOnce = true;
            }

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void gotoResetPassword(String userId){

        Intent i = new Intent(this,ResetPasswordActivity.class);
        i.putExtra(Constants.KEY_USERID,userId);
        i.putExtra(Constants.KEY_PHONE_NUMBER, sessionManager.getPhoneNumber());
        i.putExtra(Constants.KEY_COME_FROM,Constants.SCREEN_SETTINGS);
        startActivity(i);

    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_new) {
            startActivity(new Intent(MainActivity.this, CreateNewTodoActivity.class));
        } else if (id == R.id.nav_change_password) {
            gotoResetPassword(sessionManager.getUserId());
        } else if (id == R.id.nav_task_list) {
            startActivity(new Intent(MainActivity.this, TaskListActivity.class));
        } else if (id == R.id.nav_my_contacts) {
            startActivity(new Intent(MainActivity.this, MyContactsActivityWithTabs.class));
        } else if (id == R.id.nav_change_snooze_time) {
            SnoozeDialogFragmentBottomSheet snoozeDialog = new SnoozeDialogFragmentBottomSheet(this);
            snoozeDialog.show(getSupportFragmentManager(), "");
            // snoozeDialog.setOnTimeSelectListener(this::setSnoozeValue);
        }
        else if(id == R.id.nav_change_notification_tone)
        {
            Intent i = new Intent(this, SelectRingtoneActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_logout) {
            logoutAlert();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setOnRealDataChangeListeners() {

        //this is for single value event listener
        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                TodoModel post = dataSnapshot.getValue(TodoModel.class);

                RealmController.getInstance().addTodoItem(post);

                refreshListFromLocal();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                TodoModel post = dataSnapshot.getValue(TodoModel.class);
                RealmController.getInstance().addTodoItem(post);

                refreshListFromLocal();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                TodoModel post = dataSnapshot.getValue(TodoModel.class);

                RealmController.getInstance().removeTodoByFireId(Objects.requireNonNull(post).getFireId());

                refreshListFromLocal();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void finishTaskForAssignedUsers(final TodoModel todoModel, String taskId) {
        final String[] arrayUsers = getArrayFromString(todoModel.getAssignTo());
        for (i = 0; i < arrayUsers.length; i++) {
            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, arrayUsers[i], taskId).setOnCompleteListener(new OnCallCompleteListener() {
                @Override
                public void onComplete() {
                    //after finish task from all assigned user's then refresh tasklist
                    if (i == arrayUsers.length - 1) {
                        refreshListFromLocal();
                    }
                }

                @Override
                public void onFailed() {
                }
            });
        }

    }

    private void notifyAssigedUserAboutTaskFinished(TodoModel todoModel) {

        String assignTo = todoModel.getAssignTo();
        String body = todoModel.getTitle();

        if (!assignTo.equals("")) {
            String[] arrayUsers = getArrayFromString(assignTo);
            for (String arrayUser : arrayUsers) {
                if (!arrayUser.equals(sessionManager.getUserId())) {
                    //extract number from here
                    networkConnectivity.pushNotificationForThisUser(arrayUser, body, getString(R.string.task_finished_title), Constants.FINISH_TASK);
                }
            }

            if (!todoModel.getCreatedBy().equals(sessionManager.getUserId())) {
                networkConnectivity.pushNotificationForThisUser(todoModel.getCreatedBy(), body, getString(R.string.task_finished_title), Constants.FINISH_TASK);
            }
        }
    }

    //========================Below code is for Tasks&reminders===========
    private void finishTask(int position, String note) {
        TodoModel todoManaged = RealmController.getInstance().getTodoByFireId(list.get(currentItemPosition).getFireId());
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
        todoModel.setStatus(Constants.STATUS_FINISHED);//udpated this field
        todoModel.setCompletedBy(sessionManager.getUserId());
        todoModel.setCreatedDate(todoManaged.getCreatedDate());
        todoModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTime());//updated here
        todoModel.setViewType(list.get(position).getViewType());
        todoModel.setDelivered(list.get(position).isDelivered());
        todoModel.setAssignTo(list.get(position).getAssignTo());
        todoModel.setAssignMeAlso(list.get(position).isAssignMeAlso());
        todoModel.setImages(list.get(position).getImages());

        if (ConnectionChecker.isInternetAvailable(MainActivity.this)) {
            todoModel.setPendingForSync(false);
            todoModel.setWhatPending("");

            String fireId = list.get(position).getFireId();
            String createdBy = list.get(position).getCreatedBy();

            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, createdBy, fireId);//finish task for self
            if (!todoManaged.getAssignTo().equals("")) {
                //finish task from assigned users
                finishTaskForAssignedUsers(todoModel, fireId);
                notifyAssigedUserAboutTaskFinished(todoModel);
            }

        } else {
            todoModel.setPendingForSync(true);
            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_UPDATE);

        }
        RealmController.getInstance().addTodoItem(todoModel);//added for update Realtime db in local also

        refreshListFromLocal();

    }

    private void changeFollowUpDateAndTime() {

        String fireId = list.get(currentItemPosition).getFireId();

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
        todoModel.setStatus(Constants.STATUS_ACTIVE);//updated this field
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

        if (ConnectionChecker.isInternetAvailable(MainActivity.this)) {
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

        String taskFireId = list.get(position).getFireId();
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

        if (ConnectionChecker.isInternetAvailable(MainActivity.this)) {
            commentModel.setPendingForSync(false);
            commentModel.setWhatPending("");

            FirebaseRealtimeController.getInstance().addOrUpdateCommentByTaskId(commentModel, taskFireId, cFireId).setOnCompleteListener(new OnCallCompleteListener() {
                @Override
                public void onComplete() {

                    String assignTo = list.get(position).getAssignTo();
                    String title = list.get(position).getTitle();

                    if (!assignTo.equals("")) {
                        String[] arrayUsers = getArrayFromString(assignTo);
                        for (String arrayUser : arrayUsers) {
                            if (!arrayUser.equals(sessionManager.getUserId())) {
                                networkConnectivity.pushNotificationForThisUser(arrayUser, comment, title, Constants.CREATE_NEW_COMMENT);
                            }
                        }

                        if (!list.get(position).getCreatedBy().equals(sessionManager.getUserId())) {
                            networkConnectivity.pushNotificationForThisUser(list.get(position).getCreatedBy(), comment, title, Constants.CREATE_NEW_COMMENT);
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
