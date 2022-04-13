package com.cyberlight.perfect.util;

import android.content.Context;
import android.widget.Toast;

public final class ToastUtil {

    private static Toast mToast;

    private ToastUtil() {

    }

    public static void showToast(Context context, CharSequence text, int duration) {
        if (mToast != null) {
            mToast.setText(text);
            mToast.setDuration(duration);
        } else {
            mToast = Toast.makeText(context.getApplicationContext(), text, duration);
        }
        mToast.show();
    }
}
