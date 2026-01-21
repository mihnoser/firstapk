package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity WHERE showed = 1 ORDER BY id DESC")
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
                likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
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

    @Query("UPDATE PostEntity SET showed=1 WHERE showed=0")
    suspend fun showAll()


    @Query("SELECT * FROM PostEntity WHERE showed=0 ORDER BY id DESC")
    fun getUnshowed(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int
}