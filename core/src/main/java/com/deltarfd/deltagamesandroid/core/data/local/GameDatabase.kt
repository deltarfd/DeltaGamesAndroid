package com.deltarfd.deltagamesandroid.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deltarfd.deltagamesandroid.core.data.local.entity.GameEntity

@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
