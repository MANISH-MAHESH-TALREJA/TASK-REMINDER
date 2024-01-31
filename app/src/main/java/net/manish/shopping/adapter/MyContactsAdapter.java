package net.manish.shopping.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.manish.shopping.R;
import net.manish.shopping.model.BlockContactModel;

import java.util.ArrayList;
import java.util.List;

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.ViewHolder>
        implements SectionIndexer {

    private final List<BlockContactModel> dataset;
    private ArrayList<Integer> mSectionPositions;
    private final Context context;

    public MyContactsAdapter(List<BlockContactModel> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;
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
                .inflate(R.layout.item_my_contacts, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        holder.tvName.setText(dataset.get(position).getName());
        holder.tvPhoneNumber.setText(dataset.get(position).getNumber());

        if (dataset.get(position).isBlocked()){
            holder.btnBlock.setText(context.getString(R.string.unblock));
        }

        holder.btnBlock.setOnClickListener(v -> {
        });
    }


    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = dataset.size(); i < size; i++) {
            String section = String.valueOf(dataset.get(i).getName().charAt(0)).toUpperCase();
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhoneNumber;
        Button btnBlock;
        RelativeLayout rltvRoot;

        ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone);
            btnBlock = itemView.findViewById(R.id.btn_block);
            rltvRoot = itemView.findViewById(R.id.rltv_root);

            rltvRoot.setOnClickListener(v -> {
            });
        }
    }

}