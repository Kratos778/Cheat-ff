package com.kratos.fflauncher.model

data class GameStatus(
    val installed: Boolean,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val message: String
)
