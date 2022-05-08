package com.cyberlight.perfect.model;

import androidx.annotation.NonNull;

public class Plan {
    public final int planId;
    public final String planContent;
    public final int targetNum;

    public Plan(int planId, String planContent, int targetNum) {
        this.planId = planId;
        this.planContent = planContent;
        this.targetNum = targetNum;
    }

    @NonNull
    @Override
    public String toString() {
        return "Plan{" +
                "planId=" + planId +
                ", planContent='" + planContent + '\'' +
                ", targetNum=" + targetNum +
                '}';
    }
}