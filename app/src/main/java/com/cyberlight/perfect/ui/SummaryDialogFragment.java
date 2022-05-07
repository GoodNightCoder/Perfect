package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.OnDataAddedListener;
import com.cyberlight.perfect.util.ToastUtil;

import java.time.LocalDate;

public class SummaryDialogFragment extends DialogFragment {
    public static final String TAG = "SummaryDialogFragment";

    private static final String SUMMARY_DATE_KEY = "summary_date_key";
    private static final String SUMMARY_RATING_KEY = "summary_rating_key";

    private ImageView mThumbUpIv1;
    private ImageView mThumbUpIv2;
    private ImageView mThumbUpIv3;
    private ImageView mThumbUpIv4;
    private ImageView mThumbUpIv5;

    private LocalDate mDate;
    private int mRating;

    public SummaryDialogFragment() {
    }

    private OnDataAddedListener mOnDataAddedListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnDataAddedListener = (OnDataAddedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity()
                    + " must implement OnDataAddedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 设置对话框布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_summary, null);

        Context context = getContext();
        if (savedInstanceState != null) {
            mRating = savedInstanceState.getInt(SUMMARY_RATING_KEY);
            mDate = LocalDate.ofEpochDay(savedInstanceState.getLong(SUMMARY_DATE_KEY));
        } else {
            mRating = 0;
            mDate = LocalDate.now();
        }
        //初始化5个评分图片
        mThumbUpIv1 = view.findViewById(R.id.dialog_summary_thumb_up_iv1);
        mThumbUpIv2 = view.findViewById(R.id.dialog_summary_thumb_up_iv2);
        mThumbUpIv3 = view.findViewById(R.id.dialog_summary_thumb_up_iv3);
        mThumbUpIv4 = view.findViewById(R.id.dialog_summary_thumb_up_iv4);
        mThumbUpIv5 = view.findViewById(R.id.dialog_summary_thumb_up_iv5);
        setRating(mRating);
        mThumbUpIv1.setOnClickListener(v -> setRating(1));
        mThumbUpIv2.setOnClickListener(v -> setRating(2));
        mThumbUpIv3.setOnClickListener(v -> setRating(3));
        mThumbUpIv4.setOnClickListener(v -> setRating(4));
        mThumbUpIv5.setOnClickListener(v -> setRating(5));
        // 设置按钮栏
        TextView dialogTitleTv = view.findViewById(R.id.dialog_action_bar_title_tv);
        dialogTitleTv.setText(R.string.summary_dialog_title);
        ImageView cancelIv = view.findViewById(R.id.dialog_action_bar_cancel_iv);
        cancelIv.setOnClickListener(v -> dismiss());
        ImageView confirmIv = view.findViewById(R.id.dialog_action_bar_confirm_iv);
        confirmIv.setOnClickListener(v -> {
            EditText reviewEt = view.findViewById(R.id.dialog_summary_review_et);
            EditText memoEt = view.findViewById(R.id.dialog_summary_memo_et);
            String review = reviewEt.getText().toString();
            String memo = memoEt.getText().toString();
            if (review.equals("")) {
                ToastUtil.showToast(context,
                        R.string.summary_no_review_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            if (mRating == 0) {
                ToastUtil.showToast(context,
                        R.string.summary_no_rating_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            if (DbUtil.addSummary(context, mRating, review, memo,
                    DateTimeFormatUtil.getNeatDate(mDate))) {
                ToastUtil.showToast(context,
                        R.string.summary_success_toast,
                        Toast.LENGTH_SHORT);
                if (mOnDataAddedListener != null)
                    mOnDataAddedListener.onDataAdded(TAG);
            } else {
                ToastUtil.showToast(context,
                        R.string.summary_fail_toast,
                        Toast.LENGTH_SHORT);
            }
            dismiss();
        });
        // 设置对话框
        Dialog dialog = new Dialog(context, R.style.SlideBottomAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 靠近底部
            lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //保存对话框状态
        outState.putInt(SUMMARY_RATING_KEY, mRating);
        outState.putLong(SUMMARY_DATE_KEY, mDate.toEpochDay());
        super.onSaveInstanceState(outState);
    }

    private void setRating(int rating) {
        this.mRating = rating;
        Context context = requireContext();
        int purpleA50 = ContextCompat.getColor(context, R.color.purple_a50);
        int grayA50 = ContextCompat.getColor(context, R.color.gray_a50);
        ImageView[] imageViews = {mThumbUpIv1, mThumbUpIv2, mThumbUpIv3, mThumbUpIv4, mThumbUpIv5};
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i].setColorFilter(i < rating ? purpleA50 : grayA50);
        }
    }

}