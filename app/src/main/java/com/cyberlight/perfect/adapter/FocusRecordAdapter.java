package com.cyberlight.perfect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.data.FocusRecord;
import com.cyberlight.perfect.util.DateTimeFormatUtil;

import java.util.List;

public class FocusRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_RECORD = 0;
    private static final int TYPE_NO_RECORD = 1;
    private static final int TYPE_INDICATOR = 2;

    private final Context mContext;
    private final List<FocusRecord> mFocusRecords;

    public FocusRecordAdapter(Context context, List<FocusRecord> focusRecords) {
        mContext = context;
        mFocusRecords = focusRecords;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_RECORD) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_focus_record, parent, false);
            return new RecordViewHolder(v);
        } else if (viewType == TYPE_NO_RECORD) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_no_focus_record, parent, false);
            return new NoRecordViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_focus_record_indicator, parent, false);
            return new IndicatorViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecordViewHolder) {
            RecordViewHolder recordViewHolder = (RecordViewHolder) holder;
            FocusRecord focusRecord = mFocusRecords.get(position - 1);
            recordViewHolder.mCompletionTimeTv.setText(
                    DateTimeFormatUtil.getNeatDateTime(focusRecord.completionTime));
            recordViewHolder.mFocusDurationTv.setText(
                    mContext.getString(R.string.main_focus_duration,
                            focusRecord.focusDuration / 60000));
        }
    }

    @Override
    public int getItemCount() {
        return mFocusRecords.size() > 0 ? mFocusRecords.size() + 1 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mFocusRecords.size() > 0)// 有专注记录
            // 第一行是指示文字，第一行之后是一条条专注记录
            return position == 0 ? TYPE_INDICATOR : TYPE_RECORD;
        else
            // 没有专注记录
            return TYPE_NO_RECORD;
    }

    private static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView mCompletionTimeTv;
        private final TextView mFocusDurationTv;

        public RecordViewHolder(View v) {
            super(v);
            mCompletionTimeTv = v.findViewById(R.id.rv_focus_record_completion_time_tv);
            mFocusDurationTv = v.findViewById(R.id.rv_focus_record_focus_duration_tv);
        }
    }

    private static class NoRecordViewHolder extends RecyclerView.ViewHolder {
        public NoRecordViewHolder(View v) {
            super(v);
        }
    }

    private static class IndicatorViewHolder extends RecyclerView.ViewHolder {
        public IndicatorViewHolder(View v) {
            super(v);
        }
    }
}
