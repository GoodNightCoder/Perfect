package com.cyberlight.perfect.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cyberlight.perfect.util.DateTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private static final String TAG = "Event";
    public final int eventId;
    public final String title;
    public final long start;
    public final long duration;
    public final long interval;

    public Event(int eventId, String title, long start, long duration, long interval) {
        this.eventId = eventId;
        this.title = title;
        this.start = start;
        this.duration = duration;
        this.interval = interval;
    }

    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", title='" + title + '\'' +
                ", start=" + start +
                ", duration=" + duration +
                ", interval=" + interval +
                '}';
    }

    /**
     * 辗转相除法求最大公约数
     *
     * @param a 一个数
     * @param b 另一个数
     * @return 两数的最大公约数
     */
    private long computeGCD(long a, long b) {
        long r;
        while (true) {
            if (a > b) {
                r = a % b;//取余运算符要求符号两边都是整型数
                if (r != 0)
                    a = r;
                else
                    break;
            } else {
                r = b % a;
                if (r != 0)
                    b = r;
                else
                    break;
            }
        }
        return Math.min(a, b);
    }

    /**
     * 获取事件在指定时间已经发生的次数
     *
     * @param specTime 指定时间(ms)
     * @return 事件在指定时间已经发生的次数
     */
    public int getEventOccurNum(long specTime) {
        int occurNum;
        if (specTime < start) {
            occurNum = 0;
        } else {
            occurNum = (int) ((specTime - start) / interval + 1);
        }
        return occurNum;
    }

    /**
     * 获取在指定时间，事件下一次开始时间
     *
     * @param specTime 指定时间(ms)
     * @return 该事件在指定时间之后最近的开始时间
     */
    public long getNextStart(long specTime) {
        return start + getEventOccurNum(specTime) * interval;
    }

    /**
     * 获取指定时间正在进行的具体事件
     *
     * @param specTime 指定时间(ms)
     * @return 指定时间正在进行的具体事件, 若无具体事件正在进行返回null
     */
    public SpecEvent getOnGoingSpecEvent(long specTime) {
        int occurNum = getEventOccurNum(specTime);
        if (occurNum == 0) return null;// 事件还没开始
        long lastStart = start + (occurNum - 1) * interval;
        if (specTime < lastStart + duration)
            return new SpecEvent(this, lastStart, occurNum);
        return null;
    }

    /**
     * 判断事件与指定事件时间是否冲突
     *
     * @param event 指定事件
     * @return 有冲突返回true, 无冲突返回false
     */
    public boolean isTimeConflictWith(Event event) {
        //计算两事件interval的最大公约数
        long intervalGCD = computeGCD(this.interval, event.interval);
        //把两事件按事件时间跨度中点分成更早的和更晚的
        Event early, later;
        long earlyMidTime, laterMidTime;
        if ((this.start * 2 + this.duration) / 2 < (event.start * 2 + event.duration) / 2) {
            early = this;
            later = event;
            earlyMidTime = (this.start * 2 + this.duration) / 2;
            laterMidTime = (event.start * 2 + event.duration) / 2;
        } else {
            early = event;
            later = this;
            earlyMidTime = (event.start * 2 + event.duration) / 2;
            laterMidTime = (this.start * 2 + this.duration) / 2;
        }
        //把更早的事件向更晚的事件以intervalGCD为单位平移距离，平移到两事件时间跨度中点距离最近
        long translationTimes = (laterMidTime - earlyMidTime) / intervalGCD;
        long distance = laterMidTime - (earlyMidTime + intervalGCD * translationTimes);
        if (Math.abs(laterMidTime - (earlyMidTime + intervalGCD * (translationTimes + 1))) < distance) {
            translationTimes++;
        }
        long earlyStartNearest = early.start + intervalGCD * translationTimes;
        //判断两事件距离最近时是否有时间冲突
        if (!(earlyStartNearest + early.duration - 1 < later.start ||
                later.start + later.duration - 1 < earlyStartNearest)) {
            Log.d(TAG, "Time Conflict\n" + early + "\n" +
                    DateTimeFormatUtil.getReadableDateHourMinute(earlyStartNearest) + "~" +
                    DateTimeFormatUtil.getReadableDateHourMinute(earlyStartNearest +
                            early.duration - 1) + "\n" +
                    "Conflict with\n" + later + "\n" +
                    DateTimeFormatUtil.getReadableDateHourMinute(later.start) + "~" +
                    DateTimeFormatUtil.getReadableDateHourMinute(later.start +
                            later.duration - 1)
            );
            return true;
        }
        return false;
    }

    /**
     * 获取指定时间段内该事件要发生的具体事件
     *
     * @param startTime 时间段起始时间(ms)
     * @param endTime   时间段结束时间(ms)
     * @return 包含所有指定时间段内发生的具体事件的列表
     */
    public List<SpecEvent> getSpecEventsDuring(long startTime, long endTime) {
        List<SpecEvent> specEvents = new ArrayList<>();
        int occurNum = getEventOccurNum(startTime);
        if (occurNum == 0) {
            occurNum++;
        }
        long specStart = start + (occurNum - 1) * interval;
        while (specStart <= endTime) {
            if (specStart + duration - 1 >= startTime) {
                SpecEvent specEvent = new SpecEvent(this, specStart, occurNum);
                specEvents.add(specEvent);
            }
            occurNum++;
            specStart += interval;
        }
        return specEvents;
    }
}
