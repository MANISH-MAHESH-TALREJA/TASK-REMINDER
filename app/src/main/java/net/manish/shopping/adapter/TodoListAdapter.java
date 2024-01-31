package net.manish.shopping.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.manish.shopping.R;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Objects;

public class TodoListAdapter extends RecyclerView.Adapter {

    private final List<TodoModel> dataset;
    private final Context context;
    private final SessionManager sessionManager;
    private final DateAndTimePicker dateAndTimePicker;

    private final DatabaseReference myDbRef;

    public TodoListAdapter(List<TodoModel> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.dateAndTimePicker = new DateAndTimePicker(context);

        //init firebase.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
    }

    @Override
    public int getItemCount() {
        if (dataset == null)
            return 0;
        return dataset.size();
    }


    @Override
    public int getItemViewType(int position) {

        if (dataset.get(position).getViewType() == Constants.ITEM_TYPE_REMINDER) {
            return Constants.ITEM_TYPE_REMINDER;
        } else if (dataset.get(position).getViewType() == Constants.ITEM_TYPE_TASK) {
            return Constants.ITEM_TYPE_TASK;
        } else {
            return Constants.ITEM_TYPE_REMINDER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == Constants.ITEM_TYPE_REMINDER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
            return new ViewHolderReminder(view);
        } else if (viewType == Constants.ITEM_TYPE_TASK) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new ViewHolderTask(view);
        }
        //noinspection ConstantConditions
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (dataset.get(position) != null) {

            if (dataset.get(position).getViewType() == Constants.ITEM_TYPE_REMINDER) {

                ((ViewHolderReminder) holder).tvTitle.setText(dataset.get(position).getTitle());

                String dateTitle = CommonUtils.getCurrentDateText(dateAndTimePicker.extractDateFromDateTime(dataset.get(position).getRemindTime()), dateAndTimePicker.getCurrentDate());
                ((ViewHolderReminder) holder).tvDate.setText(dateTitle + " \u25CF " + dateAndTimePicker.extractTimeFromDateTime(dataset.get(position).getRemindTime()));
                ((ViewHolderReminder) holder).tvRepeat.setText(dataset.get(position).getRepeat());

                //For manage assign to linear layout
                if (dataset.get(position).getAssignTo().equals("")) {
                    ((ViewHolderReminder) holder).lnrAssignTo.setVisibility(View.GONE);
                } else {
                    ((ViewHolderReminder) holder).lnrAssignTo.setVisibility(View.VISIBLE);

                    String[] arrayNames = getArrayFromString(dataset.get(position).getAssignTo());

                    if (dataset.get(position).getCreatedBy().equals(sessionManager.getUserId())) {

                        ((ViewHolderReminder) holder).tvAssignTitle.setText(context.getString(R.string.assigned_to));
                        //this task created by self
                        if (arrayNames.length == 0) {
                            ((ViewHolderReminder) holder).tvAssignTo.setVisibility(View.GONE);
                        } else if (arrayNames.length == 1) {
                            setNameByFirebaseUserId(arrayNames[0], ((ViewHolderReminder) holder).tvAssignTo);
                        } else {
                            ((ViewHolderReminder) holder).tvAssignTo.setText(context.getString(R.string.other));
                        }
                    } else {
                        ((ViewHolderReminder) holder).tvAssignTitle.setText(context.getString(R.string.assign_from));
                        setNameByFirebaseUserId(dataset.get(position).getCreatedBy(), ((ViewHolderReminder) holder).tvAssignTo);
                    }
                }

            } else if (dataset.get(position).getViewType() == Constants.ITEM_TYPE_TASK) {

                String dateTitle = CommonUtils.getCurrentDateText(dateAndTimePicker.extractDateFromDateTime(dataset.get(position).getRemindTime()), dateAndTimePicker.getCurrentDate());
                ((ViewHolderTask) holder).tvDate.setText(dateTitle + " \u25CF " + dateAndTimePicker.extractTimeFromDateTime(dataset.get(position).getRemindTime()));
                ((ViewHolderTask) holder).tvRepeat.setText(dataset.get(position).getRepeat());

                if (dataset.get(position).getStatus().equals(Constants.STATUS_FINISHED)) {

                    if (dataset.get(position).getCompletedBy() != null) {
                        if (dataset.get(position).getCompletedBy().equals(sessionManager.getUserId())) {
                            ((ViewHolderTask) holder).tvCompletedBy.setText(context.getResources().getString(R.string.you));
                        } else {
                            setNameByFirebaseUserId(dataset.get(position).getCompletedBy(), ((ViewHolderTask) holder).tvCompletedBy);
                        }
                    }

                    if (dataset.get(position).getAssignTo().equals("")) {
                        ((ViewHolderTask) holder).lnrCompletedBy.setVisibility(View.GONE);
                    } else {
                        ((ViewHolderTask) holder).lnrCompletedBy.setVisibility(View.VISIBLE);
                    }
                } else {
                    ((ViewHolderTask) holder).lnrCompletedBy.setVisibility(View.GONE);
                }

                if (dataset.get(position).getAssignTo().equals("")) {
                    ((ViewHolderTask) holder).lnrAssignedTo.setVisibility(View.GONE);
                } else {
                    ((ViewHolderTask) holder).lnrAssignedTo.setVisibility(View.VISIBLE);

                    String[] arrayNames = getArrayFromString(dataset.get(position).getAssignTo());

                    if (dataset.get(position).getCreatedBy().equals(sessionManager.getUserId())) {

                        ((ViewHolderTask) holder).tvAssignToTitle.setText(context.getString(R.string.assigned_to));//For share assigned todos

                        //this task created by self
                        if (arrayNames.length == 0) {
                            ((ViewHolderTask) holder).tvAssignTo.setVisibility(View.GONE);
                        } else if (arrayNames.length == 1) {
                            setNameByFirebaseUserId(arrayNames[0], ((ViewHolderTask) holder).tvAssignTo);
                        } else {
                            ((ViewHolderTask) holder).tvAssignTo.setText(context.getString(R.string.other));
                        }
                    } else {
                        //this task created by other person
                        ((ViewHolderTask) holder).tvAssignToTitle.setText(context.getString(R.string.assign_from));
                        setNameByFirebaseUserId(dataset.get(position).getCreatedBy(), ((ViewHolderTask) holder).tvAssignTo);
                    }
                }

                switch (dataset.get(position).getPriority()) {
                    case Constants.PRIORITY_HIGH:
                        ((ViewHolderTask) holder).viewPriority.setBackgroundColor(Color.RED);
                        break;
                    case Constants.PRIORITY_MEDIUM:
                        ((ViewHolderTask) holder).viewPriority.setBackgroundColor(Color.YELLOW);
                        break;
                    case Constants.PRIORITY_LOW:
                        ((ViewHolderTask) holder).viewPriority.setBackgroundColor(Color.GREEN);
                        break;
                }

                if (dataset.get(position).getStatus().equals(Constants.STATUS_FINISHED)) {
                    ((ViewHolderTask) holder).tvTitle.setText(dataset.get(position).getTitle());
                    ((ViewHolderTask) holder).tvTitle.setPaintFlags(
                            ((ViewHolderTask) holder).tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    ((ViewHolderTask) holder).checkBox.setChecked(true);
                    ((ViewHolderTask) holder).checkBox.setClickable(false);

                } else {
                    ((ViewHolderTask) holder).tvTitle.setText(dataset.get(position).getTitle());
                    ((ViewHolderTask) holder).tvTitle.setPaintFlags(
                            ((ViewHolderTask) holder).tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    ((ViewHolderTask) holder).checkBox.setChecked(false);
                    ((ViewHolderTask) holder).checkBox.setClickable(true);
                }

                ((ViewHolderTask) holder).checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                });
            }
        }
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

