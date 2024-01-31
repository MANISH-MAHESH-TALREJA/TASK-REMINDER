package net.manish.shopping.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

import com.google.firebase.database.ChildEventListener;
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
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

public class BlockedContactsFragment extends Fragment {

    private View view;
    private IndexFastScrollRecyclerView mRecyclerView;
    private TextView textViewNDF;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final List<BlockContactModel> blockedList = new ArrayList<>();
    private final List<BlockContactModel> mainList = new ArrayList<>();
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


    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_active_contacts, container, false);

        findViews();
        setupRecyclerView();

        blockedList.clear();
        blockedList.addAll(RealmController.getInstance().getAllBlockedNumber());

        if (blockedList.size() == 0) {
            if (networkConnectivity.isNetworkAvailable()) {
                syncBlockListWithRealTimeD();
            }
        } else {
            getBlockedNumbers();
        }

        setRealTimeDataChangeListener();
        return view;
    }


    private void setRealTimeDataChangeListener() {
        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                BlockContactModel post = dataSnapshot.getValue(BlockContactModel.class);

                RealmController.getInstance().addOrUpdateBlockUser(post);

                getBlockedNumbers();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                BlockContactModel post = dataSnapshot.getValue(BlockContactModel.class);

                RealmController.getInstance().addOrUpdateBlockUser(post);

                getBlockedNumbers();


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                BlockContactModel post = dataSnapshot.getValue(BlockContactModel.class);

                if (post != null) {
                    RealmController.getInstance().removeBlockedNumberByUniqueId(post.getUniqueId());
                }

                getBlockedNumbers();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // Refresh your fragment here
            if (mRecyclerView != null) {
                if (ConnectionChecker.isInternetAvailable(requireActivity())) {
                    syncBlockListWithRealTimeD();
                }else {
                    getBlockedNumbers();
                }
            }
        }
    }

    private void findViews() {

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mRecyclerView = view.findViewById(R.id.fast_scroller_recycler);
        textViewNDF = view.findViewById(R.id.tv_ndf);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (networkConnectivity.isNetworkAvailable()) {
                syncBlockListWithRealTimeD();
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    protected void initialiseData() {

        //Alphabet fast scroller data
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < blockedList.size(); i++) {
            String name = blockedList.get(i).getName();
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
        adapter = new MyContactsAdapter(blockedList, getActivity());
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

        //setup item click lestener
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), mRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(final View view, final int position) {

                Button btnBlock = view.findViewById(R.id.btn_block);
                btnBlock.setOnClickListener(v -> unBlockConfirmationAlert(position));

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    private void unBlockConfirmationAlert(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.unblock_user_msg))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    //delete from firebase
                    if (ConnectionChecker.isInternetAvailable(requireActivity())) {

                        FirebaseRealtimeController.getInstance().unblockUserByFireId(sessionManager.getUserId(), blockedList.get(position).getUniqueId()).setOnCompleteListener(new OnCallCompleteListener() {
                            @Override
                            public void onComplete() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailed() {
                                dialog.dismiss();
                            }
                        });
                    } else {

                        dialog.dismiss();

                        BlockContactModel blockContactModel = new BlockContactModel();

                        blockContactModel.setId(blockedList.get(position).getId());
                        blockContactModel.setName(blockedList.get(position).getName());
                        blockContactModel.setNumber(blockedList.get(position).getNumber());
                        blockContactModel.setBlocked(false);
                        blockContactModel.setUniqueId(blockedList.get(position).getUniqueId());
                        blockContactModel.setUserId(blockedList.get(position).getUserId());

                        blockContactModel.setPendingForSync(true);
                        blockContactModel.setWhatPending(Constants.SYNC_PENDING_FOR_DELETE);

                        RealmController.getInstance().addOrUpdateBlockUser(blockContactModel);

                        getBlockedNumbers();//refresh list
                    }


                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                    dialog.dismiss();
                }).show();
    }

    private void sortDataAlphabeticWise() {
        //sort data alphabetic wise
        blockedList.sort(Comparator.comparing(BlockContactModel::getName));
    }

    private void hideRecyclerView() {
        if (blockedList.size() == 0) {
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

        blockedList.clear();
        for (int i = 0; i < mainList.size(); i++) {
            if (mainList.get(i).getName().toLowerCase().contains(query.toLowerCase())
                    || mainList.get(i).getNumber().contains(query)) {


                blockedList.add(mainList.get(i));
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

    private void getBlockedNumbers() {

        blockedList.clear();
        blockedList.addAll(RealmController.getInstance().getAllBlockedNumber());

        mainList.clear();
        mainList.addAll(blockedList);

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
                getBlockedNumbers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}