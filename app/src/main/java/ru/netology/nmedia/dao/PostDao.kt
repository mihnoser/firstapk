package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content =:content WHERE id=:id")
    suspend fun updateContentById(id: Long,content: String)

    suspend fun save(post: PostEntity) = if (post.id == 0.toLong()) {
        insert(post)
    } else {
        updateContentById(post.id, post.content)
    }

    @Query(
        """
            UPDATE PostEntity SET
                likes = likes + CASE WHEN likeByMe THEN -1 ELSE 1 END,
                likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
            WHERE id = :id;
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
            UPDATE PostEntity SET
                shared = shared + 1
            WHERE id = :id;
        """
    )
    suspend fun shareById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id=:id")
    suspend fun removeById(id: Long)
}