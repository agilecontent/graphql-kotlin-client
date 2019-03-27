package com.agilecontent.grapqhqlkotlin

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.*

import java.io.File
import java.net.URLEncoder


open class GraphQLQueryService(val url: String, val auth: String? = null, val context: Context) {

    suspend inline fun executePost(query: String): JsonObject? {
        val cache = Cache(File(context.cacheDir, "http-cache"), 10 * 1024 * 1024)
        val client = OkHttpClient.Builder().addNetworkInterceptor(CacheInterceptor()).cache(cache).build()
        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val queryText = "{\"query\": \"$query\" }"
        val body = RequestBody.create(mediaType, queryText)
        val request = Request.Builder()
                .apply { if (auth != null) addHeader("Authorization", auth) }
                .post(body)
                .url(url)
                .build()
        val deferred = CoroutineScope(Dispatchers.Default).async { client.newCall(request).execute() }
        val response = deferred.await()
        return Gson().fromJson(response.body()?.string(), JsonObject::class.java)?.getAsJsonObject("data")
    }

    suspend inline fun execute(query: String): JsonObject? {
        val cache = Cache(File(context.cacheDir, "http-cache"), 10 * 1024 * 1024)
        val client = OkHttpClient.Builder().addNetworkInterceptor(CacheInterceptor()).cache(cache).build()
        val request = Request.Builder()
                .apply { if (auth != null) addHeader("Authorization", auth) }
                .get()
                .url(HttpUrl.Builder().host(url).addQueryParameter("query",query).build())
                .build()
        val deferred = CoroutineScope(Dispatchers.Default).async { client.newCall(request).execute() }
        val response = deferred.await()
        return Gson().fromJson(response.body()?.string(), JsonObject::class.java)?.getAsJsonObject("data")
    }
}