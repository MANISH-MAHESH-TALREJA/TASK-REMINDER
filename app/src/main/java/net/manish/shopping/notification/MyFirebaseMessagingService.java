package net.manish.shopping.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import net.manish.shopping.MyApplication;
import net.manish.shopping.R;
import net.manish.shopping.activities.LoginActivity;
import net.manish.shopping.activities.MainActivity;
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.service.InternetStateChangeBR;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = MyFirebaseMessagingService.this.getClass().getName();
    private static final String ADMIN_CHANNEL_ID = "100";
    private NotificationManager notificationManager;
    NotificationUtils notificationUtils;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    @Override
    public void onNewToken(@androidx.annotation.NonNull String s) {
        super.onNewToken(s);

        Mylogger.getInstance().printLog(TAG, "new Token : " + s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        notificationUtils = new NotificationUtils(this);

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        if (!MyApplication.isIsAppVisible() && sessionManager.isLoggedIn()) {

            syncTodosWithRealtimeDatabase(userId);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels();
        }

        handleNotification(remoteMessage);
        // Check if message contains a notification payload.

    }

    private void handleNotification(RemoteMessage remoteMessage) {

        SessionManager sessionManager = new SessionManager(this);
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("from", "out");
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        Log.d(TAG, "data : " + remoteMessage.getData().toString());
        String msg, title, from, notiFor;

        Map<String, String> data = remoteMessage.getData();
        title = data.get("title");
        msg = data.get("msg");
        from = data.get("fromm");
        notiFor = data.get("notiFor");

        getUserNameByUserIdAndCreateNotification(this, from, msg, title, notiFor, intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    //This method is used to popup push notification in Oreo
    private void showNotificationMessage(Context context, String title, String message, Intent intent) {

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_IMMUTABLE
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());


    }

    public void syncTodosWithRealtimeDatabase(final String uId) {

        //get all comments from firebase real time db by taskId, And store all comment in single table

        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_TODO_LIST).child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Mylogger.getInstance().printLog(TAG, "TABLE_TODO_LIST onDataChanged" + dataSnapshot.getChildrenCount());

                RealmController.getInstance().clearTodosTable();//ade

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    TodoModel post = postSnapshot.getValue(TodoModel.class);
                    RealmController.getInstance().addTodoItem(post);

                    if (post != null && post.getStatus().equals(Constants.STATUS_ACTIVE)) {
                        CommonUtils.startForegroundReminderService(MyFirebaseMessagingService.this, post);
                    }
                }

                syncCommentsWithRealtimeDatabase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog(TAG, "Failed to read value : " + error.toException());
            }
        });

    }

    private void syncCommentsWithRealtimeDatabase() {
        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String taskId = postSnapshot.getKey();
                    Mylogger.getInstance().printLog(TAG, "key : " + taskId);

                    if (taskId != null) {
                        myDbRef.child(Constants.TABLE_COMMENTS).child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    CommentModel post = postSnapshot.getValue(CommentModel.class);
                                    RealmController.getInstance().addComment(post);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Failed to read value
                                Mylogger.getInstance().printLog(TAG, "Failed to read value : " + error.toException());
                            }
                        });
                    }
                }
                syncBlockListWithRealTimeD();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog(TAG, "Failed to read value : " + error.toException());

            }
        });

    }

    private void syncBlockListWithRealTimeD() {
        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BlockContactModel post = postSnapshot.getValue(BlockContactModel.class);
                    RealmController.getInstance().addOrUpdateBlockUser(post);
                }
                syncLocalDbWithFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog(TAG, "Failed to read value : " + error.toException());
            }
        });
    }

    private void syncLocalDbWithFirebase() {

        InternetStateChangeBR mNetworkReceiver = new InternetStateChangeBR();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, intentFilter);


        Intent broadcastIntent = new Intent(this, mNetworkReceiver.getClass());
        sendBroadcast(broadcastIntent);
    }

    public void getUserNameByUserIdAndCreateNotification(final Context context, String userId, final String msg, final String title, final String notiFor, final Intent intent) {

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null && userModel.getUserId() != null) {

                    String notiTitle;

                    Mylogger.getInstance().printLog(TAG, "phoneNumber : " + userModel.getPhone());
                    String name = CommonUtils.getNameByPhoneNumber(userModel.getPhone());

                    if (name.equals("")) {
                        name = userModel.getName();
                    }

                    if (notiFor.equals(Constants.CREATE_NEW_TODO)) {
                        notiTitle = name + " " + title;
                    } else {
                        notiTitle = title;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationMessage(context, notiTitle, msg, intent);
                    } else {
                        notificationUtils.showNotificationMessage(notiTitle, msg, "Timestanmp", intent);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
