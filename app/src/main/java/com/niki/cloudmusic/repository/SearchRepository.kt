package com.niki.cloudmusic.repository

import com.niki.cloudmusic.repository.model.PlaylistSongsResponse
import com.niki.cloudmusic.repository.model.SearchResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SearchRepository(private val baseUrl: String) : BaseRepository() {
    private interface SearchService {
        @GET("/search")
        fun searchSongs(
            @Query("keywords") keywords: String,
            @Query("limit") limit: Int,
            @Query("offset") offset: Int
        ): Call<SearchResponse>

        @GET("/song/detail")
        fun searchSongsByIds(
            @Query("ids") ids: String
        ): Call<PlaylistSongsResponse>
    }

    init {
        TAG = "SearchRepository"
    }

    private val searchService: SearchService by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClientBuilder())
            .build()
            .create(SearchService::class.java)
    }

    fun searchSongs(
        limit: Int,
        offset: Int,
        keywords: String,
        call: (SearchResponse?, Int?, String?) -> Unit
    ) = enqueueCall(searchService.searchSongs(keywords,limit,offset), call)

    fun searchSongsByIds(
        ids: String,
        call: (PlaylistSongsResponse?, Int?, String?) -> Unit
    ) = enqueueCall(searchService.searchSongsByIds(ids), call)

}