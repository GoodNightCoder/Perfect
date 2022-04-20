package com.cyberlight.perfect.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ToastUtil {

    // 限制showToast的duration参数取值
    @IntDef({Toast.LENGTH_SHORT, Toast.LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    private static Toast mToast;

    private ToastUtil() {

    }

    public static void showToast(Context context, CharSequence text, @Duration int duration) {
        if (mToast != null) {
            mToast.setText(text);
            mToast.setDuration(duration);
        } else {
            mToast = Toast.makeText(context.getApplicationContext(), text, duration);
        }
        mToast.show();
    }

    public static void showToast(Context context, int resId, @Duration int duration) {
        showToast(context, context.getResources().getText(resId), duration);
    }
}
