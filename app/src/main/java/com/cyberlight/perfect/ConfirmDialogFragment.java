package com.cyberlight.perfect;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ConfirmDialogFragment extends DialogFragment {
    public static final String TAG = "ConfirmDialogFragment";

    public static final int CONFIRM_POSITIVE = 1;
    public static final int CONFIRM_NEGATIVE = 2;
    public static final String CONFIRM_WHICH_KEY = "confirm_which_key";

    private static final String CONFIRM_TITLE_KEY = "confirm_title_key";
    private static final String CONFIRM_CONTENT_KEY = "confirm_content_key";
    private static final String CONFIRM_POSITIVE_TEXT_KEY = "confirm_positive_text_key";
    private static final String CONFIRM_NEGATIVE_TEXT_KEY = "confirm_negative_text_key";
    private static final String CONFIRM_REQUEST_KEY = "confirm_request_key";

    private String mTitle;
    private String mContent;
    private String mPositiveText;
    private String mNegativeText;
    private String mRequestKey;

    public ConfirmDialogFragment() {
    }

    public static ConfirmDialogFragment newInstance(String requestKey,
                                                    String title,
                                                    String content,
                                                    String positiveText,
                                                    String negativeText) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CONFIRM_REQUEST_KEY, requestKey);
        bundle.putString(CONFIRM_TITLE_KEY, title);
        bundle.putString(CONFIRM_CONTENT_KEY, content);
        bundle.putString(CONFIRM_POSITIVE_TEXT_KEY, positiveText);
        bundle.putString(CONFIRM_NEGATIVE_TEXT_KEY, negativeText);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mRequestKey = bundle.getString(CONFIRM_REQUEST_KEY);
            mTitle = bundle.getString(CONFIRM_TITLE_KEY);
            mContent = bundle.getString(CONFIRM_CONTENT_KEY);
            mPositiveText = bundle.getString(CONFIRM_POSITIVE_TEXT_KEY);
            mNegativeText = bundle.getString(CONFIRM_NEGATIVE_TEXT_KEY);
        }
        if (savedInstanceState != null) {
            mRequestKey = savedInstanceState.getString(CONFIRM_REQUEST_KEY);
            mTitle = savedInstanceState.getString(CONFIRM_TITLE_KEY);
            mContent = savedInstanceState.getString(CONFIRM_CONTENT_KEY);
            mPositiveText = savedInstanceState.getString(CONFIRM_POSITIVE_TEXT_KEY);
            mNegativeText = savedInstanceState.getString(CONFIRM_NEGATIVE_TEXT_KEY);
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_confirm, null);
        TextView titleTv = view.findViewById(R.id.dialog_confirm_title_tv);
        TextView contentTv = view.findViewById(R.id.dialog_confirm_content_tv);
        TextView negativeTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        TextView positiveTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        titleTv.setText(mTitle);
        contentTv.setText(mContent);
        positiveTv.setText(mPositiveText);
        negativeTv.setText(mNegativeText);
        positiveTv.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt(CONFIRM_WHICH_KEY, CONFIRM_POSITIVE);
            getParentFragmentManager().setFragmentResult(mRequestKey, result);
            dismiss();
        });
        negativeTv.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt(CONFIRM_WHICH_KEY, CONFIRM_NEGATIVE);
            getParentFragmentManager().setFragmentResult(mRequestKey, result);
            dismiss();
        });
        // 设置对话框
        Dialog dialog = new Dialog(getContext(), R.style.FadeAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(CONFIRM_REQUEST_KEY, mRequestKey);
        outState.putString(CONFIRM_TITLE_KEY, mTitle);
        outState.putString(CONFIRM_CONTENT_KEY, mContent);
        outState.putString(CONFIRM_POSITIVE_TEXT_KEY, mPositiveText);
        outState.putString(CONFIRM_NEGATIVE_TEXT_KEY, mNegativeText);
        super.onSaveInstanceState(outState);
    }

}