package net.manish.shopping.fragments;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.manish.shopping.R;
import net.manish.shopping.adapter.MyContactsAdapter;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.AskPermition;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

public class ActiveContactsFragment extends Fragment {

    private  final String TAG = ActiveContactsFragment.this.getClass().getName();
    private View view;
    private IndexFastScrollRecyclerView mRecyclerView;
    private TextView textViewNDF;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<ContactModel> mainContactList = new ArrayList<>();
    private final List<BlockContactModel> blockedList = new ArrayList<>();
    private final List<BlockContactModel> mainList = new ArrayList<>();
    public static List<BlockContactModel> contactList = new ArrayList<>();
    public static OnItemClickedListener onItemClickedListener;
    private MyContactsAdapter adapter;

    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    public static void setOnItemClickedListener(OnItemClickedListener listener) {
        onItemClickedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        networkConnectivity = new NetworkConnectivity(getActivity());
        sessionManager = new SessionManager(getActivity());
        userId = sessionManager.getUserId();
        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        mainContactList.clear();
        mainContactList.addAll(RealmController.getInstance().getAllRegisteredContacts());


    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_active_contacts, container, false);

        findViews();
        setupRecyclerView();

        if (mainContactList.size() == 0) {
            if (AskPermition.getInstance(getActivity()).canReadContacts()) {
                if (networkConnectivity.isNetworkAvailable()) {
                    new GetContacts().execute();
                }
            }
        } else {
            getActiveContacts();
        }

        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // Refresh your fragment here
            if (mRecyclerView != null) {
                getActiveContacts();
            }
        }
    }

    private void findViews() {

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mRecyclerView = view.findViewById(R.id.fast_scroller_recycler);
        textViewNDF = view.findViewById(R.id.tv_ndf);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (networkConnectivity.isNetworkAvailable()) {
                getActiveContacts();
            }else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    protected void initialiseData() {

        //Alphabet fast scroller data
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < contactList.size(); i++) {
            String name = contactList.get(i).getName();
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word)) {
                strAlphabets.add(word);
            }
        }
    }

    private void setupRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyContactsAdapter(contactList, getActivity());
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setIndexTextSize(12);
        mRecyclerView.setIndexBarColor("#33334c");
        mRecyclerView.setIndexBarCornerRadius(10);
        mRecyclerView.setIndexBarTransparentValue((float) 0.4);
        mRecyclerView.setIndexbarMargin(5);
        mRecyclerView.setIndexbarWidth(35);
        mRecyclerView.setPreviewPadding(0);
        mRecyclerView.setIndexBarTextColor("#FFFFFF");

        mRecyclerView.setIndexBarVisibility(true);
        mRecyclerView.setIndexbarHighLightTextColor("#33334c");
        mRecyclerView.setIndexBarHighLightTextVisibility(true);

        //setup item click listener
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), mRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(final View view, final int position) {

                Button btnBlock = view.findViewById(R.id.btn_block);
                btnBlock.setOnClickListener(v -> blockConfirmationAlert(position));
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

    }

    @SuppressLint("NotifyDataSetChanged")
    private void blockConfirmationAlert(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());


        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.block_user_msg))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                    String generatedId = myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(sessionManager.getUserId()).push().getKey();

                    BlockContactModel blockContactModel = new BlockContactModel();

                    blockContactModel.setId(contactList.get(position).getId());
                    blockContactModel.setName(contactList.get(position).getName());
                    blockContactModel.setNumber(contactList.get(position).getNumber());
                    blockContactModel.setBlocked(true);
                    blockContactModel.setUniqueId(generatedId);
                    blockContactModel.setUserId(contactList.get(position).getUserId());

                    if (ConnectionChecker.isInternetAvailable(requireActivity())) {

                        blockContactModel.setPendingForSync(false);
                        blockContactModel.setWhatPending("");

                        FirebaseRealtimeController.getInstance().addOrUpdateBlockUser(blockContactModel, sessionManager.getUserId(), generatedId).setOnCompleteListener(new OnCallCompleteListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onComplete() {

                                dialog.dismiss();

                                contactList.remove(position);
                                adapter.notifyDataSetChanged();
                                hideRecyclerView();
                            }

                            @Override
                            public void onFailed() {
                                dialog.dismiss();
                            }
                        });
                    } else {

                        dialog.dismiss();

                        blockContactModel.setPendingForSync(true);
                        blockContactModel.setWhatPending(Constants.SYNC_PENDING_FOR_INSERT);

                        RealmController.getInstance().addOrUpdateBlockUser(blockContactModel);

                        contactList.remove(position);
                        adapter.notifyDataSetChanged();
                        hideRecyclerView();

                    }


                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                    dialog.dismiss();
                }).show();
    }

    private void sortDataAlphabeticWise() {
        //sort data alphabetic wise
        contactList.sort(Comparator.comparing(BlockContactModel::getName));
    }

    private void hideRecyclerView() {
        if (contactList.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            textViewNDF.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            textViewNDF.setVisibility(View.GONE);
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByNameOrNumber(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByNameOrNumber(newText);
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh) {

            if (networkConnectivity.isNetworkAvailable()) {
                syncBlockListWithRealTimeD();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void filterByNameOrNumber(String query) {

        contactList.clear();
        for (int i = 0; i < mainList.size(); i++) {
            if (mainList.get(i).getName().toLowerCase().contains(query.toLowerCase())
                    || mainList.get(i).getNumber().contains(query)) {
                contactList.add(mainList.get(i));
            }
        }

        refreshList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshList() {

        sortDataAlphabeticWise();
        initialiseData();

        adapter.notifyDataSetChanged();
        hideRecyclerView();
    }

    private void getActiveContacts() {

        mainContactList.clear();
        mainContactList.addAll(RealmController.getInstance().getAllRegisteredContacts());
        blockedList.clear();
        blockedList.addAll(RealmController.getInstance().getAllBlockedNumber());

        mainList.clear();
        for (int i = 0; i < mainContactList.size(); i++) {
            boolean isBlocked = false;
            for (int j = 0; j < blockedList.size(); j++) {
                if (mainContactList.get(i).getNumber().equals(blockedList.get(j).getNumber())
                        && blockedList.get(j).isBlocked()  //added for support offline
                        ) {
                    isBlocked = true;
                }
            }

            if (!isBlocked) {
                BlockContactModel contactModel = new BlockContactModel();
                contactModel.setId(getNextIdForBlockedUser());
                contactModel.setName(mainContactList.get(i).getName());
                contactModel.setNumber(mainContactList.get(i).getNumber());
                contactModel.setUserId(mainContactList.get(i).getUserId());
                contactModel.setBlocked(false);

                mainList.add(contactModel);
            }
        }

        swipeRefreshLayout.setRefreshing(false);
        contactList.clear();
        contactList.addAll(mainList);
        refreshList();
    }

    private void syncBlockListWithRealTimeD() {
        swipeRefreshLayout.setRefreshing(true);
        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                swipeRefreshLayout.setRefreshing(false);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BlockContactModel post = postSnapshot.getValue(BlockContactModel.class);
                    RealmController.getInstance().addOrUpdateBlockUser(post);
                }

                getActiveContacts();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            getContactList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);

            getActiveContacts();
        }
    }

    @SuppressLint("Range")
    private void getContactList() {

        ContentResolver cr = requireActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {

            while (cur.moveToNext()) {
                 String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            phoneNo = phoneNo.replaceAll(" ", "");
                            if (phoneNo.length() > 10) {
                                Mylogger.getInstance().printLog(TAG, "greter 10 : " + phoneNo);
                                phoneNo = phoneNo.substring(phoneNo.length() - 10);
                            }

                            if (phoneNo.length() == 10) {
                                isPhoneRegistered(phoneNo, name);
                            }

                        }
                    }
                    Objects.requireNonNull(pCur).close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

    }

    public void isPhoneRegistered(final String phone, final String name) {

        myDbRef.child(Constants.TABLE_USERS).orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = new UserModel();
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    userModel = user.getValue(UserModel.class);
                }

                if (userModel != null) {
                    if (userModel.getUserId() != null) {

                        ContactModel contactModel = new ContactModel();
                        contactModel.setId(getNextIdForContact());
                        contactModel.setName(name);
                        contactModel.setNumber(phone);
                        contactModel.setUserId(userModel.getUserId());
                        contactModel.setSelected(false);

                        if (!contactModel.getNumber().equals(sessionManager.getPhoneNumber())) {//can't add own number
                            RealmController.getInstance().addRegisteredContact(contactModel);
                        }

                    } else {
                        //not registered
                        Mylogger.getInstance().printLog(TAG, "is Not Registered : " + name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

    public int getNextIdForBlockedUser() {
        try {
            Number number = RealmController.getInstance().getRealm().where(BlockContactModel.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }
}