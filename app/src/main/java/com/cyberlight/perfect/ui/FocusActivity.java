package com.cyberlight.perfect.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.service.FocusService;
import com.cyberlight.perfect.util.SettingManager;
import com.cyberlight.perfect.util.SharedPrefSettingManager;
import com.cyberlight.perfect.util.ToastUtil;
import com.cyberlight.perfect.widget.CircularProgressView;

@SuppressLint("ClickableViewAccessibility")
public class FocusActivity extends AppCompatActivity {

    private static final String TAG = "FocusActivity";

    // 显示模式
    public static final int DISPLAY_MODES_COUNT = 5;
    public static final int SHOW_TIME_PROGRESS_STATE_EXIT = 0;
    public static final int SHOW_TIME_PROGRESS_STATE = 1;
    public static final int SHOW_TIME_PROGRESS = 2;
    public static final int SHOW_TIME = 3;
    public static final int SHOW_NONE = 4;

    /**
     * 动画时长
     */
    private static final int ANIM_DURATION = 300;

    /**
     * 指示cpv是否是第一次设置progress
     */
    private boolean isFirstUpdate = true;

    /**
     * 正在展示动画时为true
     */
    private boolean disallowAnim = false;

    /**
     * 正在执行动画的view的数量
     */
    private int animatingViewsCount;

    /**
     * 当前显示模式
     */
    private int displayMode = SHOW_TIME_PROGRESS_STATE_EXIT;

    private CircularProgressView mCpv;
    private TextView mExitBtn;
    private TextView mTimeTv;
    private TextView mStateTv;

    private FocusService mService;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            FocusService.FocusServiceBinder binder = (FocusService.FocusServiceBinder) service;
            mService = binder.getService();
            mService.setOnUpdateListener((delayMillis, progress, remainTimeStr, focusStateStr) ->
                    mHandler.post(() -> {
                        mCpv.setProgress(progress, isFirstUpdate ? delayMillis / 2 : delayMillis);
                        mTimeTv.setText(remainTimeStr);
                        mStateTv.setText(focusStateStr);
                        if (isFirstUpdate) {
                            isFirstUpdate = false;
                        }
                    }));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

