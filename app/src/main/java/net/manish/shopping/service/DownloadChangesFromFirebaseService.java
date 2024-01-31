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
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import java.util.Objects;

public class DownloadChangesFromFirebaseService extends Service {

    public SessionManager sessionManager;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    public DownloadChangesFromFirebaseService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        sessionManager = new SessionManager(this);

        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        userId = sessionManager.getUserId();

        syncDatabase();
        return START_STICKY;
    }

    private void syncDatabase() {
        //get here all records which is pending for sync with firebase including (Todos & Comments)
        syncTodosWithRealtimeDatabase(userId);
    }

    public void syncTodosWithRealtimeDatabase(final String uId) {

        //get all comments from firebase real time db by taskId, And store all comment in single table

        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_TODO_LIST).child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                RealmController.getInstance().clearTodosTable();//ade

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    TodoModel post = postSnapshot.getValue(TodoModel.class);
                    RealmController.getInstance().addTodoItem(post);
                }

                syncCommentsWithRealtimeDatabase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog("Mainn", "Failed to read value : " + error.toException());
            }
        });

    }

    private void syncCommentsWithRealtimeDatabase() {
        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                RealmController.getInstance().clearCommentsTable();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String taskId = postSnapshot.getKey();

                    myDbRef.child(Constants.TABLE_COMMENTS).child(Objects.requireNonNull(taskId)).addListenerForSingleValueEvent(new ValueEventListener() {
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
                            Mylogger.getInstance().printLog("Mainn", "Failed to read value : " + error.toException());

                        }
                    });
                }

                syncBlockListWithRealTimeD();
                //You can Goto MainActivity from now
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog("Mainn", "Failed to read value : " + error.toException());
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Mylogger.getInstance().printLog("Mainn", "Failed to read value : " + error.toException());
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}