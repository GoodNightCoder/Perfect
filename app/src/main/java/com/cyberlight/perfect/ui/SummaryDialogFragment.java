package com.cyberlight.perfect.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbUtil;
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

    private LocalDate date;
    private int rating;

    public SummaryDialogFragment() {
    }

    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnDismissListener = (DialogInterface.OnDismissListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity()
                    + " must implement DialogInterface.OnDismissListener");
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        mOnDismissListener.onDismiss(dialog);
        super.onDismiss(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 设置对话框布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_summary, null);

        Context context = getContext();
        if (savedInstanceState != null) {
            rating = savedInstanceState.getInt(SUMMARY_RATING_KEY);
            date = LocalDate.ofEpochDay(savedInstanceState.getLong(SUMMARY_DATE_KEY));
        } else {
            rating = 0;
            date = LocalDate.now();
        }
        //为关闭图片添加点击监听
        ImageView mCancelIv = view.findViewById(R.id.summary_cancel_iv);
        mCancelIv.setOnClickListener(v -> dismiss());
        //初始化5个评分图片
        mThumbUpIv1 = view.findViewById(R.id.summary_thumb_up_iv1);
        mThumbUpIv2 = view.findViewById(R.id.summary_thumb_up_iv2);
        mThumbUpIv3 = view.findViewById(R.id.summary_thumb_up_iv3);
        mThumbUpIv4 = view.findViewById(R.id.summary_thumb_up_iv4);
        mThumbUpIv5 = view.findViewById(R.id.summary_thumb_up_iv5);
        setRating(rating);
        mThumbUpIv1.setOnClickListener(v -> setRating(1));
        mThumbUpIv2.setOnClickListener(v -> setRating(2));
        mThumbUpIv3.setOnClickListener(v -> setRating(3));
        mThumbUpIv4.setOnClickListener(v -> setRating(4));
        mThumbUpIv5.setOnClickListener(v -> setRating(5));
        ImageView mConfirmIv = view.findViewById(R.id.summary_confirm_iv);
        mConfirmIv.setOnClickListener(v -> {
            EditText mReviewEt = view.findViewById(R.id.summary_review_et);
            EditText mMemoEt = view.findViewById(R.id.summary_memo_et);
            String review = mReviewEt.getText().toString();
            String memo = mMemoEt.getText().toString();
            if (review.equals("")) {
                ToastUtil.showToast(
                        context,
                        getString(R.string.summary_no_text_toast),
                        Toast.LENGTH_SHORT);
                return;
            }
            if (rating == 0) {
                ToastUtil.showToast(
                        context,
                        getString(R.string.summary_no_rating_toast),
                        Toast.LENGTH_SHORT);
                return;
            }
            if (DbUtil.addSummary(context, rating, review, memo,
                    DateTimeFormatUtil.getNeatDate(date))) {
                ToastUtil.showToast(
                        context,
                        getString(R.string.summary_success_toast),
                        Toast.LENGTH_SHORT);
            } else {
                ToastUtil.showToast(
                        context,
                        getString(R.string.summary_fail_toast),
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
        outState.putInt(SUMMARY_RATING_KEY, rating);
        outState.putLong(SUMMARY_DATE_KEY, date.toEpochDay());
        super.onSaveInstanceState(outState);
    }

    private void setRating(int rating) {
        this.rating = rating;
        Context context = requireContext();
        int purpleA50 = ContextCompat.getColor(context, R.color.purple_a50);
        int grayA50 = ContextCompat.getColor(context, R.color.gray_a50);
        ImageView[] imageViews = {mThumbUpIv1, mThumbUpIv2, mThumbUpIv3, mThumbUpIv4, mThumbUpIv5};
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i].setColorFilter(i < rating ? purpleA50 : grayA50);
        }
    }

}