package com.niki.cloudmusic.repository


import com.niki.cloudmusic.repository.model.CatPlaylistResponse
import com.niki.cloudmusic.repository.model.HotPlaylistResponse
import com.niki.cloudmusic.repository.model.TopPlaylistResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class PlaylistRepository(private val baseUrl: String) : BaseRepository() {
    private interface PlaylistService {
        @GET("/playlist/hot")
        fun getPlaylistsByHot(): Call<HotPlaylistResponse>

        @GET("/playlist/catlist")
        fun getPlaylistsByCat(): Call<CatPlaylistResponse>

        @GET("/top/playlist")
        fun getTopPlaylists(
            @Query("limit") limit: Int,
            @Query("order") order: String,
            @Query("offset") offset: Int
        ): Call<TopPlaylistResponse>
    }

    init {
        TAG = "PlaylistRepository"
    }

    private val playlistService: PlaylistService by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClientBuilder())
            .build()
            .create(PlaylistService::class.java)
    }

    fun getPlaylistByHot(
        call: (HotPlaylistResponse?, Int?, String?) -> Unit
    ) = enqueueCall(playlistService.getPlaylistsByHot(), call)

    fun getPlaylistByCat(
        call: (CatPlaylistResponse?, Int?, String?) -> Unit
    ) = enqueueCall(playlistService.getPlaylistsByCat(), call)

    fun getTopPlaylists(
        limit: Int,
        order: String,
        offset: Int,
        call: (TopPlaylistResponse?, Int?, String?) -> Unit
    ) = enqueueCall(playlistService.getTopPlaylists(limit, order, offset), call)
}