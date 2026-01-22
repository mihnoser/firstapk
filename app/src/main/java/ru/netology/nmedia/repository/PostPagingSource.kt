package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post
import java.io.IOException

class PostPagingSource(
    private val apiService: ApiService
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams.Refresh -> {
                    apiService.getLatest(params.loadSize)
                }

                is LoadParams.Append ->
                    apiService.getBefore(
                        params.key ?: 0L,
                        params.loadSize
                    )

                is LoadParams.Prepend -> {
                    return LoadResult.Page(
                        emptyList(),
                        params.key,
                        null,
                    )
                }
            }

            val data = result

            return LoadResult.Page(
                data = data,
                prevKey = when (params) {
                    is LoadParams.Refresh -> null
                    is LoadParams.Append -> params.key
                    is LoadParams.Prepend -> null
                },
                nextKey = data.lastOrNull()?.id
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }
}