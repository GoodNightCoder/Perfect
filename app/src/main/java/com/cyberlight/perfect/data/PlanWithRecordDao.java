package com.cyberlight.perfect.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;


import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface PlanWithRecordDao {

    // fixme:应考虑计划没有匹配的记录的情况
    @Query("SELECT * FROM tb_plan LEFT OUTER JOIN tb_plan_record ON "
            + "tb_plan.plan_id = tb_plan_record.plan_id AND "
            + "date_str = :date")
    LiveData<List<PlanWithRecord>> loadPlanWithRecordsLiveDataByDate(LocalDate date);

    @Query("SELECT * FROM tb_plan LEFT OUTER JOIN tb_plan_record ON "
            + "tb_plan.plan_id = tb_plan_record.plan_id AND "
            + "date_str = :date")
    List<PlanWithRecord> loadPlanWithRecordsByDateSync(LocalDate date);

    @Query("SELECT * FROM tb_plan LEFT OUTER JOIN tb_plan_record ON "
            + "tb_plan.plan_id = tb_plan_record.plan_id AND "
            + "date_str = :date AND tb_plan.plan_id = :planId")
    ListenableFuture<PlanWithRecord> loadPlanWithRecordByDateAndPlanId(LocalDate date, int planId);
}

