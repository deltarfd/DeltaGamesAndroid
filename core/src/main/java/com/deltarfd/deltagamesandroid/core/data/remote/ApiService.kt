package com.deltarfd.deltagamesandroid.core.data.remote

import com.deltarfd.deltagamesandroid.core.BuildConfig
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameDetailResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("page_size") pageSize: Int = 20,
        @Query("ordering") ordering: String = "-added",
        @Query("page") page: Int = 1
    ): GameListResponse

    @GET("games/lists/main")
    suspend fun getTrendingGames(
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("page_size") pageSize: Int = 8,
        @Query("ordering") ordering: String = "-relevance",
        @Query("discover") discover: Boolean = true
    ): GameListResponse

    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("search") query: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search_precise") searchPrecise: Boolean = true
    ): GameListResponse

    @GET("games/{id}")
    suspend fun getGameDetail(
        @Path("id") id: Int,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY
    ): GameDetailResponse
}
