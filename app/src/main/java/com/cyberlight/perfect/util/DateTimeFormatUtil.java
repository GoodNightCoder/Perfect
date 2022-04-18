package com.cyberlight.perfect.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeFormatUtil {

    private DateTimeFormatUtil() {
    }

    // -----------------------By LocalDate, LocalTime, LocalDateTime------------------------

    /**
     * 获取位数统一、样式唯一的格式化日期，适于存日期至数据库(yyyy-MM-dd)
     *
     * @param date LocalDate对象
     * @return 位数统一、样式唯一的格式化日期
     */
    public static String getNeatDate(LocalDate date) {
        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * 获取位数统一、样式唯一的格式化日期与时间，包括
     * 年月日时分秒(yyyy-MM-dd HH:mm:ss)
     *
     * @param dateTime LocalDateTime对象
     * @return 位数统一、样式唯一的格式化日期与时间
     */
    public static String getNeatDateTime(LocalDateTime dateTime) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 获取易读的格式化日期，适于展示给用户
     * 中文:yyyy年M月d日
     * English:MMM d, yyyy
     *
     * @param date LocalDate对象
     * @return 易读的格式化日期
     */
    public static String getReadableDate(LocalDate date) {
        String language = Locale.getDefault().getLanguage();
        String pattern;
        if (language.equals(new Locale("zh").getLanguage())) {
            pattern = "yyyy年M月d日";
        } else {
            pattern = "MMM d, yyyy";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * 获取易读的、附带星期的格式化日期，适于展示给用户
     * 中文:yyyy年M月d日E
     * English:E, MMM d, yyyy
     *
     * @param date LocalDate对象
     * @return 易读的格式化日期
     */
    public static String getReadableDateAndDayOfWeek(LocalDate date) {
        String language = Locale.getDefault().getLanguage();
        String pattern;
        if (language.equals(new Locale("zh").getLanguage())) {
            pattern = "yyyy年M月d日E";
        } else {
            pattern = "E, MMM d, yyyy";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * 获取日期的月和日字符串
     * 中文:M月d日
     * English:MMM d
     *
     * @param date LocalDate对象
     * @return 日期的月和日字符串
     */
    public static String getReadableMonthAndDayOfMonth(LocalDate date) {
        String language = Locale.getDefault().getLanguage();
        String pattern;
        if (language.equals(new Locale("zh").getLanguage())) {
            pattern = "M月d日";
        } else {
            pattern = "MMM d";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * 获取易读的日期时间(年、月、日、时、分)，适于展示给用户(yyyy.M.d HH:mm)
     * 本App中用于发送事件提醒通知
     *
     * @param dateTime LocalDateTime对象
     * @return 易读的日期时间
     */
    public static String getReadableDateHourMinute(LocalDateTime dateTime) {
        String language = Locale.getDefault().getLanguage();
        String pattern;
        if (language.equals(new Locale("zh").getLanguage())) {
            pattern = "yyyy年M月d日 HH:mm";
        } else {
            pattern = "MMM d, yyyy HH:mm";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }


    // ---------------------------------By fields-------------------------------------------

    /**
     * 获取位数统一、样式唯一的格式化日期，适于存日期至数据库(yyyy-MM-dd)
     *
     * @param year       年
     * @param month      月(1~12)
     * @param dayOfMonth 日
     * @return 位数统一、样式唯一的格式化日期
     */
    public static String getNeatDate(int year, int month, int dayOfMonth) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        return getNeatDate(date);
    }

    /**
     * 获取位数统一、样式唯一的格式化日期与时间，包括
     * 年月日时分秒(yyyy-MM-dd HH:mm:ss)
     *
     * @param year       年
     * @param month      月
     * @param dayOfMonth 日
     * @param hour       时
     * @param minute     分
     * @param second     秒
     * @return 位数统一、样式唯一的格式化日期与时间
     */
    public static String getNeatDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        LocalDateTime dateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        return getNeatDateTime(dateTime);
    }

    /**
     * 获取易读的格式化日期，适于展示给用户
     * 中文:yyyy年M月d日
     * English:MMM d, yyyy
     *
     * @param year       年
     * @param month      月(1~12)
     * @param dayOfMonth 日
     * @return 易读的格式化日期
     */
    public static String getReadableDate(int year, int month, int dayOfMonth) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        return getReadableDate(date);
    }

    /**
     * 获取易读的、附带星期的格式化日期，适于展示给用户
     * 中文:yyyy年M月d日E
     * English:E, MMM d, yyyy
     *
     * @param year       年
     * @param month      月(1~12)
     * @param dayOfMonth 日
     * @return 易读的格式化日期
     */
    public static String getReadableDateAndDayOfWeek(int year, int month, int dayOfMonth) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        return getReadableDateAndDayOfWeek(date);
    }

    /**
     * 获取日期的月和日字符串
     * 中文:M月d日
     * English:MMM d
     *
     * @param year       年
     * @param month      月(1~12)
     * @param dayOfMonth 日
     * @return 日期的月和日字符串
     */
    public static String getReadableMonthAndDayOfMonth(int year, int month, int dayOfMonth) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        return getReadableMonthAndDayOfMonth(date);
    }

    /**
     * 获取易读的日期时间(年、月、日、时、分)，适于展示给用户(yyyy.M.d HH:mm)
     * 本App中用于发送事件提醒通知
     *
     * @param year       年
     * @param month      月(1~12)
     * @param dayOfMonth 日
     * @param hour       时
     * @param minute     分
     * @return 易读的日期时间
     */
    public static String getReadableDateHourMinute(
            int year, int month, int dayOfMonth, int hour, int minute) {
        LocalDateTime dateTime =
                LocalDateTime.of(year, month, dayOfMonth, hour, minute);
        return getReadableDateHourMinute(dateTime);
    }


//    --------------------------------------By timeInMillis----------------------------------------

    /**
     * 获取位数统一、样式唯一的格式化日期，适于存日期至数据库(yyyy-MM-dd)
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 位数统一、样式唯一的格式化日期
     */
    public static String getNeatDate(long millis) {
        LocalDate date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        return getNeatDate(date);
    }

    /**
     * 获取位数统一、样式唯一的格式化日期与时间，包括
     * 年月日时分秒(yyyy-MM-dd HH:mm:ss)
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 位数统一、样式唯一的格式化日期与时间
     */
    public static String getNeatDateTime(long millis) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
                ZoneId.systemDefault());
        return getNeatDateTime(dateTime);
    }

    /**
     * 获取易读的格式化日期，适于展示给用户
     * 中文:yyyy年M月d日
     * English:MMM d, yyyy
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 易读的格式化日期
     */
    public static String getReadableDate(long millis) {
        LocalDate date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        return getReadableDate(date);
    }

    /**
     * 获取易读的、附带星期的格式化日期，适于展示给用户
     * 中文:yyyy年M月d日E
     * English:E, MMM d, yyyy
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 易读的格式化日期
     */
    public static String getReadableDateAndDayOfWeek(long millis) {
        LocalDate date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        return getReadableDateAndDayOfWeek(date);
    }

    /**
     * 获取日期的月和日字符串
     * 中文:M月d日
     * English:MMM d
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 日期的月和日字符串
     */
    public static String getReadableMonthAndDayOfMonth(long millis) {
        LocalDate date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        return getReadableMonthAndDayOfMonth(date);
    }

    /**
     * 获取易读的日期时间(年、月、日、时、分)，适于展示给用户(yyyy.M.d HH:mm)
     * 本App中用于发送事件提醒通知
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 易读的日期时间
     */
    public static String getReadableDateHourMinute(long millis) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
                ZoneId.systemDefault());
        return getReadableDateHourMinute(dateTime);
    }


    // ----------------------------------For debugging purpose----------------------------------

    /**
     * 获取调试用的日期时间
     *
     * @param millis 从1970-01-01 00:00:00经过的毫秒数
     * @return 调试用的日期时间
     */
    public static String getDateTimeForDebugging(long millis) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
                ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss.SSS");
        return dateTime.format(formatter);
    }

    /**
     * 获取调试用的时长
     *
     * @param millis 时长毫秒数
     * @return 调试用的时长字符串
     */
    public static String getDurationForDebugging(long millis) {
        long hour = millis / 3600000;
        long min = millis % 3600000 / 60000;
        long sec = millis % 60000 / 1000;
        long ms = millis % 1000;
        return hour + "h " + min + "min " + sec + "s " + ms + "ms";
    }

}
