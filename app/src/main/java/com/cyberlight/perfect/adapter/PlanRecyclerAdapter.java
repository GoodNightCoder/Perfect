package com.cyberlight.perfect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.model.SpecPlan;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.widget.CountableRadioButton;

import java.util.List;

public class PlanRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PLAN = 0;
    private static final int TYPE_NO_PLAN = 1;

    private final Context mContext;
    private final List<SpecPlan> specPlans;

    private static class PlanViewHolder extends RecyclerView.ViewHolder {
        private final TextView mContentTv;
        private final CountableRadioButton mCrbtn;

        public PlanViewHolder(View v) {
            super(v);
            mContentTv = v.findViewById(R.id.rv_plan_content_tv);
            mCrbtn = v.findViewById(R.id.rv_plan_crbtn);
        }
    }

    private static class NoPlanViewHolder extends RecyclerView.ViewHolder {
        public NoPlanViewHolder(View v) {
            super(v);
        }
    }


    public PlanRecyclerAdapter(Context context, List<SpecPlan> specPlans) {
        mContext = context;
        this.specPlans = specPlans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PLAN) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_plan, parent, false);
            return new PlanViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_no_plan, parent, false);
            return new NoPlanViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlanViewHolder) {
            PlanViewHolder planViewHolder = (PlanViewHolder) holder;
            SpecPlan specPlan = specPlans.get(position);
            planViewHolder.mContentTv.setText(specPlan.planContent);
            planViewHolder.mCrbtn.initCountAndMaxCount(specPlan.completionCount, specPlan.targetNum);
            planViewHolder.mCrbtn.setOnCountListener(count -> {
                if (DbUtil.getPlanCompletionCountByDate(mContext, specPlan.dateStr, specPlan.planId) != -1) {
                    DbUtil.updatePlanRecord(mContext, specPlan.dateStr, specPlan.planId, count);
                } else {
                    DbUtil.addPlanRecord(mContext, specPlan.dateStr, specPlan.planId, count);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return specPlans.size() > 0 ? specPlans.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return specPlans.size() > 0 ? TYPE_PLAN : TYPE_NO_PLAN;
    }
}