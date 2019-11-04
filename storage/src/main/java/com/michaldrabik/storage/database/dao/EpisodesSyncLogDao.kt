package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.EpisodesSyncLog

@Dao
interface EpisodesSyncLogDao {

  @Query("SELECT * from sync_episodes_log")
  suspend fun getAll(): List<EpisodesSyncLog>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(log: EpisodesSyncLog)
}