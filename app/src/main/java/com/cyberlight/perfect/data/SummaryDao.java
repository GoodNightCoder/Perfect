package com.cyberlight.perfect.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;

@Dao
public interface SummaryDao {

    @Insert
    ListenableFuture<Void> insertSummaries(Summary... summaries);

    @Delete
    ListenableFuture<Void> deleteSummaries(Summary... summaries);

    @Query("DELETE FROM tb_summary")
    ListenableFuture<Void> deleteAllSummaries();

    @Query("SELECT * FROM tb_summary WHERE date_str LIKE :date")
    LiveData<Summary> loadSummaryLiveDataByDate(LocalDate date);

    @Query("SELECT * FROM tb_summary WHERE date_str LIKE :date")
    ListenableFuture<Summary> loadSummaryByDate(LocalDate date);
}