package com.example.about

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val bio: String,
    val website: String
)
