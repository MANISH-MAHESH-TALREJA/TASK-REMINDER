package net.manish.shopping.activities;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static net.manish.shopping.utils.Constants.EVERYDAY;
import static net.manish.shopping.utils.Constants.FRIDAY;
import static net.manish.shopping.utils.Constants.LANG_ENGLISH;
import static net.manish.shopping.utils.Constants.LANG_GUJARATI;
import static net.manish.shopping.utils.Constants.LANG_HINDI;
import static net.manish.shopping.utils.Constants.MONDAY;
import static net.manish.shopping.utils.Constants.MONTHLY;
import static net.manish.shopping.utils.Constants.ONCE;
import static net.manish.shopping.utils.Constants.SATURDAY;
import static net.manish.shopping.utils.Constants.SUNDAY;
import static net.manish.shopping.utils.Constants.THURSDAY;
import static net.manish.shopping.utils.Constants.TUESDAY;
import static net.manish.shopping.utils.Constants.WEDNESDAY;
import static net.manish.shopping.utils.Constants.WEEKLY;
import static net.manish.shopping.utils.Constants.YEARLY;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import net.manish.shopping.R;
import net.manish.shopping.adapter.ImageListAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.AskPermition;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateNewTodoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDate;
    private TextView tvTime;
    private TextView tvRepeat;
    private TextView tvLanguage;
    private TextView tvRepeatDay;
    private TextView tvRepeatDateTime;
    private TextView tvAssign;
    private EditText etTitle, etNote;
    private DateAndTimePicker dateAndTimePicker;
    private RelativeLayout rltvRepeatDay;
    private RelativeLayout rltvRepeatDateTime;
    private RelativeLayout rltvAssignMeAlso;
    private RadioGroup radioGroupPriority, radioGroupType;
    private CheckBox checkBoxAssignMe;
    private RecyclerView recyclerViewImageList;
    private boolean assignToMe = true;
    private String speakFor = ""; //speak for = title or note

    private final int REQ_CODE_SPEECH_INPUT = 100;
    public String title = "", note = "", date = "", time = "", repeat = Constants.ONCE, priority = "", speechLangCode = "", images = "";
    private boolean isReminder = true;
    private boolean isInserted = true;

    private final List<String> langList = new ArrayList<>();
    private final List<String> supportedLanguages = new ArrayList<>();
    private String repeatDay = "", repeatDate = "", assignToIds = "";
    private boolean isForRepeatPurpose = false;

    private final List<ContactModel> selectedContactList = new ArrayList<>();

    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    //for upload image
    private final List<String> imageUriList = new ArrayList<>();
    private ImageListAdapter adapter;
    private final int GALLERY = 1;
    private final int CAMERA = 2;
    private Uri imgURI;
    private ProgressDialog progressDialog;

    private DatabaseReference myDbRef;
    private String userId; //for authentication
    private StorageReference mStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_todo);

        RealmController.with().refresh();

        networkConnectivity = new NetworkConnectivity(this);
        dateAndTimePicker = new DateAndTimePicker(CreateNewTodoActivity.this);
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        //init firebase
        mStorage = FirebaseStorage.getInstance().getReference();
        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        userId = sessionManager.getUserId();

        addAllLanguagesYouWant();

        setDateAndTimeListeners();

        findViews();
        setupRecyclerView();
        initImageList();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void initImageList() {
        imageUriList.clear();
        imageUriList.add(null);
        adapter.notifyDataSetChanged();
    }

    private void setDateAndTimeListeners() {

        dateAndTimePicker.setOnDateSelectListener(day -> {
            if (!isForRepeatPurpose) {
                date = day;
                tvDate.setText(day);
            } else {
                repeatDate = day;
                tvRepeatDateTime.setText(repeatDate);
            }
        });

        dateAndTimePicker.setOnTimeSelectListener(t -> {

            if (isSelectedTimeCorrect(t)) {

                if (!isForRepeatPurpose) {
                    time = t;
                    tvTime.setText(t);
                } else {
                    repeatDate = repeatDate + " " + t;
                    tvRepeatDateTime.setText(repeatDate);
                    isForRepeatPurpose = false;
                }
            }
        });

    }

    private boolean isSelectedTimeCorrect(String t) {

        if (dateAndTimePicker.isSelectedTimeSmallerThanCurrentTime(t, dateAndTimePicker.getCurrentTime())) {
            if (date.equals(dateAndTimePicker.getCurrentDate())) {
                Toast.makeText(this, getString(R.string.selected_time_is_smaller), Toast.LENGTH_SHORT).show();
                return false;//selected time is smaller then current time for today
            } else {
                return true;//selected time is smaller then current time but it is for not today.
            }

        } else {
            return true;
        }
    }

    private void addAllLanguagesYouWant() {
        langList.clear();
        langList.add(LANG_GUJARATI);
        langList.add(LANG_HINDI);
        langList.add(LANG_ENGLISH);

    }

    private void findViews() {

        progressDialog = new ProgressDialog(this);

        //setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.create_new_todo));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //find views reference
        Button btnCreate = findViewById(R.id.btn_create_todo);
        tvDate = findViewById(R.id.tv_date);
        tvRepeatDateTime = findViewById(R.id.tv_repeat_date);
        tvTime = findViewById(R.id.tv_time);
        tvRepeat = findViewById(R.id.tv_repeat);
        tvRepeatDay = findViewById(R.id.tv_day);
        TextView tvChangeInputLang = findViewById(R.id.tv_change_input_language);
        tvLanguage = findViewById(R.id.tv_language);
        tvAssign = findViewById(R.id.tv_assign_to);
        RelativeLayout rltvRepeat = findViewById(R.id.rltv_repeat);
        rltvRepeatDay = findViewById(R.id.rltv_select_day);
        rltvRepeatDateTime = findViewById(R.id.rltv_select_date);
        RelativeLayout rltvAssignTo = findViewById(R.id.rltv_assign_to);
        etTitle = findViewById(R.id.et_title);
        etNote = findViewById(R.id.et_note);
        ImageView ivSpeakTitle = findViewById(R.id.iv_speak_title);
        ImageView ivSpeakNote = findViewById(R.id.iv_speak_note);
        radioGroupPriority = findViewById(R.id.radioGroup1);
        radioGroupType = findViewById(R.id.radioGroupType);
        checkBoxAssignMe = findViewById(R.id.checkbox_assign_me);
        rltvAssignMeAlso = findViewById(R.id.rltv_assign_me_also);
        recyclerViewImageList = findViewById(R.id.recycler_view);

        //init views
        date = dateAndTimePicker.getCurrentDate();
        tvDate.setText(date);

        //click listener
        tvDate.setOnClickListener(this);
        tvTime.setOnClickListener(this);
        rltvRepeat.setOnClickListener(this);
        rltvRepeatDay.setOnClickListener(this);
        ivSpeakTitle.setOnClickListener(this);
        ivSpeakNote.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        tvChangeInputLang.setOnClickListener(this);
        rltvAssignTo.setOnClickListener(this);
        rltvRepeatDateTime.setOnClickListener(this);

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {

            AppCompatRadioButton radioType = group.findViewById(checkedId);
            if (radioType.getText().toString().equals(Constants.REMINDER)) {
                radioGroupPriority.setVisibility(View.GONE);
            } else {
                radioGroupPriority.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupRecyclerView() {

        recyclerViewImageList.setHasFixedSize(true);
        recyclerViewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new ImageListAdapter(imageUriList, this, Constants.SCREEN_CREATE_NEW_TODO_ACTIVITY);
        recyclerViewImageList.setAdapter(adapter);

        recyclerViewImageList.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerViewImageList, new RecyclerViewClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view, final int position) {

                if (imageUriList.get(position) != null) {
                    ImageView ivRemoveImage = view.findViewById(R.id.iv_remove_image);

                    ivRemoveImage.setOnClickListener(v -> runOnUiThread(() -> {
                        imageUriList.remove(position);
                        adapter.notifyDataSetChanged();
                    }));
                } else {
                    CircleImageView ivAddImage = view.findViewById(R.id.iv_add_image);
                    ivAddImage.setOnClickListener(v -> {
                        if (AskPermition.getInstance(CreateNewTodoActivity.this).isPermitted()) {
                            hideKeyboard();
                            showPictureDialog();
                        }
                    });
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public void onClick(View v) {

        dateAndTimePicker = new DateAndTimePicker(CreateNewTodoActivity.this);

        int id = v.getId();
        if (id == R.id.tv_date) {
            hideKeyboard();
            isForRepeatPurpose = false;
            dateAndTimePicker.datePicker();
        } else if (id == R.id.tv_time) {
            hideKeyboard();
            isForRepeatPurpose = false;
            dateAndTimePicker.timePicker();
        } else if (id == R.id.rltv_repeat) {
            showRepeatMenus();
        } else if (id == R.id.iv_speak_title) {
            speakFor = Constants.TITLE;
            promptSpeechInput();
        } else if (id == R.id.iv_speak_note) {
            speakFor = Constants.NOTE;
            promptSpeechInput();
        } else if (id == R.id.btn_create_todo) {//get all values from all views
            getAllValuesFromViews();


            if (isValidInputs()) {
                if (imageUriList.size() > 0) {
                    imageUriList.remove(imageUriList.size() - 1);
                    adapter.notifyDataSetChanged();
                }
                if (imageUriList.size() > 0) {
                    uploadImages();
                } else {
                    saveTodoInFirebase();
                }
            }
        } else if (id == R.id.tv_change_input_language) {
            popupLanguages();
        } else if (id == R.id.rltv_select_day) {
            showRepeatDayMenus();
        } else if (id == R.id.rltv_assign_to) {
            showContactListActivity();
        } else if (id == R.id.rltv_select_date) {
            isForRepeatPurpose = true;
            dateAndTimePicker.datePicker();
        }
    }

    private void showContactListActivity() {

        AssignToActivity.selectedList.clear();
        AssignToActivity.selectedList.addAll(selectedContactList);

        Intent i = new Intent(this, AssignToActivity.class);
        startActivity(i);

        AssignToActivity.setOnItemClickedListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(List<ContactModel> selectedContacts) {

                selectedContactList.clear();
                selectedContactList.addAll(selectedContacts);

                if (selectedContactList.size() == 0) {
                    tvAssign.setText(getResources().getString(R.string.none));
                    rltvAssignMeAlso.setVisibility(View.GONE);
                    assignToMe = true;
                } else if (selectedContactList.size() == 1) {
                    tvAssign.setText(selectedContactList.get(0).getName());
                    rltvAssignMeAlso.setVisibility(View.VISIBLE);
                } else {
                    tvAssign.setText(getString(R.string.other));
                    rltvAssignMeAlso.setVisibility(View.VISIBLE);
                }

                int count = selectedContactList.size();
                String[] numberArray = new String[count];

                for (int i = 0; i < count; i++) {
                    numberArray[i] = selectedContactList.get(i).getUserId();
                }
                assignToIds = Arrays.toString(numberArray);
            }

            @Override
            public void onTaskFinished(boolean isFinished, String note) {

            }

            @Override
            public void onAddComment(String comment) {

            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidInputs() {

        if (title.trim().length() == 0) {
            etTitle.setError(getResources().getString(R.string.error_enter_title));
            return false;
        }
        if (date.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.error_provide_date), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.error_provide_time), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isSelectedTimeCorrect(time)) {
            Toast.makeText(this, getString(R.string.selected_time_is_smaller), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (repeat.equals(Constants.WEEKLY)) {
            if (repeatDay.equals("")) {
                Toast.makeText(this, getResources().getString(R.string.error_provide_repeat_day), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (repeat.equals(Constants.MONTHLY) || repeat.equals(Constants.YEARLY)) {
            if (repeatDate.equals("")) {
                Toast.makeText(this, getResources().getString(R.string.error_provide_repeat_date), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public int getNextId() {
        try {
            Number number = RealmController.getInstance().getRealm().where(TodoModel.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    private void getAllValuesFromViews() {

        //get Values from Edit text
        title = etTitle.getText().toString();
        note = etNote.getText().toString();
        assignToMe = checkBoxAssignMe.isChecked();

        //get Radio button's values
        int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
        int selectedTypeId = radioGroupType.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        AppCompatRadioButton radioButtonPriority = findViewById(selectedPriorityId);
        priority = (String) radioButtonPriority.getText();

        AppCompatRadioButton radioButtonType = findViewById(selectedTypeId);
        isReminder = radioButtonType.getText().toString().equals(Constants.REMINDER);
    }

    private void popupLanguages() {

        supportedLanguages.clear();
        getSupportedLanguages();

        final String[] arrayLangs = new String[supportedLanguages.size()];
        for (int i = 0; i < supportedLanguages.size(); i++) {
            arrayLangs[i] = supportedLanguages.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_language));
        builder.setItems(arrayLangs, (dialog, which) -> {
            // the user clicked on colors[which]
            tvLanguage.setText(arrayLangs[which]);

            switch (arrayLangs[which]) {
                case LANG_GUJARATI:
                    speechLangCode = Constants.LANG_CODE_GUJARATI;
                    break;
                case LANG_HINDI:
                    speechLangCode = Constants.LANG_CODE_HINDI;
                    break;
                case LANG_ENGLISH:
                    speechLangCode = Constants.LANG_CODE_ENGLISH;
                    break;
            }

        });
        builder.show();
    }

    private void showRepeatMenus() {
        final String[] repeatArray = {ONCE, EVERYDAY, WEEKLY, MONTHLY, YEARLY};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.set_repeat));
        builder.setItems(repeatArray, (dialog, which) -> {
            // the user clicked on colors[which]
            repeat = repeatArray[which];
            tvRepeat.setText(repeat);

            switch (repeatArray[which]) {
                case ONCE:
                case EVERYDAY:
                    rltvRepeatDay.setVisibility(View.GONE);
                    break;
                case WEEKLY:
                    showRepeatDayMenus();
                    rltvRepeatDay.setVisibility(View.VISIBLE);
                    rltvRepeatDateTime.setVisibility(View.GONE);
                    break;
                case MONTHLY:
                case YEARLY:
                    //display date picker text view
                    rltvRepeatDateTime.setVisibility(View.VISIBLE);
                    rltvRepeatDay.setVisibility(View.GONE);
                    break;

            }
        });
        builder.show();
    }

    private void showRepeatDayMenus() {
        final String[] repeatDayArray = {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.set_repeat_day));
        builder.setItems(repeatDayArray, (dialog, which) -> {
            // the user clicked on colors[which]
            repeatDay = repeatDayArray[which];
            tvRepeatDay.setText(repeatDay);
        });
        builder.show();
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLangCode); //(Hindi=hi,Gujarati=gu-IN,English=en)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            //noinspection deprecation
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getSupportedLanguages() {

        for (String lang : langList) {
            if (isLanSupported(lang)) {
                supportedLanguages.add(lang);
            }
        }
    }

    private boolean isLanSupported(String text) {

        int w = 200, h = 80;
        Resources resources = getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(w, h, conf);
        Bitmap orig = bitmap.copy(conf, false);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setTextSize((int) (14 * scale));

        // draw text to the Canvas
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);
        boolean res = !orig.sameAs(bitmap);
        orig.recycle();
        bitmap.recycle();
        return res;
    }

    private void uploadImages() {

        progressDialog.setMessage(getString(R.string.pls_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        int index;
        for (index = 0; index < imageUriList.size(); index++) {

            Uri uri = Uri.parse(imageUriList.get(index));
            StorageReference filePath = mStorage.child(Constants.STORAGE_FOLDAR_IMAGES).child(Objects.requireNonNull(uri.getLastPathSegment()));

            uploadToFirebase(filePath, index, uri);
        }
    }

    private void uploadToFirebase(final StorageReference filePath, final int index, Uri uri) {
        //Below is the new code to upload file on firebase storage.
        UploadTask uploadTask = filePath.putFile(uri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }

            // Continue with the task to get the download URL
            return filePath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();

                images = images + downloadUri.toString();
                if (imageUriList.size() > index + 1) {
                    images = images + ",";
                }

                if (index == imageUriList.size() - 1) {
                    //this is last image uploaded
                    saveTodoInFirebase();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void saveTodoInFirebase() {

        int viewType;
        if (isReminder) {
            viewType = Constants.ITEM_TYPE_REMINDER;
        } else {
            viewType = Constants.ITEM_TYPE_TASK;
        }

        String fireId = myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).push().getKey();
        TodoModel todoModel = new TodoModel();

        todoModel.setId(getNextId());
        todoModel.setFireId(fireId);
        todoModel.setTitle(title);
        todoModel.setNote(note);
        todoModel.setDate(date);
        todoModel.setTime(time);
        todoModel.setRemindTime(date + " " + time);
        todoModel.setRepeat(repeat);
        todoModel.setRepeatDay(repeatDay);
        todoModel.setRepeatDate(repeatDate);
        todoModel.setReminder(isReminder);
        todoModel.setPriority(priority);
        todoModel.setFollowUpDate("");
        todoModel.setFollowUpTime("");
        todoModel.setCreatedBy(sessionManager.getUserId());
        todoModel.setStatus(Constants.STATUS_ACTIVE);
        todoModel.setCompletedBy("");
        todoModel.setCreatedDate(dateAndTimePicker.getCurrentDateTime());
        todoModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTime());
        todoModel.setViewType(viewType);
        todoModel.setDelivered(false);
        todoModel.setAssignMeAlso(assignToMe);
        todoModel.setImages(images);

        if (ConnectionChecker.isInternetAvailable(this)) {
            todoModel.setPendingForSync(false);
            todoModel.setWhatPending("");

            if (selectedContactList.size() > 0) {
                for (int i = 0; i < selectedContactList.size(); i++) {
                    todoModel.setAssignTo(assignToIds);
                    checkIsNumberBlockedOrNot(todoModel, selectedContactList.get(i).getUserId(), fireId);
                }
            }
            if (assignToMe) {
                if (assignToIds.equals("[]")) {
                    todoModel.setAssignTo("");
                } else {
                    todoModel.setAssignTo(assignToIds);
                }

                FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, sessionManager.getUserId(), fireId)
                        .setOnCompleteListener(new OnCallCompleteListener() {
                            @Override
                            public void onComplete() {
                                isInserted = true;
                            }

                            @Override
                            public void onFailed() {
                                isInserted = false;
                            }
                        });
            }
        } else {
            todoModel.setAssignTo(assignToIds);
            todoModel.setPendingForSync(true);
            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_INSERT);

            RealmController.getInstance().addTodoItem(todoModel);
        }

        if (isInserted) {
            onBackPressed();
        } else {
            Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIsNumberBlockedOrNot(final TodoModel todo, final String userId, final String fireId) {

        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).orderByChild("number").equalTo(sessionManager.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BlockContactModel post = new BlockContactModel();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    post = postSnapshot.getValue(BlockContactModel.class);
                }

                if (Objects.requireNonNull(post).getUserId() == null) {
                    FirebaseRealtimeController.getInstance()
                            .addOrUpdateTodoInFirebaseForMultipleAssign(todo, userId, fireId)
                            .setOnCompleteListener(new OnCallCompleteListener() {
                                @Override
                                public void onComplete() {
                                    isInserted = true;
                                }

                                @Override
                                public void onFailed() {
                                    isInserted = false;
                                }
                            });
                    sendNotification(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog("Crr", "Failed to read value : " + error.toException());
            }
        });

    }

    private void sendNotification(String userId) {

        final String todo;
        if (isReminder) {
            todo = "Reminder";
        } else {
            todo = "Task";
        }

        String title = "Assigned " + "New " + todo;
        String body = this.title;

        networkConnectivity.pushNotificationForThisUser(userId, body, title, Constants.CREATE_NEW_TODO);

    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Gallery",
                "Camera"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallary();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //noinspection deprecation
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgURI = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgURI);
        //noinspection deprecation
        startActivityForResult(intent, CAMERA);

    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constants.IMAGE_FOLDAR_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "O_" + System.currentTimeMillis() + ".jpeg");
        } else {
            return null;
        }
        return mediaFile;
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (speakFor.equals(Constants.TITLE)) {
                    if (result != null) {
                        etTitle.setText(result.get(0));
                    }
                } else {
                    if (result != null) {
                        etNote.setText(result.get(0));
                    }
                }
            }
        } else if (requestCode == GALLERY) {
            if (data != null) {
                imgURI = data.getData();
                imageUriList.remove(imageUriList.size() - 1);
                imageUriList.add(imgURI.toString());
                imageUriList.add(null);

                adapter.notifyItemInserted(imageUriList.size() - 1);

            }
        } else if (requestCode == CAMERA) {
            try {
                imageUriList.remove(imageUriList.size() - 1);
                imageUriList.add(imgURI.toString());
                imageUriList.add(null);
                adapter.notifyItemInserted(imageUriList.size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
