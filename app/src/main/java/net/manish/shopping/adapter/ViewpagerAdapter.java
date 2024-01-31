package net.manish.shopping.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import net.manish.shopping.R;

import java.util.List;

public class ViewpagerAdapter extends PagerAdapter
{
    private final Context context;
    private final List<String> list;

    public ViewpagerAdapter(Context context, List<String> itemList) {
        this.context = context;
        this.list = itemList;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_image_list_pager, container, false);

        ImageView ivPreview = view.findViewById(R.id.iv_preview);
        ImageMatrixTouchHandler imageMatrixTouchHandler = new ImageMatrixTouchHandler(context);
        ivPreview.setOnTouchListener(imageMatrixTouchHandler);
        container.addView(view);


        StorageReference mStorage = FirebaseStorage.getInstance().getReference();

        Glide.with(context)
                .load(mStorage.child(list.get(position)))
                .placeholder(context.getDrawable(R.mipmap.ic_launcher))
                .into(ivPreview);

        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
