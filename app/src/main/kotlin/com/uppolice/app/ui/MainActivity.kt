package com.uppolice.app.ui

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.uppolice.app.R
import com.uppolice.app.databinding.ActivityMainBinding
import com.uppolice.app.util.Constants
import com.uppolice.app.util.NetworkUtil
import com.uppolice.app.webview.UPPoliceChromeClient
import com.uppolice.app.webview.UPPoliceWebViewClient
import com.uppolice.app.webview.WebViewManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),
    UPPoliceWebViewClient.WebViewClientListener,
    UPPoliceChromeClient.ChromeClientListener {

    private lateinit var binding: ActivityMainBinding
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var isPageLoaded = false

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        filePathCallback?.onReceiveValue(uris.toTypedArray())
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash screen visible until page loads
        splashScreen.setKeepOnScreenCondition { !isPageLoaded }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupSwipeRefresh()
        setupBackNavigation()
        setupNetworkObserver()
        setupDownloadListener()

        if (savedInstanceState == null) {
            loadUrl()
        }
    }

    private fun setupWebView() {
        val webViewClient = UPPoliceWebViewClient(this)
        val chromeClient = UPPoliceChromeClient(this)
        WebViewManager.configureWebView(binding.webView, webViewClient, chromeClient)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
            )
            setOnRefreshListener {
                binding.webView.reload()
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.webView.canGoBack() -> binding.webView.goBack()
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun setupNetworkObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                NetworkUtil.observeNetworkState(this@MainActivity).collect { isConnected ->
                    if (isConnected) {
                        showContent()
                        if (binding.webView.url == null || binding.webView.url == "about:blank") {
                            loadUrl()
                        }
                    } else {
                        showNoNetwork()
                    }
                }
            }
        }
    }

    private fun setupDownloadListener() {
        binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimeType)
                    addRequestHeader("User-Agent", userAgent)
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                    setDescription(getString(R.string.downloading))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "${Constants.DOWNLOAD_DIRECTORY}/${
                            URLUtil.guessFileName(url, contentDisposition, mimeType)
                        }"
                    )
                }
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
                Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUrl() {
        val intentUrl = intent?.data?.toString()
        val url = intentUrl ?: Constants.BASE_URL
        binding.webView.loadUrl(url)
    }

    private fun showContent() {
        binding.webView.visibility = View.VISIBLE
        binding.errorLayout.visibility = View.GONE
    }

    private fun showNoNetwork() {
        binding.webView.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.btnRetry.setOnClickListener {
            if (NetworkUtil.isNetworkAvailable(this)) {
                showContent()
                loadUrl()
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // region WebViewClientListener
    override fun onPageStarted(url: String) {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onPageFinished(url: String) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        isPageLoaded = true
    }

    override fun onPageError(errorCode: Int, description: String, failingUrl: String) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showNoNetwork()
        }
    }
    // endregion

    // region ChromeClientListener
    override fun onProgressChanged(progress: Int) {
        binding.progressBar.apply {
            this.progress = progress
            visibility = if (progress < 100) View.VISIBLE else View.GONE
        }
    }

    override fun onReceivedTitle(title: String) {
        // Can be used to update toolbar title if needed
    }

    override fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>): Boolean {
        this.filePathCallback?.onReceiveValue(null)
        this.filePathCallback = filePathCallback
        fileChooserLauncher.launch("*/*")
        return true
    }
    // endregion

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.toString()?.let { url ->
            binding.webView.loadUrl(url)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.webView.restoreState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        WebViewManager.destroyWebView(binding.webView)
        super.onDestroy()
    }
}
