package net.manish.shopping.model;

import androidx.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CountryCodeModel extends RealmObject{

    @PrimaryKey int id;
    String countryName,countryCode;

    public CountryCodeModel() {
    }

    public CountryCodeModel(int id, String countryName, String countryCode) {
        this.id = id;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
