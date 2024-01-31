package net.manish.shopping.firebaseutils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.SessionManager;

@SuppressLint("StaticFieldLeak")
public class FirebaseRealtimeController {


    public static FirebaseRealtimeController instance;
    //For firebase
    public FirebaseDatabase database;
    public static DatabaseReference myDbRef;
    public static String userId;
    public static SessionManager sessionManager;
    public static Context context;
    public OnCallCompleteListener onCallCompleteListener;

    public static DatabaseReference with(Application application) {

        context = application.getApplicationContext();

        if (instance == null) {
            instance = new FirebaseRealtimeController();
        }

        if (myDbRef == null) {
            myDbRef = FirebaseDatabase.getInstance().getReference();
            //get firebase auth instance
            sessionManager = new SessionManager(context);
            userId = sessionManager.getUserId();
        }
        return myDbRef;
    }

    public static void init(Activity context) {
        //get firebase auth instance
        sessionManager = new SessionManager(context);
        userId = sessionManager.getUserId();

    }

    public static FirebaseRealtimeController getInstance() {
        return instance;
    }

    public void setOnCompleteListener(OnCallCompleteListener listener) {
        onCallCompleteListener = listener;
    }

    public FirebaseRealtimeController addOrUpdateTodoInFirebase(TodoModel todoModel, String userId, String todoFireId) {

        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).child(todoFireId).setValue(todoModel).addOnCompleteListener(task -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onFailed();
            }
        });

        return instance;
    }

    public FirebaseRealtimeController addOrUpdateTodoInFirebaseForMultipleAssign(TodoModel todoModel, String userId, String fireId) {

        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).child(fireId).setValue(todoModel).addOnCompleteListener(task -> {

            if (onCallCompleteListener != null) {
                onCallCompleteListener.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onFailed();
            }
        });

        return instance;
    }

    public FirebaseRealtimeController deleteTodoByFireId(String fireId) {

        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).child(fireId).removeValue().addOnCompleteListener(task -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onFailed();
            }
        });

        return instance;
    }

    public void deleteTodoByUserIdAndFireId(String userId, String fireId) {

        myDbRef.child(Constants.TABLE_TODO_LIST).child(userId).child(fireId).removeValue().addOnCompleteListener(task -> {
        });

    }

    //===========================Comment Table======================
    public FirebaseRealtimeController addOrUpdateCommentByTaskId(CommentModel commentModel, String taskFireId, String cFireId) {
        myDbRef.child(Constants.TABLE_COMMENTS).child(taskFireId).child(cFireId).setValue(commentModel).addOnCompleteListener(task -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onFailed();
            }
        });

        return instance;
    }

    public void deleteCommentBycFireId(String cFireId) {
        myDbRef.child(Constants.TABLE_COMMENTS).child(cFireId).removeValue().addOnCompleteListener(task -> {
        });

    }

    ///For User Table
    public FirebaseRealtimeController addOrUpdateUser(UserModel userModel, String userId) {
        myDbRef.child(Constants.TABLE_USERS).child(userId).setValue(userModel).addOnCompleteListener(task -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onFailed();
            }
        });

        return instance;
    }

    //For block user
    public FirebaseRealtimeController addOrUpdateBlockUser(BlockContactModel contactModel, String userId, String generatedId) {

        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).child(generatedId).setValue(contactModel).addOnCompleteListener(task -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (onCallCompleteListener != null) {
                onCallCompleteListener.onFailed();
            }
        });

        return instance;
    }

    public FirebaseRealtimeController unblockUserByFireId(String userId, String fireId) {
        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).child(fireId).removeValue().addOnCompleteListener(task -> {
        });

        return instance;
    }

}
