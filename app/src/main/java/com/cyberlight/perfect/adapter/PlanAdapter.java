package com.cyberlight.perfect.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.data.PlanWithRecord;
import com.cyberlight.perfect.widget.CountableRadioButton;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_PLAN = 0;
    private static final int TYPE_NO_PLAN = 1;
    private static final String TAG = "PlanAdapter";

    private final List<PlanWithRecord> mPlanWithRecords;

    @Nullable
    private final PlanClickCallback mPlanClickCallback;

    public PlanAdapter(List<PlanWithRecord> planWithRecords,
                       @Nullable PlanClickCallback planClickCallback) {
        mPlanWithRecords = planWithRecords;
        mPlanClickCallback = planClickCallback;
    }

    private static class PlanViewHolder extends RecyclerView.ViewHolder {
        private final TextView mContentTv;
        private final CountableRadioButton mCrbtn;

        public PlanViewHolder(View v) {
            super(v);
            mContentTv = v.findViewById(R.id.rv_plan_content_tv);
            mCrbtn = v.findViewById(R.id.rv_plan_crbtn);
//            mCrbtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.d(TAG,"serghuiergiuh");
//                }
//            });
        }
    }

    private static class NoPlanViewHolder extends RecyclerView.ViewHolder {
        public NoPlanViewHolder(View v) {
            super(v);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PLAN) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_plan, parent, false);
            return new PlanViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_no_plan, parent, false);
            return new NoPlanViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlanViewHolder) {
            PlanViewHolder planViewHolder = (PlanViewHolder) holder;
            PlanWithRecord planWithRecord = mPlanWithRecords.get(position);
            planViewHolder.mContentTv.setText(planWithRecord.plan.content);
            planViewHolder.mCrbtn.setMaxCount(planWithRecord.plan.targetNum);
            planViewHolder.mCrbtn.setCount(planWithRecord.completionCount);
            planViewHolder.mCrbtn.setOnClickListener(v -> {
                if (mPlanClickCallback != null) {
                    Log.d(TAG,planWithRecord.toString());
                    mPlanClickCallback.onClick(planWithRecord);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPlanWithRecords.size() > 0 ? mPlanWithRecords.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return mPlanWithRecords.size() > 0 ? TYPE_PLAN : TYPE_NO_PLAN;
    }
}