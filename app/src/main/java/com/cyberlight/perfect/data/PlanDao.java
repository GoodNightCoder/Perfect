package com.cyberlight.perfect.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface PlanDao {

    @Insert
    ListenableFuture<Void> insertPlans(Plan... plans);

    @Delete
    ListenableFuture<Void> deletePlans(Plan... plans);

    @Query("DELETE FROM tb_plan")
    ListenableFuture<Void> deleteAllPlans();

    @Query("SELECT * FROM tb_plan")
    LiveData<List<Plan>> loadAllPlansLiveData();

    @Query("SELECT * FROM tb_plan")
    ListenableFuture<List<Plan>> loadAllPlans();
}