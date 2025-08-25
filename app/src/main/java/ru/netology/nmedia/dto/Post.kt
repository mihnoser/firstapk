package ru.netology.nmedia.dto

data class Post(
   val id: Int,
   val author: String,
   val published: String,
   val content: String,
   val likes: Int = 12,
   val shared: Int = 25,
   val likeByMe: Boolean = false,
   val shareByMe: Boolean = false,
   val views: Int = 7
)
