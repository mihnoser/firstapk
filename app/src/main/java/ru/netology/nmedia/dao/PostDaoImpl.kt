package ru.netology.nmedia.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.dto.Post

class PostDaoImpl(private val db: SQLiteDatabase) : PostDao {
    companion object {
        val DDL = """
            CREATE TABLE ${PostColumns.TABLE} (
                ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
                ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL,
                ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
                ${PostColumns.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 0,
                ${PostColumns.COLUMN_SHARED} INTEGER NOT NULL DEFAULT 0,
                ${PostColumns.COLUMN_LIKE_BY_ME} INTEGER NOT NULL DEFAULT 0,
                ${PostColumns.COLUMN_SHARE_BY_ME} INTEGER NOT NULL DEFAULT 0,
                ${PostColumns.COLUMN_VIEWS} INTEGER NOT NULL DEFAULT 0,
                ${PostColumns.COLUMN_VIDEO} TEXT
            );
        """.trimIndent()
    }

    object PostColumns {
        const val TABLE = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_SHARED = "shared"
        const val COLUMN_LIKE_BY_ME = "likeByMe"
        const val COLUMN_SHARE_BY_ME = "shareByMe"
        const val COLUMN_VIEWS = "views"
        const val COLUMN_VIDEO = "video"
        val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_AUTHOR,
            COLUMN_PUBLISHED,
            COLUMN_CONTENT,
            COLUMN_LIKES,
            COLUMN_SHARED,
            COLUMN_LIKE_BY_ME,
            COLUMN_SHARE_BY_ME,
            COLUMN_VIEWS,
            COLUMN_VIDEO
        )
    }

    override fun get(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            put(PostColumns.COLUMN_AUTHOR, "Me")
            put(PostColumns.COLUMN_PUBLISHED, "16 september 2025")
            put(PostColumns.COLUMN_CONTENT, post.content)
        }
        val id = if (post.id != 0) {
            db.update(
                PostColumns.TABLE,
                values,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(post.id.toString()),
            )
            post.id
        } else {
            db.insert(PostColumns.TABLE, null, values)
        }
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likeById(id: Int) {
        db.execSQL(
            """
                UPDATE posts SET
                    likes = likes + CASE WHEN likeByMe THEN -1 ELSE 1 END,
                    likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
                WHERE id = ?;
            """.trimIndent(), arrayOf(id)
        )
    }

    override fun shareById(id: Int) {
        db.execSQL(
            """
                UPDATE posts SET
                    shared = shared + 1
                WHERE id = ?;
            """.trimIndent(), arrayOf(id)
        )
    }

    override fun removeById(id: Int) {
        db.delete(
            PostColumns.TABLE,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                id = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                likes = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                shared = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARED)),
                likeByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKE_BY_ME)) != 0,
                shareByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARE_BY_ME)) != 0,
                views = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_VIEWS)),
                video = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEO))
            )
        }
    }

}