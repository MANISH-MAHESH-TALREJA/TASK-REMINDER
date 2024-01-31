package net.manish.shopping.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.manish.shopping.R;
import net.manish.shopping.model.RingtoneModel;
import net.manish.shopping.utils.SessionManager;

import java.util.List;

public class RingtoneListAdapter extends RecyclerView.Adapter<RingtoneListAdapter.ViewHolder> {

    private final List<RingtoneModel> dataset;
    private final Context context;
    private final SessionManager sessionManager;

    public RingtoneListAdapter(List<RingtoneModel> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;

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
                .inflate(R.layout.item_ringtone_list, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        holder.tvTitle.setText(dataset.get(position).getName());

        if (dataset.get(position).isPlaying()) {
            holder.ivPlay.setImageDrawable(context.getDrawable(R.drawable.ic_pause_black_24dp));
        } else {
            holder.ivPlay.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp));
        }

        holder.checkBox.setChecked(sessionManager.getRingtoneUri().equals(dataset.get(position).getUri().toString()));

        holder.ivPlay.setOnClickListener(v -> {
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView ivPlay;
        CheckBox checkBox;
        LinearLayout lnrRoot;

        ViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_name);
            ivPlay = itemView.findViewById(R.id.iv_play);
            lnrRoot = itemView.findViewById(R.id.lnr_root);
            checkBox = itemView.findViewById(R.id.checkbox);

            lnrRoot.setOnClickListener(v -> {
            });
        }
    }

}