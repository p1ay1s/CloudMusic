package com.niki.cloudmusic.repository.model

data class PlaylistSongsResponse(
    var songs: List<Song>? = null,
    var code: Int
)

data class Song(
    var name: String? = "null",
    var id: String?,
    var ar: List<Author>?,
    var al: Album?,
    var dt: Int?, // 歌曲长度
    var mark: Long? // 1048576脏标
)

data class LikeListResponse(
    val ids: List<String>,
    var code: Int
)

data class SingleSongResponse(
    var code: Int,
    var data: List<SongInfo>
)

data class SongInfo(
    var code: Int,
    var url: String
)

data class AvailableResponse(
    var success: Boolean? = false,
    var code: Int
)

data class Author(
    var id: String,
    var name: String,
)

data class Album(
    var id: String,
    var name: String,
    var picUrl: String
)