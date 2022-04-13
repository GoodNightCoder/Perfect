package com.cyberlight.perfect.ui;

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

    private ImageView thumbUpImg1;
    private ImageView thumbUpImg2;
    private ImageView thumbUpImg3;
    private ImageView thumbUpImg4;
    private ImageView thumbUpImg5;

    private LocalDate date;
    private int rating;

    public SummaryDialogFragment() {
    }

    public static SummaryDialogFragment newInstance() {
        return new SummaryDialogFragment();
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
        ImageView cancelImg = view.findViewById(R.id.summary_cancel_img);
        cancelImg.setOnClickListener(v -> dismiss());
        //初始化5个评分图片
        thumbUpImg1 = view.findViewById(R.id.summary_thumb_up_img1);
        thumbUpImg2 = view.findViewById(R.id.summary_thumb_up_img2);
        thumbUpImg3 = view.findViewById(R.id.summary_thumb_up_img3);
        thumbUpImg4 = view.findViewById(R.id.summary_thumb_up_img4);
        thumbUpImg5 = view.findViewById(R.id.summary_thumb_up_img5);
        setRating(rating);
        thumbUpImg1.setOnClickListener(v -> setRating(1));
        thumbUpImg2.setOnClickListener(v -> setRating(2));
        thumbUpImg3.setOnClickListener(v -> setRating(3));
        thumbUpImg4.setOnClickListener(v -> setRating(4));
        thumbUpImg5.setOnClickListener(v -> setRating(5));
        ImageView confirmImg = view.findViewById(R.id.summary_confirm_img);
        confirmImg.setOnClickListener(v -> {
            EditText summaryTextEt = view.findViewById(R.id.summary_text_et);
            EditText summaryMemoEt = view.findViewById(R.id.summary_memo_et);
            String summaryText = summaryTextEt.getText().toString();
            String summaryMemo = summaryMemoEt.getText().toString();
            if (summaryText.equals("")) {
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
            if (DbUtil.addSummary(context, rating, summaryText, summaryMemo,
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
        ImageView[] imageViews = {thumbUpImg1, thumbUpImg2, thumbUpImg3, thumbUpImg4, thumbUpImg5};
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i].setColorFilter(i < rating ? purpleA50 : grayA50);
        }
    }

}