    static class ViewHolderTask extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvDate;
        TextView tvRepeat;
        TextView tvAssignTo;
        TextView tvAssignToTitle;
        TextView tvCompletedBy;
        TextView tvCreatedBy;
        View viewPriority;
        AppCompatCheckBox checkBox;
        LinearLayout rltvRoot, lnrAssignedTo, lnrCompletedBy;
        ImageView ivRemove;

        ViewHolderTask(View itemView) {
            super(itemView);

            viewPriority = itemView.findViewById(R.id.view_priority);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAssignTo = itemView.findViewById(R.id.tv_for_who);
            tvAssignToTitle = itemView.findViewById(R.id.tv_assign_to_title);
            tvCreatedBy = itemView.findViewById(R.id.tv_creaed_by);
            tvRepeat = itemView.findViewById(R.id.tv_repeat);
            tvCompletedBy = itemView.findViewById(R.id.tv_completed_by);

            checkBox = itemView.findViewById(R.id.checkbox_done);
            rltvRoot = itemView.findViewById(R.id.lnr_root);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            lnrAssignedTo = itemView.findViewById(R.id.lnr_assigned_to);
            lnrCompletedBy = itemView.findViewById(R.id.lnr_completed_by);

            rltvRoot.setOnClickListener(v -> {
            });
            ivRemove.setOnClickListener(v -> {
            });
        }
    }

    static class ViewHolderReminder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvDate;
        TextView tvRepeat;
        TextView tvAssignTitle;
        TextView tvAssignTo;
        LinearLayout rltvRoot, lnrAssignTo;
        ImageView ivRemove;

        ViewHolderReminder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRepeat = itemView.findViewById(R.id.tv_repeat);
            tvAssignTitle = itemView.findViewById(R.id.tv_assign_to_title);
            tvAssignTo = itemView.findViewById(R.id.tv_for_who);

            ivRemove = itemView.findViewById(R.id.iv_remove);
            rltvRoot = itemView.findViewById(R.id.lnr_root);
            lnrAssignTo = itemView.findViewById(R.id.lnr_assigned_to);

            rltvRoot.setOnClickListener(v -> {
            });

            ivRemove.setOnClickListener(v -> {
            });
        }
    }

    public void setNameByFirebaseUserId(final String userId, final TextView textView) {

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null) {
                    if (userModel.getUserId() != null) {

                        String name = isContactActiveInCurrentMobile(userModel.getPhone());
                        if (name.equals("")) {
                            textView.setText(userModel.getName());
                        } else {
                            textView.setText(name);
                        }

                    }
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
}