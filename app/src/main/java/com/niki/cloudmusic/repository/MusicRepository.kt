package com.niki.cloudmusic.repository

import com.niki.cloudmusic.repository.model.AvailableResponse
import com.niki.cloudmusic.repository.model.LikeListResponse
import com.niki.cloudmusic.repository.model.PlaylistSongsResponse
import com.niki.cloudmusic.repository.model.SingleSongResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MusicRepository(private val baseUrl: String) : BaseRepository() {
    private interface MusicService {
        @GET("/playlist/track/all")
        fun getSongsFromPlaylist(
            @Query("id") id: String,
            @Query("limit") limit: Int,
            @Query("offset") offset: Int
        ): Call<PlaylistSongsResponse>

        @GET("/song/url/v1")
        fun getSongInfo(
            @Query("id") id: String,
            @Query("level") level: String,
            @Query("cookie") cookie: String?
        ): Call<SingleSongResponse>

        @GET("/check/music")
        fun checkSong(
            @Query("id") id: String
        ): Call<AvailableResponse>

        @GET("/likelist")
        fun getLikeList(
            @Query("uid") uid: String,
            @Query("cookie") cookie: String
        ): Call<LikeListResponse>
    }

    init {
        TAG = "MusicRepository"
    }

    private val musicService: MusicService by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClientBuilder())
            .build()
            .create(MusicService::class.java)
    }

    fun getSongsFromPlaylist(
        id: String,
        limit: Int,
        offset: Int,
        call: (PlaylistSongsResponse?, Int?, String?) -> Unit
    ) = enqueueCall(musicService.getSongsFromPlaylist(id, limit, offset), call)

    fun getLikeList(
        uid: String,
        cookie: String,
        call: (LikeListResponse?, Int?, String?) -> Unit
    ) = enqueueCall(musicService.getLikeList(uid, cookie), call)

    fun getSongInfo(
        id: String,
        level: String,
        cookie: String?,
        call: (SingleSongResponse?, Int?, String?) -> Unit
    ) = enqueueCall(musicService.getSongInfo(id, level, cookie), call)

    fun checkSong(
        id: String,
        call: (AvailableResponse?, Int?, String?) -> Unit
    ) = enqueueCall(musicService.checkSong(id), call)

}