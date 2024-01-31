package net.manish.shopping.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.manish.shopping.R;
import net.manish.shopping.model.TodoModel;

import java.util.List;

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ViewHolder> {

    private final List<TodoModel> dataset;

    public ReminderListAdapter(List<TodoModel> dataset) {
        this.dataset = dataset;
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
                .inflate(R.layout.item_dialog_reminder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvTitle.setText(dataset.get(position).getTitle());
        holder.tvNote.setText(dataset.get(position).getNote());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvNote;
        LinearLayout lnrRoot;

        ViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_title);
            tvNote = itemView.findViewById(R.id.tv_note);

            lnrRoot = itemView.findViewById(R.id.lnr_root);

            lnrRoot.setOnClickListener(v -> {
            });
        }
    }
}