package net.manish.shopping.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserModel extends RealmObject {

    int id;

    @PrimaryKey String userId;

    String fireToken,name,phone,password;

    public UserModel() {
    }

    public UserModel(int id, String userId,String fireToken, String name, String phone, String password) {
        this.id = id;
        this.userId = userId;
        this.fireToken = fireToken;
        this.name = name;
        this.phone = phone;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFireToken() {
        return fireToken;
    }

    public void setFireToken(String fireToken) {
        this.fireToken = fireToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
