package ru.netology.nmedia.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError

@ExperimentalPagingApi
class PostRemoteMediator(
    private val postApi: PostApi,
    private val postDao: PostDao,
    private val db: AppDb,
    private val postKeyDao: PostKeyDao
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    postApi.getLatest(state.config.initialLoadSize)

                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }
                LoadType.APPEND -> {
                    val id = postKeyDao.min() ?: return MediatorResult.Success(false)
                    postApi.getBefore(id, state.config.pageSize)
                }

            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(), response.message()
            )

            if (body.isEmpty()) {
                return MediatorResult.Success(body.isEmpty())
            }
            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.Type.PREPEND,
                                    body.first().id
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.Type.APPEND,
                                    body.last().id
                                )
                            )
                        )
                    }
                    LoadType.PREPEND -> {
                        postKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.Type.PREPEND,
                                body.first().id
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        postKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.Type.APPEND,
                                body.last().id
                            )
                        )
                    }
                }

                postDao.insert(body.map(PostEntity.Companion::fromDto))
            }

            return MediatorResult.Success(false)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

}
