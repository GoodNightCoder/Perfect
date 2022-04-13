package com.cyberlight.perfect.model;

import androidx.annotation.NonNull;

public class SpecPlan extends Plan {
    public final String dateStr;
    public final int completionCount;

    public SpecPlan(Plan plan, String dateStr, int completionCount) {
        super(plan.planId, plan.planContent, plan.targetNum);
        this.dateStr = dateStr;
        this.completionCount = completionCount;
    }

    @NonNull
    @Override
    public String toString() {
        return "SpecPlan{" +
                "planId=" + planId +
                ", planContent='" + planContent + '\'' +
                ", targetNum=" + targetNum +
                ", dateStr='" + dateStr + '\'' +
                ", completionCount=" + completionCount +
                '}';
    }
}
