package net.manish.shopping.listeners;

import net.manish.shopping.model.ContactModel;

import java.util.List;

public interface OnItemClickedListener {
    void onItemClicked(List<ContactModel> selectedContacts);
    void onTaskFinished(boolean isFinished, String note);
    void onAddComment(String comment);
}