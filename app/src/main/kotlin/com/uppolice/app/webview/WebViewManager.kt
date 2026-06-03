package com.uppolice.app.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import com.uppolice.app.util.Constants

/**
 * Manages WebView configuration and lifecycle.
 * Centralizes all WebView settings for consistency and performance.
 */
object WebViewManager {

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebView(
        webView: WebView,
        webViewClient: UPPoliceWebViewClient,
        chromeClient: UPPoliceChromeClient
    ) {
        webView.apply {
            this.webViewClient = webViewClient
            this.webChromeClient = chromeClient

            settings.apply {
                // Core settings
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true

                // Cache strategy for performance
                cacheMode = WebSettings.LOAD_DEFAULT
                setGeolocationEnabled(true)

                // Display settings
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // File access
                allowFileAccess = true
                allowContentAccess = true

                // Mixed content for government sites that may have mixed resources
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                // Performance optimizations
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                setEnableSmoothTransition(true)

                // User agent
                userAgentString = "$userAgentString${Constants.USER_AGENT_SUFFIX}"

                // Media
                mediaPlaybackRequiresUserGesture = false

                // Text
                textZoom = 100
            }

            // Enable cookies
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(webView, true)
            }

            // Hardware acceleration
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // Scrollbar styling
            isScrollbarFadingEnabled = true
            scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY

            // Accessibility
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    fun clearCache(webView: WebView) {
        webView.clearCache(true)
        webView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
    }

    fun destroyWebView(webView: WebView) {
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
    }
}
