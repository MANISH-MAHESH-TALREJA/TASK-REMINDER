package net.manish.shopping.utils;

public class Constants {

    public static final String SERVER_KEY = "AAAARjPhRwE:APA91bHSBpTZnicwlrFz4SAHSlk-EwlHjOpKVcRGGV1u0PEqoFqC0Sn3eFZPWz0y2qNYtm4u8E0jhehGhauSTa4iMKPk9xL56ZRMnYAKvPp0SMCjrLAdrkMtquBxTYINxYk7zzfIwvWC";

    //Firebase table names
    public static final String TABLE_TODO_LIST = "todolist";
    public static final String TABLE_COMMENTS = "comments";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BLOCKED_USERS = "blockedusers";

    public static final int TYPE_1 = 1;
    public static final int TYPE_ADD_PHOTO = 2;

    public static final String REMINDER = "Reminder";
    public static final int ITEM_TYPE_REMINDER = 1;
    public static final int ITEM_TYPE_TASK = 2;

    public static final String COUNTRY_CODE_INDIA = "+91";
    public static final String COUNTRY_CODE_US = "+1";
    public static final String COUNTRY_CODE_AUSTRALIA = "+61";

    public static final String KEY_REPEAT_DAY = "repeatDay";
    public static final String KEY_REPEAT_DATE = "repeatDate";

    //sCREENS FIXED VALUES
    public static final String SCREEN_TODO_DETAILS_ACTIVITY = "TodoDetailsActivity";
    public static final String SCREEN_CREATE_NEW_TODO_ACTIVITY = "CreateNewTodoActivity";
    public static final String SCREEN_EDIT_TODO_ACTIVITY = "EditTodoActivity";

    public static final String SCREEN_FORGOT_PASSWORD = "ForgotScreen";
    public static final String SCREEN_SETTINGS = "SettingsScreen";
    public static final String TODAY_TEXT = "Today";
    public static final String CREATE_NEW_TODO = "createNewTodo";
    public static final String CREATE_NEW_COMMENT = "createNewComment";
    public static final String FINISH_TASK = "finishTask";
    public static final String IMAGE_FOLDAR_NAME = "Task Rider";
    public static final String STORAGE_FOLDAR_IMAGES = "Images";
    public static final int FOREGROUND_NOTIFICATION_ID = 1000;
    public static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;

    public static String KEY_FIREBASE_TOKEN = "firebasetoken";
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    //Sync fix values
    public static final String SYNC_PENDING_FOR_INSERT = "INSERT";
    public static final String SYNC_PENDING_FOR_UPDATE = "UPDATE";
    public static final String SYNC_PENDING_FOR_DELETE = "DELETE";

    public final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    //snooze times
    public static final String SNOOZE_5_MIN = "5";
    public static final String SNOOZE_10_MIN = "10";
    public static final String SNOOZE_15_MIN = "15";
    public static final String SNOOZE_30_MIN = "30";
    public static final String SNOOZE_1_HOUR = "60";

    //status for todos
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_FINISHED = "Finished";

    //priority
    public final static String PRIORITY_HIGH = "High";
    public final static String PRIORITY_MEDIUM = "Medium";
    public final static String PRIORITY_LOW = "Low";

    //languages
    public final static String LANG_GUJARATI = "ગુજરાતી";
    public final static String LANG_HINDI = "हिंदी";
    public final static String LANG_ENGLISH = "English";
    //language codes
    public final static String LANG_CODE_GUJARATI = "gu-IN";
    public final static String LANG_CODE_HINDI = "hi";
    public final static String LANG_CODE_ENGLISH = "en";

    //repeat dialog menus
    public final static String ONCE = "One Time";
    public final static String EVERYDAY = "Everyday";
    public final static String WEEKLY = "Weekly";
    public final static String MONTHLY = "Monthly";
    public final static String YEARLY = "Yearly";
    public static final String TITLE = "Title";
    public static final String NOTE = "Note";

    //keys
    public static final String KEY_ID = "id";
    public static final String KEY_FIRE_ID = "fireId";
    public static final String KEY_TITLE = "title";
    public static final String KEY_NOTE = "note";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_REPEAT = "repeat";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_ASSIGNTO = "assignTo";
    public static final String KEY_ASSIGN_ME_ALSO = "assignMeAlso";
    public static final String KEY_CREATED_BY = "createdBy";
    public static final String KEY_IS_REMINDER = "isReminder";
    public static final String KEY_REMIND_TIME = "remindTime";
    public static final String KEY_FOLLOW_UP_DATE = "followUpDate";
    public static final String KEY_FOLLOW_UP_TIME = "followUpTime";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_DATE = "createdDate";
    public static final String KEY_UPDATED_DATE = "updatedDate";
    public static final String KEY_VIEW_TYPE = "viewType";
    public static final String KEY_IS_DELIVERED = "isDelivered";
    public static final String KEY_COMPLETED_BY = "completedBy";
    public static final String KEY_IMAGES = "images";
    public static final String KEY_UNIQUE_ID = "UniqueId";

    //Login Fields name
    public static final String KEY_USERID = "userid";
    public static final String KEY_PHONE_NUMBER = "phonenumber";
    public static final String KEY_COME_FROM = "comeFrom";

    //filter titles
    public static final String FILTER_TOMORROW = "Tomorrow";
    public static final String FILTER_NEXT_WEEK = "Next 7 Days";
    public static final String FILTER_NEXT_MONTH = "Next 30 Days";
    public static final String FILTER_NEXT_YEAR = "Next Year";

    //repeate days list
    public static final String MONDAY = "Monday";
    public static final String TUESDAY = "Tuesday";
    public static final String WEDNESDAY = "Wednesday";
    public static final String THURSDAY = "Thursday";
    public static final String FRIDAY = "Friday";
    public static final String SATURDAY = "Saturday";
    public static final String SUNDAY = "Sunday";

    //wEEK DAYS NUMBERS
    public static final int ONE = 1;
    public static final int WEEK_DAYS = 7;
    public static final int MONTH_DAYS = 30;
    public static final int YEAR_DAYS = 365;

}
