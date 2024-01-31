package net.manish.shopping.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import net.manish.shopping.R;
import net.manish.shopping.listeners.OnItemClickedListener;

import java.util.Objects;


@SuppressLint("ValidFragment")
public class DialogFragmentFinishTask extends DialogFragment {

    private View view;
    private EditText etComment;
    private OnItemClickedListener onItemClickedListener;

    private final String from;

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    @SuppressLint("ValidFragment")
    public DialogFragmentFinishTask(String from) {
        this.from = from;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.dialog_fragment_task_finish_note, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        findViews();

        return view;
    }

    private void findViews() {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(requireActivity().getColor(R.color.colorWhite));

        TextView tvTitle = view.findViewById(R.id.tv_title);
        etComment = view.findViewById(R.id.et_note);
        Button buttonAddComment = view.findViewById(R.id.btn_add_comment);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        Button buttonFinish = view.findViewById(R.id.btn_complete);

        ivClose.setOnClickListener(v -> {
            onItemClickedListener.onTaskFinished(false, "");
            dismiss();
        });

        buttonAddComment.setOnClickListener(v -> {
            if (isValidInput()) {
                onItemClickedListener.onAddComment(etComment.getText().toString());

                dismiss();
            }
        });

        buttonFinish.setOnClickListener(v -> {
            onItemClickedListener.onTaskFinished(true, etComment.getText().toString());
            dismiss();
        });

        //change title and button text if comes from detail screen
        if (from.equals("TodoDetailsActivity")){
            buttonFinish.setVisibility(View.GONE);
            tvTitle.setText(getString(R.string.new_comment));
        }
    }

    private boolean isValidInput() {

        if (etComment.getText().toString().trim().length() == 0) {
            etComment.setError(getResources().getString(R.string.comment_required));
            return false;
        }
        return true;
    }

}
