package com.cyberlight.perfect.test;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.cyberlight.perfect.ui.MainActivity;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbUtil;

import java.time.LocalDate;
import java.util.Random;

public class DebugUtil {
    private static final String TAG = "perfect_debug";

    // 测试模式指示常量
    public static boolean enableTestMode = true;

    /**
     * 只调用Log
     *
     * @param text 调试文本
     */
    public static void log(String text) {
        Log.d(TAG, text);
    }

    /**
     * Log和Toast一起用
     *
     * @param context 可用Context对象
     * @param text    调试文本
     */
    public static void log(Context context, String text) {
        Log.d(TAG, text);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }


    /**
     * 为调试模式切换view设置5秒长按监听
     *
     * @param context 可用Context对象
     * @param view    调试模式切换view
     * @param handler 可用Handler对象
     */
    public static void setToggleView(final Context context, final View view, final Handler handler) {
        final Runnable r = () -> toggleTestMode(context);
        view.setOnTouchListener(new View.OnTouchListener() {

            int mDownX;
            int mDownY;
            final int mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 防止出现ACTION_UP事件被丢弃等情况
                        handler.removeCallbacks(r);
                        mDownX = (int) event.getX();
                        mDownY = (int) event.getY();
                        handler.postDelayed(r, 3000);// 自定义长按时间800ms
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mDownX - event.getX()) > mTouchSlop
                                || Math.abs(mDownY - event.getY()) > mTouchSlop) {
                            // 移动过远则不是长按
                            handler.removeCallbacks(r);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 当用户触摸到事件按钮，但其实是为了在ScrollView中上下滑动或者
                        // 在ViewPager中左右滑动时，父view会传ACTION_CANCEL通知事件
                        // 按钮停止处理手势，所以收到ACTION_CANCEL时必须结束长按
                        handler.removeCallbacks(r);
                }
                return false;
            }
        });
    }

    public static void toggleTestMode(Context context) {
        enableTestMode = !enableTestMode;
        Toast.makeText(context, "Test Mode: " + enableTestMode, Toast.LENGTH_SHORT).show();
    }

    public static void addTestSummary(Context context, LocalDate date) {
        boolean result = DbUtil.addSummary(context,
                (int) (1 + Math.random() * 5),
                "review test data " + DateTimeFormatUtil.getNeatDate(date),
                "memo test data " + DateTimeFormatUtil.getNeatDate(date),
                DateTimeFormatUtil.getNeatDate(date));
        Toast.makeText(context, "addTestSummary: " + result, Toast.LENGTH_SHORT).show();
    }
}
