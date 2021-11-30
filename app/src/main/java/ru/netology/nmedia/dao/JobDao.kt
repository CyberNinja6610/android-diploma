package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.JobEntity

@Dao
interface JobDao {

    @Query("SELECT * FROM JobEntity WHERE userId = :userId ORDER BY id DESC")
    fun getAll(userId: Long): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("UPDATE JobEntity SET position = :position WHERE id = :id")
    suspend fun updateContentById(id: Long, position: String)

    suspend fun save(job: JobEntity) =
        if (job.id == 0L) insert(job) else updateContentById(job.id, job.position)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}