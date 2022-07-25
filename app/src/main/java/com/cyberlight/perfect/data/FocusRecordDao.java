package com.cyberlight.perfect.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface FocusRecordDao {
    @Insert
    ListenableFuture<Void> insertFocusRecords(FocusRecord... focusRecords);

    @Delete
    ListenableFuture<Void> deleteFocusRecords(FocusRecord... focusRecords);

    @Query("DELETE FROM tb_focus_record")
    ListenableFuture<Void> deleteAllFocusRecords();

    @Query("SELECT * FROM tb_focus_record WHERE completion_time BETWEEN :start AND :end")
    LiveData<List<FocusRecord>> loadAllFocusRecordsLiveDataDuring(long start, long end);

    @Query("SELECT * FROM tb_focus_record WHERE completion_time BETWEEN :start AND :end")
    ListenableFuture<List<FocusRecord>> loadAllFocusRecordsDuring(long start, long end);
}