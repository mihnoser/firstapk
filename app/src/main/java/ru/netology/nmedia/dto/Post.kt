package ru.netology.nmedia.dto

data class Post(
   val id: Int,
   val author: String,
   val published: String,
   val content: String,
   var likes: Int = 12,
   var shared: Int = 25,
   var likeByMe: Boolean = false,
   var shareByMe: Boolean = false
)
