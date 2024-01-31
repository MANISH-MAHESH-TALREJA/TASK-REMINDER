package net.manish.shopping.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CommentModel extends RealmObject {

    int id;

    @PrimaryKey
    String cFireId;

    String taskId, userId, phone;
    String senderName;
    String comment, createdDate, updatedDate;
    boolean isPendingForSync;  //(It is used to identify records which has been updated at localy during no internet available.)
    String whatPending; //(It is used to identify records that what is pending for sync e.g : Insert,delete,update)

    public CommentModel() {
    }

    public CommentModel(int id, String cFireId, String taskId, String userId, String phone,String senderName, String comment, String createdAt, String updatedAt, boolean isPendingForSync, String whatPending) {
        this.id = id;
        this.cFireId = cFireId;
        this.taskId = taskId;
        this.userId = userId;
        this.phone = phone;
        this.senderName = senderName;
        this.comment = comment;
        this.createdDate = createdAt;
        this.updatedDate = updatedAt;
        this.isPendingForSync = isPendingForSync;
        this.whatPending = whatPending;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getcFireId() {
        return cFireId;
    }

    public void setcFireId(String cFireId) {
        this.cFireId = cFireId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String date) {
        this.createdDate = date;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
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
}
