package net.manish.shopping.realm;

import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.CountryCodeModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.utils.Constants;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmController {

    private static RealmController instance;
    private final Realm realm;

    public RealmController() {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with() {

        if (instance == null) {
            instance = new RealmController();
        }
        return instance;
    }

    public static RealmController getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    public void refresh() {
        realm.refresh();
    }

    //For Todos items-----------
    public void addTodoItem(TodoModel todoModel) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(todoModel);
        realm.commitTransaction();
    }

    public RealmResults<TodoModel> getAllActiveTodos(String date) {
        return realm.where(TodoModel.class)
                .equalTo("status", Constants.STATUS_ACTIVE)
                .or()
                .beginGroup()
                .equalTo("date", date)
                .equalTo("isReminder", false)
                .endGroup()

                .beginGroup()//Don't get item which hase whatPending=DELETE
                .equalTo("whatPending", Constants.SYNC_PENDING_FOR_INSERT)
                .or()
                .equalTo("whatPending", Constants.SYNC_PENDING_FOR_UPDATE)
                .or()
                .equalTo("whatPending", "")
                .endGroup()
                .findAll();
    }

    public RealmResults<TodoModel> getAllTodosByStatusAndTime(String datetime) {
        return realm.where(TodoModel.class)
                .equalTo("status", Constants.STATUS_ACTIVE)
                .equalTo("remindTime", datetime)
                .findAll();
    }

    public TodoModel getTodoById(int id) {
        return realm.where(TodoModel.class).equalTo("id", id).findFirst();
    }

    public TodoModel getTodoByFireId(String id) {

        TodoModel todoManaged = realm.where(TodoModel.class).equalTo("fireId", id).findFirst();
        TodoModel todoModel = new TodoModel();

        todoModel.setId(Objects.requireNonNull(todoManaged).getId());
        todoModel.setFireId(todoManaged.getFireId());
        todoModel.setTitle(todoManaged.getTitle());
        todoModel.setNote(todoManaged.getNote());
        todoModel.setDate(todoManaged.getDate());
        todoModel.setTime(todoManaged.getTime());
        todoModel.setRemindTime(todoManaged.getRemindTime());
        todoModel.setRepeat(todoManaged.getRepeat());
        todoModel.setRepeatDay(todoManaged.getRepeatDay());
        todoModel.setRepeatDate(todoManaged.getRepeatDate());
        todoModel.setPriority(todoManaged.getPriority());
        todoModel.setReminder(todoManaged.isReminder());
        todoModel.setFollowUpDate(todoManaged.getFollowUpDate());
        todoModel.setFollowUpTime(todoManaged.getFollowUpTime());
        todoModel.setCreatedBy(todoManaged.getCreatedBy());
        todoModel.setStatus(todoManaged.getStatus());
        todoModel.setCreatedDate(todoManaged.getCreatedDate());
        todoModel.setUpdatedDate(todoManaged.getUpdatedDate());
        todoModel.setViewType(todoManaged.getViewType());
        todoModel.setDelivered(todoManaged.isDelivered());
        todoModel.setAssignTo(todoManaged.getAssignTo());
        todoModel.setPendingForSync(todoManaged.isPendingForSync());
        todoModel.setWhatPending(todoManaged.getWhatPending());
        todoModel.setImages(todoManaged.getImages());

        return todoModel;
    }

    public RealmResults<TodoModel> getAllPendingTodosForSync() {
        return realm.where(TodoModel.class).equalTo("isPendingForSync", true).findAll();
    }

    public RealmResults<TodoModel> getAllTasksByStatus(String s) {
        return realm.where(TodoModel.class)
                .equalTo("isReminder", false)
                .equalTo("status", s)

                .beginGroup()//Don't get item which hase whatPending=DELETE
                .equalTo("whatPending", Constants.SYNC_PENDING_FOR_INSERT)
                .or()
                .equalTo("whatPending", Constants.SYNC_PENDING_FOR_UPDATE)
                .or()
                .equalTo("whatPending", "")
                .endGroup()
                .findAll();
    }

    public void removeTodoByFireId(final String id) {

        realm.executeTransactionAsync(realm -> {
            RealmResults<TodoModel> rows = realm.where(TodoModel.class).equalTo("fireId", id).findAll();
            rows.deleteAllFromRealm();
        });

    }

    public void clearTodosTable() {

        realm.executeTransactionAsync(realm -> {
            RealmResults<TodoModel> rows = realm.where(TodoModel.class).findAll();
            rows.deleteAllFromRealm();
        });

    }

    public void clearNonChangedTodosDuringOfflineFromTable() {

        realm.executeTransactionAsync(realm -> {
            RealmResults<TodoModel> rows = realm.where(TodoModel.class).equalTo("isPendingForSync", false).findAll();
            rows.deleteAllFromRealm();
        });

    }

    public void updateNewRemindDate(final int id, final String newDate) {
        realm.executeTransactionAsync(realm -> {
            TodoModel todoModel = realm.where(TodoModel.class).equalTo("id", id).findFirst();
            Objects.requireNonNull(todoModel).setRemindTime(newDate);
        });
    }

    public void updateTodoStatus(final int id, final String s, final String createdBy) {
        realm.executeTransactionAsync(realm -> {
            TodoModel todoModel = realm.where(TodoModel.class).equalTo("id", id).findFirst();
            if (todoModel != null) {
                todoModel.setStatus(s);
                todoModel.setCompletedBy(createdBy);
            }
        });
    }

    //For Comments items
    public RealmResults<CommentModel> getAllCommentsByTaskId(String id) {

        return realm.where(CommentModel.class).equalTo("taskId", id).findAll();
    }

    public RealmResults<CommentModel> getAllPendingCommentsForSync() {
        return realm.where(CommentModel.class).equalTo("isPendingForSync", true).findAll();
    }

    public void removeCommentByCFireId(final String cFireId) {
        realm.executeTransactionAsync(realm -> {
            RealmResults<CommentModel> rows = realm.where(CommentModel.class).equalTo("cFireId", cFireId).findAll();
            rows.deleteAllFromRealm();
        });
    }

    public void addComment(CommentModel commentModel) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(commentModel);
        realm.commitTransaction();
    }

    public void clearCommentsTable() {

        realm.executeTransactionAsync(realm -> {
            RealmResults<CommentModel> rows = realm.where(CommentModel.class).findAll();
            rows.deleteAllFromRealm();
        });

    }

    //For Country code table
    public void addNewCountry(CountryCodeModel countryCodeModel) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(countryCodeModel);
        realm.commitTransaction();
    }

    public RealmResults<CountryCodeModel> getAllCountryCodes() {
        return realm.where(CountryCodeModel.class).findAll();
    }

    public void removeCountryCodes() {

        realm.executeTransactionAsync(realm -> {
            RealmResults<CountryCodeModel> rows = realm.where(CountryCodeModel.class).findAll();
            rows.deleteAllFromRealm();
        });
    }

    //For Registered Contact list
    public void addRegisteredContact(ContactModel contactModel) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(contactModel);
        realm.commitTransaction();
    }

    public RealmResults<ContactModel> getAllRegisteredContacts() {
        return realm.where(ContactModel.class).findAll();
    }

    public int getNextIdForContact() {
        try {
            Number number = RealmController.getInstance().getRealm().where(ContactModel.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }


    public void clearRegisteredContacts() {

        realm.executeTransactionAsync(realm -> {
            RealmResults<ContactModel> rows = realm.where(ContactModel.class).findAll();
            rows.deleteAllFromRealm();
        });

    }

    //For add or update blocked user
    public void addOrUpdateBlockUser(BlockContactModel contactModel) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(contactModel);
        realm.commitTransaction();
    }

    public RealmResults<BlockContactModel> getAllBlockedNumber() {
        return realm.where(BlockContactModel.class).equalTo("isBlocked", true).findAll();
    }

    public RealmResults<BlockContactModel> getAllBlockedAndUnblockedNumber() {
        return realm.where(BlockContactModel.class).findAll();
    }

    public void removeBlockedNumberByUniqueId(final String id) {

        realm.executeTransactionAsync(realm -> {
            RealmResults<BlockContactModel> rows = realm.where(BlockContactModel.class).equalTo("uniqueId", id).findAll();
            rows.deleteAllFromRealm();
        });

    }

    //Config realm Database
    public void clearWholeDatabase() {
        realm.executeTransactionAsync(realm -> realm.deleteAll());
    }

}
