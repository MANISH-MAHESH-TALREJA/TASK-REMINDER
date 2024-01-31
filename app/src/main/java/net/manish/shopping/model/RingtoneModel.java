package net.manish.shopping.model;

import android.net.Uri;

public class RingtoneModel {

    int id;
    String name;
    Uri uri;
    boolean isPlaying;

    public RingtoneModel(int id, String name, Uri uri, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.isPlaying = isSelected;
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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean selected) {
        isPlaying = selected;
    }
}
