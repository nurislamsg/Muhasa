package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
  @PrimaryKey val dateStr: String,
  val jsonPayload: String,
)

@Entity(tableName = "app_state")
data class AppStateEntity(
  @PrimaryKey val key: String,
  val value: String,
)

@Dao
interface MuhasabaDao {
  @Query("SELECT * FROM daily_records")
  fun observeAllDays(): Flow<List<DailyRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertDay(entity: DailyRecordEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertDays(entities: List<DailyRecordEntity>)

  @Query("SELECT * FROM app_state")
  fun observeAppState(): Flow<List<AppStateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertState(entity: AppStateEntity)

  @Query("DELETE FROM app_state WHERE `key` = :key")
  suspend fun deleteState(key: String)
}

@Database(
  entities = [DailyRecordEntity::class, AppStateEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class MuhasabaDatabase : RoomDatabase() {
  abstract fun muhasabaDao(): MuhasabaDao

  companion object {
    @Volatile private var INSTANCE: MuhasabaDatabase? = null

    fun getInstance(context: Context): MuhasabaDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                MuhasabaDatabase::class.java,
                "muhasaba_diary_db",
              )
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
