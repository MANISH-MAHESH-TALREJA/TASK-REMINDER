package net.manish.shopping.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BlockContactModel extends RealmObject {

    int id;

    @PrimaryKey String uniqueId;

    String userId,name, number;
    boolean isBlocked;
    boolean isPendingForSync;  //(It is used to identify records which has been updated at localy during no internet available.)
    String whatPending; //(It is used to identify records that what is pending for sync e.g : Insert,delete,update)

    public BlockContactModel() {
    }

    public BlockContactModel(int id, String uniqueId, String userId, String name, String number, boolean isBlocked, boolean isPendingForSync, String whatPending) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.userId = userId;
        this.name = name;
        this.number = number;
        this.isBlocked = isBlocked;
        this.isPendingForSync = isPendingForSync;
        this.whatPending = whatPending;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
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
