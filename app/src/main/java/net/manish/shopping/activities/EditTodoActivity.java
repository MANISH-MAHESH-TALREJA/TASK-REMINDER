package net.manish.shopping.activities;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static net.manish.shopping.utils.Constants.EVERYDAY;
import static net.manish.shopping.utils.Constants.FRIDAY;
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
import static net.manish.shopping.utils.Constants.LANG_ENGLISH;
import static net.manish.shopping.utils.Constants.LANG_GUJARATI;
import static net.manish.shopping.utils.Constants.LANG_HINDI;
import static net.manish.shopping.utils.Constants.MONDAY;
import static net.manish.shopping.utils.Constants.MONTHLY;
import static net.manish.shopping.utils.Constants.ONCE;
import static net.manish.shopping.utils.Constants.PRIORITY_HIGH;
import static net.manish.shopping.utils.Constants.PRIORITY_LOW;
import static net.manish.shopping.utils.Constants.PRIORITY_MEDIUM;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import net.manish.shopping.R;
import net.manish.shopping.adapter.ImageListAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.AskPermition;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditTodoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDate;
    private TextView tvTime;
    private TextView tvRepeat;
    private TextView tvLanguage;
    private TextView tvRepeatDay;
    private TextView tvRepeatDateTime;
    private TextView tvAssignTo;
    private EditText etTitle, etNote;
    private DateAndTimePicker dateAndTimePicker;
    private RelativeLayout rltvRepeatDay;
    private RelativeLayout rltvRepeatDateTime;
    private RelativeLayout rltvAssignMeAlso;
    private RadioGroup radioGroupPriority, radioGroupType;
    private CheckBox checkBoxAssignMe;
    private boolean assignToMe = true;
    private String speakFor = ""; //speak for = title or note

    private final int REQ_CODE_SPEECH_INPUT = 100;
    public String fireId = "", title = "", note = "", date = "", time = "", remindTime = "", repeat = "", priority = "", speechLangCode = "", assignTo = "", followUpDate, followUpTime, createdBy, status, createdDate, updatedDate, completedBy, images = "";
    private int id, viewType = 0;
    private boolean isReminder, isDelivered;

    private final List<String> langList = new ArrayList<>();
    private final List<String> supportedLanguages = new ArrayList<>();
    private String repeatDay = "", repeatDate = "", assignToIds = "";
    private boolean isForRepeatPurpose = false;
    private final String TAG = EditTodoActivity.this.getClass().getName();
    private String[] prevNumbers = new String[0];

    private final List<ContactModel> selectedContactList = new ArrayList<>();
    private final List<ContactModel> oldSelectedContactList = new ArrayList<>();

    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    private RecyclerView recyclerViewImageList;
    private ImageListAdapter imageAdapter;
    private final List<String> imageUriList = new ArrayList<>();
    private final List<String> imageURLList = new ArrayList<>();
    private final List<String> imageListForRemove = new ArrayList<>();

    private final int GALLERY = 1;
    private final int CAMERA = 2;
    private Uri imgURI;
    private ProgressDialog progressDialog;

    private DatabaseReference myDbRef;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo);

        dateAndTimePicker = new DateAndTimePicker(EditTodoActivity.this);
        sessionManager = new SessionManager(this);
        networkConnectivity = new NetworkConnectivity(this);

        //init firebase
        mStorage = FirebaseStorage.getInstance().getReference();
        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        //for authentication

        getIntentValues();

        addAllLanguagesYouWant();
        setDatePickerListeners();
        findViews();
        hideViews();
        initViewsWithValue();
        setupRecyclerViewForImages();
        initSelectedContacts();
        initImageList();
    }

    private void initSelectedContacts() {
        String[] userArray = getArrayFromString(assignTo);

        selectedContactList.clear();
        oldSelectedContactList.clear();
        for (String s : userArray) {
            getUserById(s, false);
        }

    }

    private void setDatePickerListeners() {

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
            if (!isForRepeatPurpose) {
                time = t;
                tvTime.setText(t);
            } else {
                repeatDate = repeatDate + " " + t;
                tvRepeatDateTime.setText(repeatDate);
                isForRepeatPurpose = false;
            }
        });
    }

    private void getIntentValues() {
        //get values from keys
        id = getIntent().getIntExtra(KEY_ID, 0);
        fireId = getIntent().getStringExtra(KEY_FIRE_ID);
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
        assignTo = getIntent().getStringExtra(KEY_ASSIGNTO);

        followUpDate = getIntent().getStringExtra(KEY_FOLLOW_UP_DATE);
        followUpTime = getIntent().getStringExtra(KEY_FOLLOW_UP_TIME);
        createdBy = getIntent().getStringExtra(KEY_CREATED_BY);
        status = getIntent().getStringExtra(KEY_STATUS);
        createdDate = getIntent().getStringExtra(KEY_CREATED_DATE);
        updatedDate = getIntent().getStringExtra(KEY_UPDATED_DATE);
        viewType = getIntent().getIntExtra(KEY_VIEW_TYPE, 0);
        isDelivered = getIntent().getBooleanExtra(KEY_IS_DELIVERED, false);
        assignToMe = getIntent().getBooleanExtra(KEY_ASSIGN_ME_ALSO, true);
        completedBy = getIntent().getStringExtra(KEY_COMPLETED_BY);
        images = getIntent().getStringExtra(KEY_IMAGES);

        if (images != null && !images.isEmpty()) {
            String[] imageArray = images.split(",");
            imageURLList.clear();
            Collections.addAll(imageURLList, imageArray);
        }

        assignToIds = assignTo;
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
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.edit));
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
        tvAssignTo = findViewById(R.id.tv_assign_to);
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

    private void hideViews() {
        if (isReminder) {
            radioGroupPriority.setVisibility(View.GONE);
        }
    }

    private void initViewsWithValue() {
        etTitle.setText(title);
        etNote.setText(note);
        tvDate.setText(date);
        tvTime.setText(time);
        tvRepeat.setText(repeat);

        checkBoxAssignMe.setChecked(assignToMe);
        if (assignTo.equals("")) {
            rltvAssignMeAlso.setVisibility(View.GONE);
        } else {
            rltvAssignMeAlso.setVisibility(View.VISIBLE);
        }

        //manage assign to row
        prevNumbers = getArrayFromString(assignTo);

        if (prevNumbers.length == 0) {
            tvAssignTo.setText(getString(R.string.none));
        } else if (prevNumbers.length == 1) {
            getUserById(prevNumbers[0], true);
        } else {
            tvAssignTo.setText(getString(R.string.other));
        }

        //set Priority like high,medium,low
        switch (priority) {
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
        if (isReminder) {
            radioGroupType.check(R.id.radio_reminder);
        } else {
            radioGroupType.check(R.id.radio_task);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initImageList() {
        imageUriList.clear();
        imageUriList.addAll(imageURLList);
        imageUriList.add(null);
        imageAdapter.notifyDataSetChanged();
    }


    protected void setupRecyclerViewForImages() {

        recyclerViewImageList.setHasFixedSize(true);
        recyclerViewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        imageAdapter = new ImageListAdapter(imageUriList, this, Constants.SCREEN_EDIT_TODO_ACTIVITY);
        recyclerViewImageList.setAdapter(imageAdapter);

        recyclerViewImageList.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerViewImageList, new RecyclerViewClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view, final int position) {

                if (imageUriList.get(position) != null) {
                    ImageView ivRemoveImage = view.findViewById(R.id.iv_remove_image);

                    ivRemoveImage.setOnClickListener(v -> {

                        imageListForRemove.add(imageUriList.get(position));
                        imageUriList.remove(position);
                        imageAdapter.notifyDataSetChanged();

                    });
                } else {
                    CircleImageView ivAddImage = view.findViewById(R.id.iv_add_image);
                    ivAddImage.setOnClickListener(v -> {
                        if (AskPermition.getInstance(EditTodoActivity.this).isPermitted()) {
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

    public void getUserById(String userId, final boolean isForGetName) {

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                try {
                    if (userModel != null && userModel.getUserId() != null) {
                        //registered
                        if (isForGetName) {
                            String name = isContactActiveInCurrentMobile(userModel.getPhone());
                            if (name.equals("")) {
                                tvAssignTo.setText(userModel.getName());
                            } else {
                                tvAssignTo.setText(name);
                            }
                        } else {
                            ContactModel contactModel = new ContactModel();
                            contactModel.setId(RealmController.getInstance().getNextIdForContact());
                            contactModel.setName(userModel.getName());
                            contactModel.setNumber(userModel.getPhone());
                            contactModel.setUserId(userModel.getUserId());
                            contactModel.setSelected(true);

                            selectedContactList.add(contactModel);
                            oldSelectedContactList.add(contactModel);
                        }
                    }
                } catch (Exception e) {
                    Mylogger.getInstance().printLog(TAG, "onCatch : " + e.getLocalizedMessage());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String isContactActiveInCurrentMobile(String phone) {
        List<ContactModel> contactList = RealmController.getInstance().getAllRegisteredContacts();
        for (int i = 0; i < contactList.size(); i++) {
            if (phone.equals(Objects.requireNonNull(contactList.get(i)).getNumber())) {
                return Objects.requireNonNull(contactList.get(i)).getName();
            }
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public void onClick(View v) {

        dateAndTimePicker = new DateAndTimePicker(EditTodoActivity.this);

        int vId = v.getId();
        if (vId == R.id.tv_date) {
            isForRepeatPurpose = false;
            dateAndTimePicker.datePicker();
        } else if (vId == R.id.tv_time) {
            isForRepeatPurpose = false;
            dateAndTimePicker.timePicker(time);
        } else if (vId == R.id.rltv_repeat) {
            showRepeatMenus();
        } else if (vId == R.id.iv_speak_title) {
            speakFor = Constants.TITLE;
            promptSpeechInput();
        } else if (vId == R.id.iv_speak_note) {
            speakFor = Constants.NOTE;
            promptSpeechInput();
        } else if (vId == R.id.btn_create_todo) {//get all values from all views
            getAllValuesFromViews();

            if (isValidInputs()) {
                //Edit item here

                if (imageUriList.size() > 0) {

                    imageUriList.remove(imageUriList.size() - 1);
                    imageAdapter.notifyDataSetChanged();
                    imageURLList.clear();
                }

                if (imageUriList.size() > 0) {
                    prepareToUpload();
                } else {
                    images = "";
                    editCurrentRecord();
                }
            }
        } else if (vId == R.id.tv_change_input_language) {
            popupLanguages();
        } else if (vId == R.id.rltv_select_day) {
            showRepeatDayMenus();
        } else if (vId == R.id.rltv_assign_to) {
            showContactListActivity();
        } else if (vId == R.id.rltv_select_date) {
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
                    tvAssignTo.setText(getResources().getString(R.string.none));
                    rltvAssignMeAlso.setVisibility(View.GONE);
                    assignToMe = true;
                } else if (selectedContactList.size() == 1) {
                    tvAssignTo.setText(selectedContactList.get(0).getName());
                    rltvAssignMeAlso.setVisibility(View.VISIBLE);
                } else {
                    tvAssignTo.setText(getString(R.string.other));
                    rltvAssignMeAlso.setVisibility(View.VISIBLE);
                }

                int count = selectedContactList.size();
                String[] numberArray = new String[count];

                for (int i = 0; i < count; i++) {
                    numberArray[i] = selectedContactList.get(i).getUserId();
                }

                assignToIds = Arrays.toString(numberArray);
                assignTo = assignToIds;
            }

            @Override
            public void onTaskFinished(boolean isFinished, String note) {

            }

            @Override
            public void onAddComment(String comment) {

            }
        });
    }

    private void manageTickAndUntickNumbers(String[] prevNumbers) {

        for (String prevNumber : prevNumbers) {
            boolean isAvailable = false;
            for (int j = 0; j < selectedContactList.size(); j++) {
                if (prevNumber.equals(selectedContactList.get(j).getUserId())) {
                    isAvailable = true;
                }
            }
            if (!isAvailable) {
                removeTodoFromUntickUser(prevNumber);
                for (int k = 0; k < selectedContactList.size(); k++) {
                    if (prevNumber.equals(selectedContactList.get(k).getUserId())) {
                        //noinspection SuspiciousListRemoveInLoop
                        selectedContactList.remove(k);
                    }
                }

                int count = selectedContactList.size();
                String[] numberArray = new String[count];

                for (int x = 0; x < count; x++) {
                    numberArray[x] = selectedContactList.get(x).getUserId();
                }

                if (numberArray.length > 0) {
                    assignToIds = Arrays.toString(numberArray);
                    assignTo = assignToIds;
                }
            }
        }
    }

    private void removeTodoFromUntickUser(String prevNumber) {
        FirebaseRealtimeController.getInstance().deleteTodoByUserIdAndFireId(prevNumber, fireId);
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

    private void prepareToUpload() {

        progressDialog.setMessage(getString(R.string.pls_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        images = "";
        int index;
        for (index = 0; index < imageUriList.size(); index++) {

            Uri uri = Uri.parse(imageUriList.get(index));

            if (uri.toString().startsWith("Images/")) {

                if (imageUriList.size() > index + 1) {
                    images = String.format("%s%s,",images,uri.toString());
                } else {
                    images = images + uri.toString();

                    editCurrentRecord();
                    progressDialog.dismiss();
                }
            } else {
                StorageReference filePath = mStorage.child(Constants.STORAGE_FOLDAR_IMAGES).child(Objects.requireNonNull(uri.getLastPathSegment()));
                uploadToFirebase(filePath, index, uri);
            }
        }
    }

    private void uploadToFirebase(StorageReference filePath, final int index, Uri uri) {

        filePath.putFile(uri).addOnSuccessListener(taskSnapshot -> {

            String generatedFilePath = Objects.requireNonNull(taskSnapshot.getMetadata()).getPath();
            images = images + generatedFilePath;
            if (imageUriList.size() > index + 1) {
                images = images + ",";
            }

            if (index == imageUriList.size() - 1) {
                //this is last image uploaded
                editCurrentRecord();
                progressDialog.dismiss();
            }
        });
    }

    private void removeImagesFromFirebase() {
        if (imageListForRemove.size() > 0) {
            for (int i = 0; i < imageListForRemove.size(); i++) {
                deleteImageByFirebaseUrl(imageListForRemove.get(i), i);
            }
        }
    }


    private void deleteImageByFirebaseUrl(String imageUrl, final int index) {

        StorageReference storageRef = mStorage.getStorage().getReference();
        StorageReference photoRef = storageRef.child(imageUrl);

        photoRef.delete().addOnSuccessListener(aVoid -> {
            // File deleted successfully
            Log.d(TAG, "onSuccess: deleted file i : " + index);
        }).addOnFailureListener(exception -> {
            // Uh-oh, an error occurred!
            Log.d(TAG, "onFailure: did not delete file i : " + index + "\texception : " + exception.getLocalizedMessage());
        });


    }

    private void editCurrentRecord() {

        removeImagesFromFirebase();

        manageTickAndUntickNumbers(prevNumbers);

        int viewType;
        if (isReminder) {
            viewType = Constants.ITEM_TYPE_REMINDER;
        } else {
            viewType = Constants.ITEM_TYPE_TASK;
        }

        TodoModel todoModel = new TodoModel();
        todoModel.setId(id);
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

        todoModel.setFollowUpDate(followUpDate);
        todoModel.setFollowUpTime(followUpTime);
        todoModel.setCreatedBy(createdBy);
        todoModel.setStatus(status);
        todoModel.setCreatedDate(createdDate);
        todoModel.setUpdatedDate(dateAndTimePicker.getCurrentDateTime());
        todoModel.setViewType(viewType);
        todoModel.setDelivered(isDelivered);
        todoModel.setAssignTo(assignTo);
        todoModel.setAssignMeAlso(assignToMe);
        todoModel.setImages(images);

        todoModel.setCompletedBy(completedBy);

        if (ConnectionChecker.isInternetAvailable(this)) {
            todoModel.setPendingForSync(false);
            todoModel.setWhatPending("");


            if (selectedContactList.size() > 0) {
                for (int i = 0; i < selectedContactList.size(); i++) {
                    todoModel.setAssignTo(assignToIds);
                    FirebaseRealtimeController.getInstance()
                            .addOrUpdateTodoInFirebaseForMultipleAssign(todoModel, selectedContactList.get(i).getUserId(), fireId).setOnCompleteListener(new OnCallCompleteListener() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onFailed() {

                        }
                    });

                }

                notifyAssigedUserAboutTaskAssigned(todoModel);
            }

            if (assignToMe) {

                if (assignToIds.equals("[]") || assignTo.equals("")) {
                    todoModel.setAssignTo("");
                } else {
                    todoModel.setAssignTo(assignToIds);
                }
                FirebaseRealtimeController.getInstance().addOrUpdateTodoInFirebase(todoModel, sessionManager.getUserId(), fireId);
            }

        } else {
            todoModel.setAssignTo(assignToIds);
            todoModel.setPendingForSync(true);
            todoModel.setWhatPending(Constants.SYNC_PENDING_FOR_INSERT);

            RealmController.getInstance().addTodoItem(todoModel);
        }

        if (todoModel.getStatus().equals(Constants.STATUS_ACTIVE)) {
            CommonUtils.scheduleAlarm(this, todoModel);
        }


        backToPrevActivity();

    }

    private void notifyAssigedUserAboutTaskAssigned(TodoModel todoModel) {

        Mylogger.getInstance().printLog(TAG, "notifyAssignedUser..");
        String assignTo = todoModel.getAssignTo();
        String body = todoModel.getTitle();

        final String title;
        if (todoModel.isReminder()) {
            title = "Assigned New Reminder";
        } else {
            title = "Assigned New Task";
        }

        if (!assignTo.equals("")) {
            String[] arrayUsers = getArrayFromString(assignTo);
            Mylogger.getInstance().printLog(TAG, "arrayUsers() : " + Arrays.toString(arrayUsers));
            for (String arrayUser : arrayUsers) {
                if (!arrayUser.equals(sessionManager.getUserId())) {
                    //extract numger from here
                    boolean isAlreadyAssigedOnCreation = false;
                    for (int j = 0; j < oldSelectedContactList.size(); j++) {
                        Mylogger.getInstance().printLog(TAG, "oldSelected. userId : " + oldSelectedContactList.get(j).getUserId()
                                + "\tarrayUser[i] : " + arrayUser);
                        if (arrayUser.equals(oldSelectedContactList.get(j).getUserId())) {
                            isAlreadyAssigedOnCreation = true;

                            break;
                        }

                    }
                    Mylogger.getInstance().printLog(TAG, "isAlreadyAssigedOnCreation() userId : " + arrayUser);
                    if (!isAlreadyAssigedOnCreation) {
                        Mylogger.getInstance().printLog(TAG, "isAlreadyAssigedOnCreation() in : " + arrayUser);
                        networkConnectivity.pushNotificationForThisUser(arrayUser, body, title, Constants.CREATE_NEW_TODO);
                    }
                }
            }

        }
    }

    private void backToPrevActivity() {

        //go back previous activity with updated data
        Intent i = new Intent();
        i.putExtra(KEY_ID, id);
        i.putExtra(KEY_FIRE_ID, fireId);
        i.putExtra(KEY_TITLE, title);
        i.putExtra(KEY_NOTE, note);
        i.putExtra(KEY_DATE, date);
        i.putExtra(KEY_TIME, time);
        i.putExtra(KEY_REMIND_TIME, date + " " + time);
        i.putExtra(KEY_REPEAT, repeat);
        i.putExtra(KEY_REPEAT_DAY, repeatDay);
        i.putExtra(KEY_REPEAT_DATE, repeatDate);
        i.putExtra(KEY_PRIORITY, priority);
        i.putExtra(KEY_IS_REMINDER, isReminder);

        i.putExtra(KEY_FOLLOW_UP_DATE, followUpDate);
        i.putExtra(KEY_FOLLOW_UP_TIME, followUpTime);
        i.putExtra(KEY_CREATED_BY, createdBy);
        i.putExtra(KEY_STATUS, status);
        i.putExtra(KEY_CREATED_DATE, createdDate);
        i.putExtra(KEY_UPDATED_DATE, updatedDate);
        i.putExtra(KEY_VIEW_TYPE, viewType);
        i.putExtra(KEY_IS_DELIVERED, isDelivered);
        i.putExtra(KEY_ASSIGNTO, assignTo);
        i.putExtra(KEY_ASSIGN_ME_ALSO, assignToMe);
        i.putExtra(KEY_IMAGES, images);
        Mylogger.getInstance().printLog(TAG, "backToPrevActivity() images : " + images);

        setResult(RESULT_OK, i);
        finish();
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


    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

                imageAdapter.notifyItemInserted(imageUriList.size() - 1);

            }

        } else if (requestCode == CAMERA) {

            try {
                Mylogger.getInstance().printLog(TAG, "CAMERA : URI : " + imgURI.toString());

                imageUriList.remove(imageUriList.size() - 1);
                imageUriList.add(imgURI.toString());
                imageUriList.add(null);

                imageAdapter.notifyItemInserted(imageUriList.size() - 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
