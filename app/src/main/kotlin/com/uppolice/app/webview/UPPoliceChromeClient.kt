package com.uppolice.app.webview

import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class UPPoliceChromeClient(
    private val listener: ChromeClientListener
) : WebChromeClient() {

    interface ChromeClientListener {
        fun onProgressChanged(progress: Int)
        fun onReceivedTitle(title: String)
        fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>): Boolean
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        listener.onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        title?.let { listener.onReceivedTitle(it) }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        filePathCallback?.let {
            return listener.onShowFileChooser(it)
        }
        return false
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin, true, false)
    }
}
