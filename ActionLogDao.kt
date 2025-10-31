package com.neuralic.voicecrm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActionLogDao {
    @Query("SELECT * FROM action_log ORDER BY id DESC")
    fun getAll(): List<ActionLog>

    @Insert
    fun insert(log: ActionLog)
}
