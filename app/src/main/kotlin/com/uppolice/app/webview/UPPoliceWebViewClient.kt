package com.uppolice.app.webview

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.uppolice.app.util.Constants

class UPPoliceWebViewClient(
    private val listener: WebViewClientListener
) : WebViewClient() {

    interface WebViewClientListener {
        fun onPageStarted(url: String)
        fun onPageFinished(url: String)
        fun onPageError(errorCode: Int, description: String, failingUrl: String)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { listener.onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { listener.onPageFinished(it) }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        val host = request.url?.host ?: return false

        // Allow navigation within the UP Police domain
        if (host.contains(Constants.ALLOWED_HOST) || host.contains("gov.in")) {
            return false
        }

        // Open external links in browser
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, request.url)
            view?.context?.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            listener.onPageError(
                error?.errorCode ?: -1,
                error?.description?.toString() ?: "Unknown error",
                request.url?.toString() ?: ""
            )
        }
    }

    @Suppress("DEPRECATION")
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // For government sites, proceed with SSL errors cautiously
        // In production, you may want stricter handling
        handler?.proceed()
    }
}
