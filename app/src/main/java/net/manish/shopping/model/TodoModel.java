package net.manish.shopping.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@IgnoreExtraProperties
public class TodoModel extends RealmObject implements Parcelable {

    int id;

    @PrimaryKey
    String fireId;

    int viewType;
    String title, note, date, time, remindTime, repeat, repeatDay, repeatDate, priority, assignTo, followUpDate, followUpTime, createdBy, createdDate, updatedDate, status, completedBy;
    boolean isReminder, isDelivered;  //(isReminder= specify the item's type that it is reminder of todotask),(isDelivered=Conform from server that it is delivered to receiver)
    boolean isPendingForSync;  //(It is used to identify records which has been updated at localy during no internet available.)
    boolean assignMeAlso;  //(It is used to identify records for yes= assign task/reminder to creater also.)
    String whatPending; //(It is used to identify records that what is pending for sync e.g : Insert,delete,update)
    String images = "";//For add images while creating task or reminder

    public TodoModel() {
    }

    public TodoModel(int id, String fireId, String title, String note, String date, String time, String remindTime, String repeat, String repeatDay, String repeatDate, String priority, String assignTo, String followUpDate, String followUpTime, String createdBy, boolean isReminder, boolean isDelivered, String createdDate, String updatedDate, String status, int type, boolean isPendingForSync, String whatPending, boolean assignMeAlso, String completedBy, String images) {
        this.id = id;
        this.fireId = fireId;
        this.title = title;
        this.note = note;
        this.date = date;
        this.time = time;
        this.remindTime = remindTime;
        this.repeat = repeat;
        this.repeatDay = repeatDay;
        this.repeatDate = repeatDate;
        this.priority = priority;
        this.assignTo = assignTo;
        this.followUpDate = followUpDate;
        this.followUpTime = followUpTime;
        this.createdBy = createdBy;
        this.isReminder = isReminder;
        this.isDelivered = isDelivered;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.status = status;
        this.viewType = type;
        this.isPendingForSync = isPendingForSync;
        this.whatPending = whatPending;
        this.assignMeAlso = assignMeAlso;
        this.completedBy = completedBy;
        this.images = images;
    }

    protected TodoModel(Parcel in) {
        id = in.readInt();
        fireId = in.readString();
        viewType = in.readInt();
        title = in.readString();
        note = in.readString();
        date = in.readString();
        time = in.readString();
        remindTime = in.readString();
        repeat = in.readString();
        repeatDay = in.readString();
        repeatDate = in.readString();
        priority = in.readString();
        assignTo = in.readString();
        followUpDate = in.readString();
        followUpTime = in.readString();
        createdBy = in.readString();
        createdDate = in.readString();
        updatedDate = in.readString();
        status = in.readString();
        completedBy = in.readString();
        isReminder = in.readByte() != 0;
        isDelivered = in.readByte() != 0;
        isPendingForSync = in.readByte() != 0;
        assignMeAlso = in.readByte() != 0;
        whatPending = in.readString();
        images = in.readString();
    }

    public static final Creator<TodoModel> CREATOR = new Creator<TodoModel>() {
        @Override
        public TodoModel createFromParcel(Parcel in) {
            return new TodoModel(in);
        }

        @Override
        public TodoModel[] newArray(int size) {
            return new TodoModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFireId() {
        return fireId;
    }

    public void setFireId(String fireId) {
        this.fireId = fireId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(String remindTime) {
        this.remindTime = remindTime;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getRepeatDay() {
        return repeatDay;
    }

    public void setRepeatDay(String repeatDay) {
        this.repeatDay = repeatDay;
    }

    public String getRepeatDate() {
        return repeatDate;
    }

    public void setRepeatDate(String repeatDate) {
        this.repeatDate = repeatDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    public String getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(String followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getFollowUpTime() {
        return followUpTime;
    }

    public void setFollowUpTime(String followUpTime) {
        this.followUpTime = followUpTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isReminder() {
        return isReminder;
    }

    public void setReminder(boolean reminder) {
        isReminder = reminder;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public boolean isPendingForSync() {
        return isPendingForSync;
    }

    public void setPendingForSync(boolean pendingForSync) {
        isPendingForSync = pendingForSync;
    }

    public String getWhatPending() {
        return whatPending;
    }

    public void setWhatPending(String whatPending) {
        this.whatPending = whatPending;
    }

    public boolean isAssignMeAlso() {
        return assignMeAlso;
    }

    public void setAssignMeAlso(boolean assignMeAlso) {
        this.assignMeAlso = assignMeAlso;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(fireId);
        dest.writeInt(viewType);
        dest.writeString(title);
        dest.writeString(note);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(remindTime);
        dest.writeString(repeat);
        dest.writeString(repeatDay);
        dest.writeString(repeatDate);
        dest.writeString(priority);
        dest.writeString(assignTo);
        dest.writeString(followUpDate);
        dest.writeString(followUpTime);
        dest.writeString(createdBy);
        dest.writeString(createdDate);
        dest.writeString(updatedDate);
        dest.writeString(status);
        dest.writeString(completedBy);
        dest.writeByte((byte) (isReminder ? 1 : 0));
        dest.writeByte((byte) (isDelivered ? 1 : 0));
        dest.writeByte((byte) (isPendingForSync ? 1 : 0));
        dest.writeByte((byte) (assignMeAlso ? 1 : 0));
        dest.writeString(whatPending);
        dest.writeString(images);
    }
}
