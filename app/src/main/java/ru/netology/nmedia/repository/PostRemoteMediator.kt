package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            when (loadType) {
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.REFRESH -> {
                    val latestPostId = postDao.getLatestPostId()
                    val body = if (latestPostId != null) {
                        apiService.getAfter(latestPostId, state.config.pageSize)
                    } else {
                        apiService.getLatest(state.config.pageSize)
                    }

                    return handleRefresh(body, latestPostId)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    val body = apiService.getBefore(id, state.config.pageSize)
                    return handleAppend(body)
                }
            }
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun handleRefresh(
        body: List<Post>,
        latestPostId: Long?
    ): MediatorResult {
        appDb.withTransaction {
            if (body.isNotEmpty()) {
                if (latestPostId != null) {
                    postRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            PostRemoteKeyEntity.KeyType.AFTER,
                            body.first().id,
                        ),
                    )
                } else {
                    postRemoteKeyDao.insert(
                        listOf(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            ),
                        )
                    )
                }

                val entities = body.map { dto ->
                    PostEntity.fromDto(dto).copy(showed = true)
                }
                postDao.insert(entities)
            }
        }

        return MediatorResult.Success(body.isEmpty())
    }

    private suspend fun handleAppend(body: List<Post>): MediatorResult {
        appDb.withTransaction {
            if (body.isNotEmpty()) {
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        PostRemoteKeyEntity.KeyType.BEFORE,
                        body.last().id,
                    ),
                )

                val entities = body.map { dto ->
                    PostEntity.fromDto(dto).copy(showed = true)
                }
                postDao.insert(entities)
            }
        }

        return MediatorResult.Success(body.isEmpty())
    }
}