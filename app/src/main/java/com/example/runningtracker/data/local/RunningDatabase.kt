package com.example.runningtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runningtracker.data.local.entity.RunningRecordEntity

/**
 * 앱의 Room 데이터베이스를 정의하는 추상 클래스.
 *
 * @property entities 데이터베이스에 포함될 엔티티(테이블) 목록.
 * @property version 데이터베이스 스키마 버전. 스키마 변경 시 버전을 올려야 합니다.
 * @property exportSchema 스키마 정보를 파일로 내보낼지 여부.
 */
@Database(
    entities = [RunningRecordEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun runningDao(): RunningDao

    companion object {
        @Volatile
        private var INSTANCE: RunningDatabase? = null

        fun getDatabase(context: Context): RunningDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RunningDatabase::class.java,
                    "running_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}