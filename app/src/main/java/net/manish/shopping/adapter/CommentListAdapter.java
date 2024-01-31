package net.manish.shopping.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.manish.shopping.R;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.SessionManager;

import java.util.List;
import java.util.Objects;


public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private final List<CommentModel> dataset;
    private final Context context;
    private final DateAndTimePicker dateAndTimePicker;
    private final SessionManager sessionManager;

    public CommentListAdapter(List<CommentModel> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;

        this.dateAndTimePicker = new DateAndTimePicker(context);
        this.sessionManager = new SessionManager(context);

    }

    @Override
    public int getItemCount() {
        if (dataset == null)
            return 0;
        return dataset.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.tvComment.setText(dataset.get(position).getComment());
        String dateString = dateAndTimePicker.extactDateFromDateTimeSecond(dataset.get(position).getUpdatedDate());
        String dateTitle = CommonUtils.getCurrentDateText(dateString, dateAndTimePicker.getCurrentDate());
        holder.tvDateTime.setText(dateTitle + " \u25CF " + dateAndTimePicker.extractTimeFromDateTimeSecond(dataset.get(position).getUpdatedDate()));

        if (dataset.get(position).getUserId().equals(sessionManager.getUserId())) {
            holder.tvName.setText(context.getString(R.string.you));
        } else {

            String name = isContactActiveInCurrentMobile(dataset.get(position).getPhone());

            if (name.equals("")) {
                holder.tvName.setText(dataset.get(position).getSenderName());
            } else {
                holder.tvName.setText(name);
            }

        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvComment, tvDateTime, tvName;
        LinearLayout lnrRoot;

        ViewHolder(View itemView) {
            super(itemView);

            tvComment = itemView.findViewById(R.id.tv_comment);
            tvDateTime = itemView.findViewById(R.id.tv_date);
            tvName = itemView.findViewById(R.id.tv_name);

            lnrRoot = itemView.findViewById(R.id.lnr_root);

            lnrRoot.setOnClickListener(v -> {

            });
        }
    }

    private String isContactActiveInCurrentMobile(String phone) {

        List<ContactModel> contactList = RealmController.getInstance().getAllRegisteredContacts();

        for (int i = 0; i < contactList.size(); i++) {
            if (phone.equals(Objects.requireNonNull(contactList.get(i)).getNumber())) {
                return Objects.requireNonNull(contactList.get(i)).getName();
            }
        }
        return "";
    }

}