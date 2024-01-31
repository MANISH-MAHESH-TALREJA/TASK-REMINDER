package net.manish.shopping.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContactModel extends RealmObject {

    int id;

    @PrimaryKey
    String userId;

    String name, number;
    boolean isSelected;

    public ContactModel() {
    }

    public ContactModel(int id, String userId, String name, String number, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.userId = userId;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