    private final Handler mHandler = new Handler();
    private final Runnable mAlterDisplayModeRunnable = this::alterDisplayMode;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // android:configChanges="orientation|screenSize"导致activity屏幕方向改变后，
        // activity不会销毁重建，所以必须主动地更新界面布局(用上layout-land)、重新设置各个控件
        setContentView(R.layout.activity_focus);
        // 获取并设置控件
        mTimeTv = findViewById(R.id.focus_time_tv);
        mCpv = findViewById(R.id.focus_cpv);
        mStateTv = findViewById(R.id.focus_state_tv);
        mExitBtn = findViewById(R.id.focus_exit_btn);
        mExitBtn.setOnClickListener(v -> exitFocus());
        ViewGroup mFocusLayout = findViewById(R.id.focus_layout);
        // 为布局添加长按监听
        mFocusLayout.setOnTouchListener(new View.OnTouchListener() {
            int mDownX;
            int mDownY;
            final int mTouchSlop = ViewConfiguration.get(FocusActivity.this).getScaledTouchSlop();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.removeCallbacks(mAlterDisplayModeRunnable);
                        mDownX = (int) event.getX();
                        mDownY = (int) event.getY();
                        mHandler.postDelayed(mAlterDisplayModeRunnable, 800);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mDownX - event.getX()) > mTouchSlop
                                || Math.abs(mDownY - event.getY()) > mTouchSlop) {
                            // 移动过远则不是长按
                            mHandler.removeCallbacks(mAlterDisplayModeRunnable);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mHandler.removeCallbacks(mAlterDisplayModeRunnable);
                }
                return false;
            }
        });
        //重置有必要的变量
        displayMode = SHOW_TIME_PROGRESS_STATE_EXIT;
        disallowAnim = false;
        isFirstUpdate = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        // 获取并设置控件
        mTimeTv = findViewById(R.id.focus_time_tv);
        mCpv = findViewById(R.id.focus_cpv);
        mStateTv = findViewById(R.id.focus_state_tv);
        mExitBtn = findViewById(R.id.focus_exit_btn);
        mExitBtn.setOnClickListener(v -> exitFocus());
        ViewGroup mFocusLayout = findViewById(R.id.focus_layout);
        // 为布局添加长按监听
        mFocusLayout.setOnTouchListener(new View.OnTouchListener() {
            int mDownX;
            int mDownY;
            final int mTouchSlop = ViewConfiguration.get(FocusActivity.this).getScaledTouchSlop();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.removeCallbacks(mAlterDisplayModeRunnable);
                        mDownX = (int) event.getX();
                        mDownY = (int) event.getY();
                        mHandler.postDelayed(mAlterDisplayModeRunnable, 800);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mDownX - event.getX()) > mTouchSlop
                                || Math.abs(mDownY - event.getY()) > mTouchSlop) {
                            // 移动过远则不是长按
                            mHandler.removeCallbacks(mAlterDisplayModeRunnable);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mHandler.removeCallbacks(mAlterDisplayModeRunnable);
                }
                return false;
            }
        });

        // 设置屏幕常亮
        SettingManager settingManager = SharedPrefSettingManager.getInstance(this);
        boolean keepScreenOn = settingManager.getKeepScreenOn();
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // 启动专注服务
        Intent focusServiceIntent = new Intent(this, FocusService.class);
        startService(focusServiceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 绑定服务
        Intent intent = new Intent(this, FocusService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 解绑服务
        mService.removeOnUpdateListener();
        unbindService(mConnection);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 进入粘性沉浸模式
            hideSystemUI();
        }
    }

    /**
     * 淡出动画隐藏单个View
     *
     * @param v 要隐藏的view
     */
    private void fadeOutView(View v) {
        if (!disallowAnim) {
            disallowAnim = true;
            v.animate()
                    .alpha(0f)
                    .setDuration(FocusActivity.ANIM_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.INVISIBLE);
                            disallowAnim = false;
                        }
                    });
        }
    }

    /**
     * 同时进行淡入动画显示多个View
     *
     * @param views 要显示的View数组
     */
    private void fadeInViews(View[] views) {
        if (!disallowAnim) {
            disallowAnim = true;
            animatingViewsCount = views.length;
            for (View v : views) {
                v.setVisibility(View.VISIBLE);
                v.animate()
                        .alpha(1f)
                        .setDuration(FocusActivity.ANIM_DURATION)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animatingViewsCount--;
                                if (animatingViewsCount == 0)
                                    disallowAnim = false;
                            }
                        });
            }
        }
    }

    /**
     * 切换要显示的内容，目前有4种模式
     */
    private void alterDisplayMode() {
        if (!disallowAnim) {
            displayMode = (displayMode + 1) % DISPLAY_MODES_COUNT;
            if (displayMode == SHOW_TIME_PROGRESS_STATE_EXIT) {
                fadeInViews(new View[]{mTimeTv, mCpv, mStateTv, mExitBtn});
            } else if (displayMode == SHOW_TIME_PROGRESS_STATE) {
                fadeOutView(mExitBtn);
            } else if (displayMode == SHOW_TIME_PROGRESS) {
                fadeOutView(mStateTv);
            } else if (displayMode == SHOW_TIME) {
                fadeOutView(mCpv);
            } else if (displayMode == SHOW_NONE) {
                fadeOutView(mTimeTv);
            }
        }
    }

    /**
     * 退出专注模式(结束专注服务，退出Activity)
     */
    private void exitFocus() {
        Intent stopServiceIntent = new Intent(this, FocusService.class);
        stopService(stopServiceIntent);
        ToastUtil.showToast(this, R.string.focus_exit_toast, Toast.LENGTH_SHORT);
        finish();
    }

    /**
     * 启用全屏模式(粘性沉浸模式)
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

}
