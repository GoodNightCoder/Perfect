package com.cyberlight.perfect.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface EventDao {
    /*
    @Insert注解的DAO方法的返回值可以是插入行的rowId(long类型)
    具体返回值类型如下：
    1. 插入单行数据：long、Long、ListenableFuture<Long>
    2. 插入多行数据：long[]、Long[]、List<Long>、ListenableFuture<List<Long>>
    (其中ListenableFuture用于异步执行)

    @Delete注解的DAO方法可以返回成功删除行的数量(int类型)

    @Update注解的DAO方法可以返回成功更新行的数量(int类型)

    @Query注解的DAO方法可以返回简单对象，只要查询的列能对应上
    简单对象中被@ColumnInfo注解的字段
     */
    @Insert
    ListenableFuture<Void> insertEvents(Event... events);

    @Delete
    ListenableFuture<Void> deleteEvents(Event... events);

    @Query("DELETE FROM tb_event")
    ListenableFuture<Void> deleteAllEvents();

    @Query("SELECT * FROM tb_event")
    LiveData<List<Event>> loadAllEventsLiveData();

    @Query("SELECT * FROM tb_event")
    ListenableFuture<List<Event>> loadAllEvents();
}