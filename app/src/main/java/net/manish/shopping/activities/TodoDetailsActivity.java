package net.manish.shopping.activities;

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
import static net.manish.shopping.utils.Constants.PRIORITY_HIGH;
import static net.manish.shopping.utils.Constants.PRIORITY_LOW;
import static net.manish.shopping.utils.Constants.PRIORITY_MEDIUM;
import static net.manish.shopping.utils.Constants.STATUS_FINISHED;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bogdwellers.pinchtozoom.view.ImageViewPager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.manish.shopping.R;
import net.manish.shopping.adapter.CommentListAdapter;
import net.manish.shopping.adapter.ImageListAdapter;
import net.manish.shopping.adapter.ViewpagerAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.fragments.DialogFragmentFinishTask;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TodoDetailsActivity extends AppCompatActivity
{

    private final String TAG = TodoDetailsActivity.this.getClass().getName();
    private TextView tvTitle;
    private TextView tvNote;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvRepeat;
    private TextView tvAssignTo;
    private RelativeLayout rltvAddComment, rltvAssignToMeAlso;
    private RadioGroup radioGroupPriority, radioGroupType;
    private Button btnReopen;
    private CheckBox checkBoxAssignMe;
    private boolean assignToMe = true;

    private RecyclerView mRecyclerView;
    private CommentListAdapter adapter;
    private final List<CommentModel> commentList = new ArrayList<>();

    private RelativeLayout rltvImages;
    private RecyclerView recyclerViewImageList;
    private final List<String> imageURLList = new ArrayList<>();
    private ImageListAdapter imageAdapter;

    private ImageView ivClose;
    private ImageViewPager imageViewPager;
    private RelativeLayout rltvPreview;
    private boolean isPreviewRunning = false;

    private String taskFireId, title, note, date, time, remindTime, repeat, repeatDay, repeatDate, priority, followUpDate, followUpTime, createdBy, status, createdDate, updatedDate, assignTo, completedBy, images = "";
    private int id, viewType;
    private boolean isReminder, isDelivered;

    private DateAndTimePicker dateAndTimePicker;
    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_todo_details);

        dateAndTimePicker = new DateAndTimePicker(TodoDetailsActivity.this);
        sessionManager = new SessionManager(this);
        networkConnectivity = new NetworkConnectivity(this);

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        userId = sessionManager.getUserId();

        getIntentValues();
        findViews();
        initViewsWithValue();
        setupRecyclerView();
        setupRecyclerViewForImages();
        getComments();

        setFirebaseValueChangeListeners();
    }

    private void getIntentValues()
    {
        //get values from keys
        id = getIntent().getIntExtra(KEY_ID, 0);
        taskFireId = getIntent().getStringExtra(KEY_FIRE_ID);
        title = getIntent().getStringExtra(KEY_TITLE);
        note = getIntent().getStringExtra(KEY_NOTE);
        date = getIntent().getStringExtra(KEY_DATE);
        time = getIntent().getStringExtra(KEY_TIME);
        remindTime = getIntent().getStringExtra(KEY_REMIND_TIME);
        repeat = getIntent().getStringExtra(KEY_REPEAT);
        repeatDay = getIntent().getStringExtra(KEY_REPEAT_DAY);
        repeatDate = getIntent().getStringExtra(KEY_REPEAT_DATE);
        priority = getIntent().getStringExtra(KEY_PRIORITY);
        isReminder = getIntent().getBooleanExtra(KEY_IS_REMINDER, true);

        followUpDate = getIntent().getStringExtra(KEY_FOLLOW_UP_DATE);
        followUpTime = getIntent().getStringExtra(KEY_FOLLOW_UP_TIME);
        createdBy = getIntent().getStringExtra(KEY_CREATED_BY);
        status = getIntent().getStringExtra(KEY_STATUS);
        Mylogger.getInstance().printLog(TAG, "getIntentValues() status : " + status);
        createdDate = getIntent().getStringExtra(KEY_CREATED_DATE);
        updatedDate = getIntent().getStringExtra(KEY_UPDATED_DATE);
        viewType = getIntent().getIntExtra(KEY_VIEW_TYPE, 0);
        isDelivered = getIntent().getBooleanExtra(KEY_IS_DELIVERED, false);

        assignTo = getIntent().getStringExtra(KEY_ASSIGNTO);
        assignToMe = getIntent().getBooleanExtra(KEY_ASSIGN_ME_ALSO, true);
        completedBy = getIntent().getStringExtra(KEY_COMPLETED_BY);
        images = getIntent().getStringExtra(KEY_IMAGES);

        Mylogger.getInstance().printLog(TAG, "images : " + images);

    }

    private void findViews()
    {
        //setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (isReminder)
        {
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.reminder) + " " + getResources().getString(R.string.details));
        }
        else
        {
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.task) + " " + getResources().getString(R.string.details));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //find views reference
        tvTitle = findViewById(R.id.tv_title);
        tvNote = findViewById(R.id.tv_note);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        tvRepeat = findViewById(R.id.tv_repeat);
        tvAssignTo = findViewById(R.id.tv_assign_to);
        TextView tvAddComment = findViewById(R.id.tv_add_comment);
        radioGroupPriority = findViewById(R.id.radioGroup1);
        radioGroupType = findViewById(R.id.radioGroupType);
        mRecyclerView = findViewById(R.id.recycler_view);
        recyclerViewImageList = findViewById(R.id.recycler_view_images);
        rltvAddComment = findViewById(R.id.rltv_add_comment);
        btnReopen = findViewById(R.id.btn_reopen);
        checkBoxAssignMe = findViewById(R.id.checkbox_assign_me);
        rltvAssignToMeAlso = findViewById(R.id.rltv_assign_me_also);
        rltvImages = findViewById(R.id.rltv_images);

        ivClose = findViewById(R.id.iv_close);
        rltvPreview = findViewById(R.id.rltv_img_preview);
        imageViewPager = findViewById(R.id.view_pager);

        //set on click listeners
        ivClose.setOnClickListener(v -> closePreview());

        tvAddComment.setOnClickListener(v -> popupCommentDialog());

        btnReopen.setOnClickListener(v -> reOpenTodo());
    }


    private void closePreview()
    {
        rltvPreview.setVisibility(View.GONE);
        ivClose.setClickable(false);
        isPreviewRunning = false;
    }

    private void openPreview(int position)
    {

        rltvPreview.setVisibility(View.VISIBLE);
        ivClose.setClickable(true);
        isPreviewRunning = true;

        imageViewPager.setCurrentItem(position);
    }

    private void setFirebaseValueChangeListeners()
    {
        myDbRef.child(Constants.TABLE_COMMENTS).child(taskFireId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                Mylogger.getInstance().printLog(TAG, "onChildAdded()");
                CommentModel newComment = dataSnapshot.getValue(CommentModel.class);
                if (newComment != null)
                {
                    addOrUpdateComment(newComment);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                Mylogger.getInstance().printLog(TAG, "onChildChanged()");
                CommentModel newComment = dataSnapshot.getValue(CommentModel.class);
                if (newComment != null)
                {
                    addOrUpdateComment(newComment);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

                CommentModel newComment = dataSnapshot.getValue(CommentModel.class);
                if (newComment != null)
                {
                    RealmController.getInstance().removeCommentByCFireId(newComment.getcFireId());
                }
                getComments();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void addOrUpdateComment(CommentModel newComment)
    {

        CommentModel commentModel = new CommentModel();
        commentModel.setId(newComment.getId());
        commentModel.setcFireId(newComment.getcFireId());
        commentModel.setTaskId(newComment.getTaskId());
        commentModel.setUserId(newComment.getUserId());
        commentModel.setPhone(newComment.getPhone());
        commentModel.setSenderName(newComment.getSenderName());
        commentModel.setComment(newComment.getComment());
        commentModel.setCreatedDate(newComment.getCreatedDate());
        commentModel.setUpdatedDate(newComment.getUpdatedDate());
        commentModel.setPendingForSync(newComment.isPendingForSync());
        commentModel.setWhatPending(newComment.getWhatPending());

        RealmController.getInstance().addComment(commentModel);

        getComments();
    }

    protected void setupRecyclerView()
    {

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentListAdapter(commentList, this);
        mRecyclerView.setAdapter(adapter);

        //setup item click listener
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(TodoDetailsActivity.this, mRecyclerView, new RecyclerViewClickListener()
        {
            @Override
            public void onClick(View view, final int position)
            {

            }

            @Override
            public void onLongClick(View view, int position)
            {
            }
        }));
    }

    protected void setupRecyclerViewForImages()
    {

        recyclerViewImageList.setHasFixedSize(true);
        recyclerViewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        imageAdapter = new ImageListAdapter(imageURLList, this, Constants.SCREEN_TODO_DETAILS_ACTIVITY);
        recyclerViewImageList.setAdapter(imageAdapter);

        recyclerViewImageList.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerViewImageList, new RecyclerViewClickListener()
        {
            @Override
            public void onClick(View view, final int position)
            {

                openPreview(position);
            }

            @Override
            public void onLongClick(View view, int position)
            {

            }
        }));

    }

    private void reOpenTodo()
    {

        TodoModel todoManaged = RealmController.getInstance().getTodoByFireId(taskFireId);
        TodoModel updatedTodoModel = new TodoModel();

        updatedTodoModel.setId(todoManaged.getId());
        updatedTodoModel.setFireId(todoManaged.getFireId());
        updatedTodoModel.setTitle(todoManaged.getTitle());
        updatedTodoModel.setNote(note);//new added update
        updatedTodoModel.setDate(todoManaged.getDate());
        updatedTodoModel.setTime(todoManaged.getTime());
        updatedTodoModel.setRemindTime(todoManaged.getRemindTime());
        updatedTodoModel.setRepeat(todoManaged.getRepeat());
        updatedTodoModel.setRepeatDay(todoManaged.getRepeatDay());
        updatedTodoModel.setRepeatDate(todoManaged.getRepeatDate());
        updatedTodoModel.setPriority(todoManaged.getPriority());
        updatedTodoModel.setReminder(todoManaged.isReminder());

        updatedTodoModel.setFollowUpDate(followUpDate);
        updatedTodoModel.setFollowUpTime(followUpTime);
        updatedTodoModel.setCreatedBy(todoManaged.getCreatedBy());
        updatedTodoModel.setStatus(Constants.STATUS_ACTIVE);//updated this field
        updatedTodoModel.setCreatedDate(todoManaged.getCreatedDate());
        updatedTodoModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTime());//updated here
        updatedTodoModel.setViewType(viewType);
        updatedTodoModel.setDelivered(isDelivered);
        updatedTodoModel.setAssignTo(assignTo);
        updatedTodoModel.setAssignMeAlso(assignToMe);
        updatedTodoModel.setImages(images);

        if (ConnectionChecker.isInternetAvailable(TodoDetailsActivity.this))
        {
            updatedTodoModel.setPendingForSync(todoManaged.isPendingForSync());
            updatedTodoModel.setWhatPending(todoManaged.getWhatPending());

            FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(updatedTodoModel, userId, todoManaged.getFireId());//This is for own who has clicked on reOpen button

            makeReOpenForAssignedPersonsAlso(updatedTodoModel, todoManaged);

        }
        else
        {
            updatedTodoModel.setPendingForSync(true);
            updatedTodoModel.setWhatPending(Constants.SYNC_PENDING_FOR_UPDATE);

        }
        RealmController.getInstance().addTodoItem(updatedTodoModel);//update realtime database locally

        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).child(taskFireId).setValue(updatedTodoModel);
        btnReopen.setVisibility(View.GONE);
        hideEditOptionInToolbar(false);
    }

    private void makeReOpenForAssignedPersonsAlso(TodoModel todoModel, TodoModel todoManaged)
    {
        //This method is used to change todoItem in firebase_realtime db for assigned users
        String assignTo = todoModel.getAssignTo();

        if (!assignTo.equals(""))
        {
            String[] arrayUsers = getArrayFromString(assignTo);
            for (String arrayUser : arrayUsers)
            {
                if (!arrayUser.equals(sessionManager.getUserId()))
                {
                    //changed todoModel from here for assigned persons
                    FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, arrayUser, todoManaged.getFireId());//This is for assigned users
                }
            }
        }
    }

    private void popupCommentDialog()
    {
        DialogFragmentFinishTask dialgFinishTask = new DialogFragmentFinishTask(
                "TodoDetailsActivity");
        dialgFinishTask.show(getSupportFragmentManager(), "");
        dialgFinishTask.setOnItemClickedListener(new OnItemClickedListener()
        {
            @Override
            public void onItemClicked(List<ContactModel> selectedContacts)
            {

            }

            @Override
            public void onTaskFinished(boolean isFinished, String note)
            {

            }

            @Override
            public void onAddComment(final String comment)
            {

                int id = getNextId();
                String cFireId = myDbRef.child(Constants.TABLE_COMMENTS).child(taskFireId).push().getKey();

                CommentModel commentModel = new CommentModel();
                commentModel.setId(id);
                commentModel.setcFireId(cFireId);
                commentModel.setTaskId(taskFireId);
                commentModel.setUserId(sessionManager.getUserId());
                commentModel.setComment(comment);
                commentModel.setPhone(sessionManager.getPhoneNumber());
                commentModel.setSenderName(sessionManager.getUserName());
                commentModel.setCreatedDate(dateAndTimePicker.getCurrentDateTime());
                commentModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTimeWithSecond());

                if (ConnectionChecker.isInternetAvailable(TodoDetailsActivity.this))
                {
                    commentModel.setPendingForSync(false);
                    commentModel.setWhatPending("");

                    FirebaseRealtimeController.getInstance().addOrUpdateCommentByTaskId(commentModel, taskFireId, cFireId).setOnCompleteListener(new OnCallCompleteListener()
                    {
                        @Override
                        public void onComplete()
                        {
                            if (!assignTo.equals(""))
                            {
                                String[] arrayUsers = getArrayFromString(assignTo);
                                for (String arrayUser : arrayUsers)
                                {
                                    if (!arrayUser.equals(sessionManager.getUserId()))
                                    {
                                        sendNotification(arrayUser, comment);
                                    }
                                }

                                if (!createdBy.equals(sessionManager.getUserId()))
                                {
                                    sendNotification(createdBy, comment);
                                }
                            }
                        }

                        @Override
                        public void onFailed()
                        {

                        }
                    });
                }
                else
                {
                    commentModel.setPendingForSync(true);
                    commentModel.setWhatPending(Constants.SYNC_PENDING_FOR_INSERT);

                    RealmController.getInstance().addComment(commentModel);
                }

                //update comment list
                getComments();

            }
        });
    }

    private void sendNotification(String userId, final String comment)
    {
        networkConnectivity.pushNotificationForThisUser(userId, comment, title, Constants.CREATE_NEW_COMMENT);
    }

    public int getNextId()
    {
        try
        {
            Number number = RealmController.getInstance().getRealm().where(CommentModel.class).max("id");
            if (number != null)
            {
                return number.intValue() + 1;
            }
            else
            {
                return 0;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return 0;
        }
    }

    private void hideViews()
    {
        if (isReminder)
        {
            radioGroupPriority.setVisibility(View.GONE);
            rltvAddComment.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            btnReopen.setVisibility(View.GONE);

            //disable appbar scrolling
            setAppBarDragging();
        }
        else
        {
            radioGroupPriority.setVisibility(View.VISIBLE);
            rltvAddComment.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        if (sessionManager.getUserId().equals(createdBy))
        {
            if (status.equals(Constants.STATUS_ACTIVE))
            {
                btnReopen.setVisibility(View.GONE);
            }
            else
            {
                btnReopen.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            btnReopen.setVisibility(View.GONE);
        }

        if (imageURLList.size() == 0)
        {
            rltvImages.setVisibility(View.GONE);
        }
        else
        {
            rltvImages.setVisibility(View.VISIBLE);
        }
    }

    private void setAppBarDragging()
    {
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback()
        {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout)
            {
                return false;
            }
        });
        params.setBehavior(behavior);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewsWithValue()
    {

        if (!images.isEmpty())
        {
            String[] imageArray = images.split(",");
            imageURLList.clear();
            imageURLList.addAll(Arrays.asList(imageArray));
        }
        else
        {
            imageURLList.clear();
        }

        if (imageAdapter != null)
        {
            imageAdapter.notifyDataSetChanged();
        }

        tvTitle.setText(title);
        tvNote.setText(note);
        tvDate.setText(date);
        tvTime.setText(time);
        tvRepeat.setText(repeat);

        if (assignTo.equals(""))
        {
            rltvAssignToMeAlso.setVisibility(View.GONE);
        }
        else
        {
            checkBoxAssignMe.setChecked(assignToMe);
            checkBoxAssignMe.setClickable(false);
        }

        //manage assign to row
        String[] arrayNames = getArrayFromString(assignTo);
        if (arrayNames.length == 0)
        {
            tvAssignTo.setText(getString(R.string.none));
        }
        else if (arrayNames.length == 1)
        {
            setNameByFirebaseUserId(arrayNames[0], tvAssignTo);
        }
        else
        {
            tvAssignTo.setText(getString(R.string.other));
        }

        //set Priority like high,medium,low
        switch (priority)
        {
            case PRIORITY_HIGH:
                radioGroupPriority.check(R.id.radio_high);
                break;
            case PRIORITY_MEDIUM:
                radioGroupPriority.check(R.id.radio_medium);
                break;
            case PRIORITY_LOW:
                radioGroupPriority.check(R.id.radio_low);
                break;
        }

        //set item type like reminder or task
        if (isReminder)
        {
            radioGroupType.check(R.id.radio_reminder);
        }
        else
        {
            radioGroupType.check(R.id.radio_task);
        }

        //set image list view pager
        imageViewPager.setAdapter(new ViewpagerAdapter(this, imageURLList));

        //hide view based on item type
        hideViews();
    }

    private String[] getArrayFromString(String assignTo)
    {

        try
        {
            JSONArray jsonArray = new JSONArray(assignTo);
            String[] strArr = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++)
            {

                strArr[i] = jsonArray.getString(i);
            }

            return strArr;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return new String[0];
    }

    public void setNameByFirebaseUserId(String userId, final TextView textView)
    {

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null && userModel.getUserId() != null)
                {
                    //registered
                    String name = isContactActiveInCurrentMobile(userModel.getPhone());
                    if (name.equals(""))
                    {
                        textView.setText(userModel.getName());
                    }
                    else
                    {
                        textView.setText(name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private String isContactActiveInCurrentMobile(String phone)
    {

        List<ContactModel> contactList = RealmController.getInstance().getAllRegisteredContacts();
        for (int i = 0; i < contactList.size(); i++)
        {
            if (phone.equals(Objects.requireNonNull(contactList.get(i)).getNumber()))
            {
                return Objects.requireNonNull(contactList.get(i)).getName();
            }
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo_details, menu);
        this.menu = menu;

        if (status.equals(STATUS_FINISHED))
        {
            hideEditOptionInToolbar(true);
        }
        return true;
    }

    private void hideEditOptionInToolbar(boolean b)
    {
        MenuItem item = menu.findItem(R.id.action_edit);
        item.setVisible(!b);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit)
        {
            Intent i = new Intent(TodoDetailsActivity.this, EditTodoActivity.class);
            i.putExtra(KEY_ID, this.id);
            i.putExtra(KEY_TITLE, title);
            i.putExtra(KEY_FIRE_ID, taskFireId);
            i.putExtra(KEY_NOTE, note);
            i.putExtra(KEY_DATE, date);
            i.putExtra(KEY_TIME, time);
            i.putExtra(KEY_REMIND_TIME, remindTime);
            i.putExtra(KEY_REPEAT, repeat);
            i.putExtra(Constants.KEY_REPEAT_DAY, repeatDay);
            i.putExtra(Constants.KEY_REPEAT_DATE, repeatDate);
            i.putExtra(KEY_PRIORITY, priority);
            i.putExtra(Constants.KEY_IS_REMINDER, isReminder);

            i.putExtra(KEY_FOLLOW_UP_DATE, followUpDate);
            i.putExtra(KEY_FOLLOW_UP_TIME, followUpTime);
            i.putExtra(KEY_CREATED_BY, createdBy);
            i.putExtra(KEY_STATUS, status);
            i.putExtra(KEY_CREATED_DATE, createdDate);
            i.putExtra(KEY_UPDATED_DATE, updatedDate);
            i.putExtra(KEY_VIEW_TYPE, viewType);
            i.putExtra(KEY_IS_DELIVERED, isDelivered);
            i.putExtra(KEY_ASSIGNTO, assignTo);
            i.putExtra(KEY_ASSIGNTO, assignTo);
            i.putExtra(KEY_COMPLETED_BY, completedBy);
            i.putExtra(KEY_IMAGES, images);
            //noinspection deprecation
            startActivityForResult(i, 100);
        }
        else if (id == android.R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void getComments()
    {

        commentList.clear();
        commentList.addAll(RealmController.getInstance().getAllCommentsByTaskId(taskFireId));

        sortCommentList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortCommentList()
    {

        if (commentList.size() != 0)
        {

            commentList.sort((o1, o2) -> dateAndTimePicker.convertStringToDateTimeSecond(o2.getUpdatedDate())
                    .compareTo(dateAndTimePicker.convertStringToDateTimeSecond(o1.getUpdatedDate())));
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (isPreviewRunning)
        {
            closePreview();
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100)
        {
            if (resultCode == RESULT_OK)
            {

                //get values from keys
                id = data.getIntExtra(KEY_ID, 0);
                taskFireId = data.getStringExtra(KEY_FIRE_ID);
                title = data.getStringExtra(KEY_TITLE);
                note = data.getStringExtra(KEY_NOTE);
                date = data.getStringExtra(KEY_DATE);
                time = data.getStringExtra(KEY_TIME);
                remindTime = data.getStringExtra(KEY_REMIND_TIME);
                repeat = data.getStringExtra(KEY_REPEAT);
                repeatDay = data.getStringExtra(KEY_REPEAT_DAY);
                repeatDate = data.getStringExtra(KEY_REPEAT_DATE);
                priority = data.getStringExtra(KEY_PRIORITY);
                isReminder = data.getBooleanExtra(KEY_IS_REMINDER, true);

                followUpDate = data.getStringExtra(KEY_FOLLOW_UP_DATE);
                followUpTime = data.getStringExtra(KEY_FOLLOW_UP_TIME);
                createdBy = data.getStringExtra(KEY_CREATED_BY);
                status = data.getStringExtra(KEY_STATUS);
                createdDate = data.getStringExtra(KEY_CREATED_DATE);
                updatedDate = data.getStringExtra(KEY_UPDATED_DATE);
                viewType = data.getIntExtra(KEY_VIEW_TYPE, 0);
                isDelivered = data.getBooleanExtra(KEY_IS_DELIVERED, false);
                assignTo = data.getStringExtra(KEY_ASSIGNTO);
                images = data.getStringExtra(KEY_IMAGES);

                Mylogger.getInstance().printLog(TAG, "onActivityResult() images : " + images);
                initViewsWithValue();//update view's values
            }
        }
    }

}
