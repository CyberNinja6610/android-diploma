package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.*
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.entity.EventEntity

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        JobEntity::class,
    ], version = 10, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun postKeyDao(): PostKeyDao
    abstract fun eventKeyDao(): EventKeyDao
    abstract fun jobDao(): JobDao

}