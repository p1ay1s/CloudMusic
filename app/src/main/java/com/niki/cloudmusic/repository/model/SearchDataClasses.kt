package com.niki.cloudmusic.repository.model

data class SearchResponse(
    var result: SearchResult? = null,
    var code: Int
)

data class SearchResult(
    var songs: List<SearchSong>? = null,
    var songCount: Int,
    var hasMore: Boolean
)

data class SearchSong(
    var name: String? = "null",
    var id: String?,
    var artists: List<SearchAuthor>?,
    var album: Album?,
    var duration: Int?, // 歌曲长度
    var mark: Long? // 1048576脏标
)

data class SearchAuthor(
    var id: String,
    var name: String,
    var img1v1Url: String
)