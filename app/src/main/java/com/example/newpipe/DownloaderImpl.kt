package com.example.newpipe

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.io.IOException

class DownloaderImpl private constructor() : Downloader() {
    
    companion object {
        private val INSTANCE = DownloaderImpl()
        fun getInstance(): DownloaderImpl = INSTANCE
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = okhttp3.Request.Builder().url(url)
        
        headers?.forEach { (key, list) ->
            list.forEach { value ->
                requestBuilder.addHeader(key, value)
            }
        }

        val requestBody = if (dataToSend != null) {
            dataToSend.toRequestBody(null)
        } else if (httpMethod == "POST" || httpMethod == "PUT") {
            "".toRequestBody(null)
        } else {
            null
        }

        requestBuilder.method(httpMethod, requestBody)

        val okHttpRequest = requestBuilder.build()
        val okHttpResponse = client.newCall(okHttpRequest).execute()

        val responseHeaders = okHttpResponse.headers.toMultimap().mapValues { it.value.toList() }
        
        return Response(
            okHttpResponse.code,
            okHttpResponse.message,
            responseHeaders,
            okHttpResponse.body?.string(),
            okHttpResponse.request.url.toString()
        )
    }
}
