package com.example.runningtracker.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room 데이터베이스에서 사용자 정의 타입을 저장하고 읽기 위해 필요한 타입 변환기 클래스.
 * Date <-> Long 변환을 처리합니다.
 */
class Converters {

    /**
     * Timestamp(Long) 값을 Date 객체로 변환합니다.
     * 데이터베이스에서 데이터를 읽을 때 사용됩니다.
     * @param value 데이터베이스에 저장된 Long 타입의 타임스탬프.
     * @return 변환된 Date 객체. null이면 null을 반환합니다.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Date 객체를 Timestamp(Long) 값으로 변환합니다.
     * 데이터베이스에 데이터를 쓸 때 사용됩니다.
     * @param date 변환할 Date 객체.
     * @return 데이터베이스에 저장될 Long 타입의 타임스탬프. null이면 null을 반환합니다.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
