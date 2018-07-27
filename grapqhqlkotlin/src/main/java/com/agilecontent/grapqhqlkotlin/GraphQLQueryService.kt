package com.agilecontent.grapqhqlkotlin

import com.google.gson.ExclusionStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.experimental.async
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody


open class GraphQLQueryService(val url: String, val auth: String? = null) {

    suspend inline fun execute(query: String): JsonObject? {
        val client = OkHttpClient()
        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val queryText = "{\"query\": \"$query\" }"
        val body = RequestBody.create(mediaType, queryText)
        val request = Request.Builder()
                .apply { if (auth != null) addHeader("Authorization", auth) }
                .post(body)
                .url(url)
                .build()
        val deferred = async { client.newCall(request).execute() }
        val response = deferred.await()
        return Gson().fromJson(response.body()?.string(), JsonObject::class.java)?.getAsJsonObject("data")
    }
}