package net.manish.shopping.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.manish.shopping.R;
import net.manish.shopping.adapter.AssignToListAdapter;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.listeners.OnItemClickedListener;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.model.alphabetitem.AlphabetItem;
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

public class AssignToActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private final String TAG = AssignToActivity.this.getClass().getName();

    private IndexFastScrollRecyclerView mRecyclerView;
    private TextView textViewNDF;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<ContactModel> mainContactList = new ArrayList<>();
    public static List<ContactModel> contactList = new ArrayList<>();
    public static List<ContactModel> selectedList = new ArrayList<>();
    public static OnItemClickedListener onItemClickedListener;
    private AssignToListAdapter adapter;

    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;

    public static void setOnItemClickedListener(OnItemClickedListener listener)
    {
        onItemClickedListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_to);

        networkConnectivity = new NetworkConnectivity(this);
        sessionManager = new SessionManager(this);

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        mainContactList.clear();
        mainContactList.addAll(RealmController.getInstance().getAllRegisteredContacts());

        findViews();
        setupRecyclerView();
        if (mainContactList.size() == 0)
        {
            if (networkConnectivity.isNetworkAvailable())
            {
                if (AskPermition.getInstance(AssignToActivity.this).canReadContacts())
                {
                    new GetContacts().execute();
                }
            }
        }
        else
        {
            refreshList();
        }
    }

    private void findViews()
    {

        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        setToolbar();

        mRecyclerView = findViewById(R.id.fast_scroller_recycler);
        textViewNDF = findViewById(R.id.tv_ndf);

        Button buttonOk = findViewById(R.id.btn_add_comment);
        buttonOk.setOnClickListener(v ->
        {
            hideKeyboard();
            onItemClickedListener.onItemClicked(selectedList);
            finish();
        });

    }

    private void hideKeyboard()
    {
        try
        {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void initialiseData()
    {

        //Alphabet fast scroller data
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<AlphabetItem> mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < contactList.size(); i++)
        {
            String name = contactList.get(i).getName();
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word))
            {
                strAlphabets.add(word);
                mAlphabetItems.add(new AlphabetItem(i, word, false));
            }
        }
    }

    private void setupRecyclerView()
    {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(AssignToActivity.this));
        adapter = new AssignToListAdapter(contactList);
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
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(AssignToActivity.this, mRecyclerView, new RecyclerViewClickListener()
        {
            @Override
            public void onClick(final View view, final int position)
            {

                AppCompatCheckBox checkBox = view.findViewById(R.id.checkbox);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                {
                    contactList.get(position).setSelected(isChecked);
                    if (isChecked)
                    {
                        selectedList.add(contactList.get(position));
                    }
                    else
                    {
                        removeNumberFromSelectedList(position);
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position)
            {

            }
        }));

    }

    private void removeNumberFromSelectedList(int position)
    {

        for (int i = 0; i < selectedList.size(); i++)
        {
            if (selectedList.get(i).getNumber().equals(contactList.get(position).getNumber()))
            {
                //noinspection SuspiciousListRemoveInLoop
                selectedList.remove(i);
            }
        }
    }

    private void sortDataAlphabeticWise()
    {
        //sort data alphabetic wise
        contactList.sort(Comparator.comparing(ContactModel::getName));
    }

    private void setToolbar()
    {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.assign_to));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void hideRecyclerView()
    {
        if (contactList.size() == 0)
        {
            mRecyclerView.setVisibility(View.GONE);
            textViewNDF.setVisibility(View.VISIBLE);
        }
        else
        {
            mRecyclerView.setVisibility(View.VISIBLE);
            textViewNDF.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        @SuppressWarnings("deprecation")
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                filterByNameOrNumber(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                filterByNameOrNumber(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            onBackPressed();
        }
        if (id == R.id.action_refresh)
        {

            if (networkConnectivity.isNetworkAvailable())
            {
                if (AskPermition.getInstance(AssignToActivity.this).canReadContacts())
                {
                    new GetContacts().execute();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void filterByNameOrNumber(String query)
    {

        contactList.clear();
        for (int i = 0; i < mainContactList.size(); i++)
        {
            if (mainContactList.get(i).getName().toLowerCase().contains(query.toLowerCase())
                    || mainContactList.get(i).getNumber().contains(query))
            {

                contactList.add(mainContactList.get(i));
            }
        }

        refreshList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshList()
    {

        updateListWithSelectedList();
        sortDataAlphabeticWise();
        initialiseData();

        adapter.notifyDataSetChanged();
        hideRecyclerView();
    }

    private void updateListWithSelectedList()
    {

        contactList.clear();

        for (int i = 0; i < mainContactList.size(); i++)
        {

            ContactModel contactModel = new ContactModel();
            boolean isSelected = false;

            if (selectedList.size() > 0)
            {
                for (int j = 0; j < selectedList.size(); j++)
                {

                    if (selectedList.get(j).getUserId().equals(mainContactList.get(i).getUserId()))
                    {
                        isSelected = selectedList.get(j).isSelected();
                    }
                }
            }

            contactModel.setId(mainContactList.get(i).getId());
            contactModel.setUserId(mainContactList.get(i).getUserId());
            contactModel.setName(mainContactList.get(i).getName());
            contactModel.setNumber(mainContactList.get(i).getNumber());
            contactModel.setSelected(isSelected);

            contactList.add(contactModel);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetContacts extends AsyncTask<Void, Void, Void>
    {

        public GetContacts()
        {
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);

        }

        @Override
        protected Void doInBackground(Void... voids)
        {

            getContactList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);

            mainContactList.clear();
            mainContactList.addAll(RealmController.getInstance().getAllRegisteredContacts());

            refreshList();
        }
    }

    @SuppressLint("Range")
    private void getContactList()
    {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0)
        {

            while (cur.moveToNext())
            {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (Objects.requireNonNull(pCur).moveToNext())
                    {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phoneNo = phoneNo.replaceAll(" ", "");
                        phoneNo = phoneNo.replaceAll("[^0-9]", "");
                        if (phoneNo.length() > 10)
                        {
                            phoneNo = phoneNo.substring(phoneNo.length() - 10);
                        }

                        if (phoneNo.length() == 10)
                        {
                            isPhoneRegistered(phoneNo, name);
                        }
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null)
        {
            cur.close();
        }

    }

    public void isPhoneRegistered(final String phone, final String name)
    {

        myDbRef.child(Constants.TABLE_USERS).orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                UserModel userModel = new UserModel();
                for (DataSnapshot user : dataSnapshot.getChildren())
                {
                    userModel = user.getValue(UserModel.class);
                }

                if (userModel != null)
                {
                    if (userModel.getUserId() != null)
                    {
                        Mylogger.getInstance().printLog(TAG, "isRegistered : " + userModel.getUserId());
                        //registered

                        ContactModel contactModel = new ContactModel();
                        contactModel.setId(getNextIdForContact());
                        contactModel.setName(name);
                        contactModel.setNumber(phone);
                        contactModel.setUserId(userModel.getUserId());
                        contactModel.setSelected(false);

                        if (!contactModel.getNumber().equals(sessionManager.getPhoneNumber()))
                        {//can't add own number
                            RealmController.getInstance().addRegisteredContact(contactModel);
                        }

                    }
                    else
                    {
                        //not registered
                        Mylogger.getInstance().printLog(TAG, "is Not Registered : " + name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    public int getNextIdForContact()
    {
        try
        {
            Number number = RealmController.getInstance().getRealm().where(ContactModel.class).max("id");
            if (number != null)
            {
                return number.intValue() + 1;
            }
            else
            {
                return 1;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return 0;
        }
    }
}
