package com.cyberlight.perfect.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.perfect.R;

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

    private String title;
    private String message;
    private String positiveText;
    private String negativeText;
    private String requestKey;

    public ConfirmDialogFragment() {
    }

    public static ConfirmDialogFragment newInstance(String title,
                                                    String message,
                                                    String positiveText,
                                                    String negativeText,
                                                    String requestKey) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CONFIRM_TITLE_KEY, title);
        bundle.putString(CONFIRM_CONTENT_KEY, message);
        bundle.putString(CONFIRM_POSITIVE_TEXT_KEY, positiveText);
        bundle.putString(CONFIRM_NEGATIVE_TEXT_KEY, negativeText);
        bundle.putString(CONFIRM_REQUEST_KEY, requestKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString(CONFIRM_TITLE_KEY);
            message = bundle.getString(CONFIRM_CONTENT_KEY);
            positiveText = bundle.getString(CONFIRM_POSITIVE_TEXT_KEY);
            negativeText = bundle.getString(CONFIRM_NEGATIVE_TEXT_KEY);
            requestKey = bundle.getString(CONFIRM_REQUEST_KEY);
        }
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(CONFIRM_TITLE_KEY);
            message = savedInstanceState.getString(CONFIRM_CONTENT_KEY);
            positiveText = savedInstanceState.getString(CONFIRM_POSITIVE_TEXT_KEY);
            negativeText = savedInstanceState.getString(CONFIRM_NEGATIVE_TEXT_KEY);
            requestKey = savedInstanceState.getString(CONFIRM_REQUEST_KEY);
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_confirm, null);
        TextView mTitleTv = view.findViewById(R.id.dialog_confirm_title_tv);
        TextView mContentTv = view.findViewById(R.id.dialog_confirm_content_tv);
        TextView mNegativeTv = view.findViewById(R.id.dialog_confirm_negative_tv);
        TextView mPositiveTv = view.findViewById(R.id.dialog_confirm_positive_tv);
        mTitleTv.setText(title);
        mContentTv.setText(message);
        mPositiveTv.setText(positiveText);
        mPositiveTv.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt(CONFIRM_WHICH_KEY, CONFIRM_POSITIVE);
            getParentFragmentManager().setFragmentResult(requestKey, result);
            dismiss();
        });
        mNegativeTv.setText(negativeText);
        mNegativeTv.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt(CONFIRM_WHICH_KEY, CONFIRM_NEGATIVE);
            getParentFragmentManager().setFragmentResult(requestKey, result);
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
        outState.putString(CONFIRM_TITLE_KEY, title);
        outState.putString(CONFIRM_CONTENT_KEY, message);
        outState.putString(CONFIRM_POSITIVE_TEXT_KEY, positiveText);
        outState.putString(CONFIRM_NEGATIVE_TEXT_KEY, negativeText);
        outState.putString(CONFIRM_REQUEST_KEY, requestKey);
        super.onSaveInstanceState(outState);
    }

}