package com.niki.cloudmusic.repository.model

/**
 *  热门分区:
 *  Tag: id, name, position, category
 */
data class HotPlaylistResponse(
    var code: Int,
    var tags: MutableList<Tag>? = null
)

data class Tag(
    var id: String?,
    var name: String? = "null",
    var position: Int?,
    var category: String? = null
)

/**
 * sub: name, resourceCount, category
 */
data class CatPlaylistResponse(
    var code: Int,
    var all: Sub? = null,
    var sub: MutableList<Sub>? = null
)

data class Sub(
    // also tag for cat
    var name: String? = null,
    var resourceCount: Int? = null,
    var category: String? = null
)

/**
 * playlist: name, id, coverImgUrl, description, tagList, creator
 */
data class TopPlaylistResponse(
    var more: Boolean,
    var code: Int,
    var playlists: MutableList<Playlist>? = null
)

data class Playlist(
    var name: String? = null,
    var id: String? = null,
    var coverImgUrl: String? = null,
    var description: String? = null,
    var tags: MutableList<String>? = null,
    var creator: Creator? = null
)

data class Creator(
    var nickname: String? = null,
    var avatarUrl: String? = null
)