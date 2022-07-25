package com.cyberlight.perfect.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface PlanRecordDao {
    @Insert
    ListenableFuture<Void> insertPlanRecords(PlanRecord... planRecords);

    @Delete
    ListenableFuture<Void> deletePlanRecords(PlanRecord... planRecords);

    @Query("DELETE FROM tb_plan_record")
    ListenableFuture<Void> deleteAllPlanRecords();

    @Query("SELECT * FROM tb_plan_record")
    LiveData<List<PlanRecord>> loadAllPlanRecordsLiveData();

    @Query("SELECT * FROM tb_plan_record")
    ListenableFuture<List<PlanRecord>> loadAllPlanRecords();
}
