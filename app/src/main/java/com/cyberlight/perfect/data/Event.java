package com.cyberlight.perfect.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.cyberlight.perfect.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "tb_event")
public class Event {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id", typeAffinity = ColumnInfo.INTEGER)
    public int eventId;

    @ColumnInfo(name = "title", typeAffinity = ColumnInfo.TEXT)
    public String title;

    @ColumnInfo(name = "first_start", typeAffinity = ColumnInfo.INTEGER)
    public long firstStart;

    @ColumnInfo(name = "duration", typeAffinity = ColumnInfo.INTEGER)
    public long duration;

    @ColumnInfo(name = "repeat", typeAffinity = ColumnInfo.INTEGER)
    public long repeat;

    @ColumnInfo(name = "description", typeAffinity = ColumnInfo.TEXT)
    public String description;

    // 提供Room使用的构造方法
    public Event(int eventId, String title, long firstStart, long duration, long repeat, String description) {
        this.eventId = eventId;
        this.title = title;
        this.firstStart = firstStart;
        this.duration = duration;
        this.repeat = repeat;
        this.description = description;
    }

    @Ignore
    public Event(String title, long firstStart, long duration, long repeat, String description) {
        this.title = title;
        this.firstStart = firstStart;
        this.duration = duration;
        this.repeat = repeat;
        this.description = description;
    }

    /**
     * 获取事件在指定时间已经发生的次数
     *
     * @param specTime 指定时间(ms)
     * @return 事件在指定时间已经发生的次数
     */
    public int getEventOccurNum(long specTime) {
        int occurNum;

        if (specTime < firstStart) {
            occurNum = 0;
        } else {
            occurNum = (int) ((specTime - firstStart) / repeat + 1);
        }
        return occurNum;
    }

    /**
     * 获取该事件在指定时间之后最近一次具体事件
     *
     * @param specTime 指定时间(ms)
     * @return 该事件在指定时间之后最近一次具体事件
     */
    public SpecificEvent getNextSpecEvent(long specTime) {
        int occurNum = getEventOccurNum(specTime);
        long nextStart = firstStart + getEventOccurNum(specTime) * repeat;
        return new SpecificEvent(this, nextStart, occurNum + 1);
    }

    /**
     * 判断事件与指定事件时间是否冲突
     *
     * @param event 指定事件
     * @return 有冲突返回true, 无冲突返回false
     */
    public boolean isTimeConflictWith(Event event) {
        // 计算两事件interval的最大公约数
        long intervalGCD = MathUtil.computeGCD(this.repeat, event.repeat);
        // 把两事件按事件时间跨度中点分成更早的和更晚的
        Event early, later;
        long earlyMidTime, laterMidTime;
        if ((this.firstStart * 2 + this.duration) / 2 < (event.firstStart * 2 + event.duration) / 2) {
            early = this;
            later = event;
            earlyMidTime = (this.firstStart * 2 + this.duration) / 2;
            laterMidTime = (event.firstStart * 2 + event.duration) / 2;
        } else {
            early = event;
            later = this;
            earlyMidTime = (event.firstStart * 2 + event.duration) / 2;
            laterMidTime = (this.firstStart * 2 + this.duration) / 2;
        }
        // 把更早的事件向更晚的事件以intervalGCD为单位平移距离，平移到两事件时间跨度中点距离最近
        long translationTimes = (laterMidTime - earlyMidTime) / intervalGCD;
        long distance = laterMidTime - (earlyMidTime + intervalGCD * translationTimes);
        if (Math.abs(laterMidTime - (earlyMidTime + intervalGCD * (translationTimes + 1)))
                < distance) {
            translationTimes++;
        }
        long earlyStartNearest = early.firstStart + intervalGCD * translationTimes;
        // 判断两事件距离最近时是否有时间冲突
        return !(earlyStartNearest + early.duration - 1 < later.firstStart
                || later.firstStart + later.duration - 1 < earlyStartNearest);
    }

    /**
     * 获取指定时间段内该事件要发生的具体事件
     *
     * @param startTime 时间段起始时间(ms)
     * @param endTime   时间段结束时间(ms)
     * @return 包含所有指定时间段内发生的具体事件的列表
     */
    public List<SpecificEvent> getSpecEventsDuring(long startTime, long endTime) {
        List<SpecificEvent> specificEvents = new ArrayList<>();
        int occurNum = getEventOccurNum(startTime);
        if (occurNum == 0) {
            occurNum++;
        }
        long specStart = firstStart + (occurNum - 1) * repeat;
        while (specStart <= endTime) {
            if (specStart + duration - 1 >= startTime) {
                SpecificEvent specificEvent = new SpecificEvent(this, specStart, occurNum);
                specificEvents.add(specificEvent);
            }
            occurNum++;
            specStart += repeat;
        }
        return specificEvents;
    }
}