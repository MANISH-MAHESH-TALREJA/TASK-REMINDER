package net.manish.shopping.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import net.manish.shopping.R;
import net.manish.shopping.utils.Constants;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<String> dataSet;
    private final Context mContext;
    private final String comeFrom;

    public ImageListAdapter(List<String> data, Context context, String comeFrom) {
        this.dataSet = data;
        this.mContext = context;
        this.comeFrom = comeFrom;
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) == null)
            return Constants.TYPE_ADD_PHOTO;
        else {
            return Constants.TYPE_1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {

            case Constants.TYPE_1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_list, parent, false);
                return new ViewHolderType1(view);
            case Constants.TYPE_ADD_PHOTO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_image, parent, false);
                return new ViewHolderAddPhoto(view);

        }
        //noinspection ConstantConditions
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int listPosition) {

        String object = dataSet.get(listPosition);
        if (object != null) {

            StorageReference mStorage = FirebaseStorage.getInstance().getReference();
            switch (comeFrom) {
                case Constants.SCREEN_CREATE_NEW_TODO_ACTIVITY:
                    popupImageFromContentUri(holder, dataSet.get(listPosition));
                    break;
                case Constants.SCREEN_TODO_DETAILS_ACTIVITY:
                    Glide.with(mContext)
                            .load(dataSet.get(listPosition))
                            .into(((ViewHolderType1) holder).ivImage);
                    ((ViewHolderType1) holder).ivRemoveImage.setVisibility(View.GONE);
                    break;
                case Constants.SCREEN_EDIT_TODO_ACTIVITY:

                    if (dataSet.get(listPosition).contains("content:")) {
                        popupImageFromContentUri(holder, dataSet.get(listPosition));
                    } else {
                        Glide.with(mContext)
                                .load(dataSet.get(listPosition))
                                .into(((ViewHolderType1) holder).ivImage);
                    }
                    break;
            }
        }
    }

    private void popupImageFromContentUri(RecyclerView.ViewHolder holder, String imagePath) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(imagePath));
            ((ViewHolderType1) holder).ivImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    static class ViewHolderAddPhoto extends RecyclerView.ViewHolder {

        CircleImageView ivAddPhoto;

        ViewHolderAddPhoto(View itemView) {
            super(itemView);

            ivAddPhoto = itemView.findViewById(R.id.iv_add_image);

            ivAddPhoto.setOnClickListener(v -> {
            });
        }
    }

    static class ViewHolderType1 extends RecyclerView.ViewHolder {

        ImageView ivRemoveImage;
        CircleImageView ivImage;
        RelativeLayout rltvRoot;

        ViewHolderType1(View itemView) {
            super(itemView);

            ivRemoveImage = itemView.findViewById(R.id.iv_remove_image);
            ivImage = itemView.findViewById(R.id.iv_image);
            rltvRoot = itemView.findViewById(R.id.rltv_root);

            ivRemoveImage.setOnClickListener(v -> {
            });
        }
    }
}
