package com.deepinnovations.call_history

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.startapp.sdk.ads.banner.BannerFormat
import com.startapp.sdk.ads.banner.BannerRequest
import com.startapp.sdk.ads.banner.BannerCreator
import com.startapp.sdk.adsbase.StartAppSDK
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** In-app-only Start.io test screen. Uses Start.io's public demo App ID and test mode. */
class StartIoBannerTestActivity : Activity() {
    companion object {
        private const val TAG = "StartIoBannerTest"
        private const val DEMO_APP_ID = "205489527"
    }

    private lateinit var status: TextView
    private lateinit var logView: TextView
    private lateinit var adContainer: FrameLayout
    private var requestInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        appendLog("Screen opened")
        initializeSdk()
    }

    private fun buildScreen() {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(Color.WHITE)
        }
        val title = TextView(this).apply {
            text = "Start.io Banner Test"
            textSize = 22f
            setTextColor(Color.rgb(25, 40, 65))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        page.addView(title)
        status = TextView(this).apply {
            text = "Status: preparing test..."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(12), 0, dp(12))
        }
        page.addView(status)

        val adLabel = TextView(this).apply {
            text = "TEST BANNER AREA"
            textSize = 12f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        page.addView(adLabel)
        adContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(245, 245, 245))
        }
        page.addView(adContainer, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(90)
        ))

        val reload = Button(this).apply {
            text = "Reload test banner"
            setOnClickListener { requestBanner() }
        }
        page.addView(reload)

        val logTitle = TextView(this).apply {
            text = "Diagnostics"
            textSize = 17f
            setTextColor(Color.BLACK)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(6))
        }
        page.addView(logTitle)
        logView = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.DKGRAY)
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply {
            addView(logView)
        }
        page.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val back = Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        }
        page.addView(back)
        setContentView(page)
    }

    private fun initializeSdk() {
        status.text = "Status: initializing Start.io test SDK..."
        appendLog("SDK init started; demo App ID=$DEMO_APP_ID; testAds=true")
        try {
            StartAppSDK.setTestAdsEnabled(true)
            StartAppSDK.initParams(applicationContext, DEMO_APP_ID)
                .setReturnAdsEnabled(false)
                .setCallback {
                    runOnUiThread {
                        status.text = "Status: SDK ready; requesting test banner"
                        appendLog("SDK initialization callback received")
                        requestBanner()
                    }
                }
                .init()
        } catch (t: Throwable) {
            status.text = "Status: SDK initialization error"
            appendLog("SDK INIT ERROR: ${t.javaClass.simpleName}: ${t.message}")
            Log.e(TAG, "SDK initialization failed", t)
        }
    }

    private fun requestBanner() {
        if (requestInProgress) {
            appendLog("Request skipped: another request is in progress")
            return
        }
        requestInProgress = true
        adContainer.removeAllViews()
        status.text = "Status: requesting test banner..."
        appendLog("BannerRequest started (format=BANNER)")
        try {
            BannerRequest(applicationContext)
                .setAdFormat(BannerFormat.BANNER)
                .load { creator: BannerCreator?, error: String? ->
                    runOnUiThread {
                        requestInProgress = false
                        if (creator != null) {
                            try {
                                val adView = creator.create(applicationContext, null)
                                adContainer.addView(adView, FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    Gravity.CENTER
                                ))
                                adView.visibility = View.VISIBLE
                                status.text = "Status: banner view created and attached"
                                appendLog("LOAD SUCCESS: BannerCreator returned a view")
                                appendLog("View class=${adView.javaClass.name}; size=${adView.width}x${adView.height} (measured after layout)")
                                adView.addOnLayoutChangeListener { v, l, t, r, b, _, _, _, _ ->
                                    val w = r - l
                                    val h = b - t
                                    if (w > 0 && h > 0) {
                                        appendLog("Banner view measured: ${w}x${h}px")
                                    }
                                }
                            } catch (t: Throwable) {
                                status.text = "Status: banner view creation failed"
                                appendLog("VIEW CREATE ERROR: ${t.javaClass.simpleName}: ${t.message}")
                                Log.e(TAG, "Banner view creation failed", t)
                            }
                        } else {
                            val message = error ?: "SDK returned no banner and no error text"
                            status.text = "Status: banner load failed"
                            appendLog("LOAD FAILED: $message")
                            Log.e(TAG, "Banner load failed: $message")
                        }
                    }
                }
        } catch (t: Throwable) {
            requestInProgress = false
            status.text = "Status: banner request error"
            appendLog("REQUEST ERROR: ${t.javaClass.simpleName}: ${t.message}")
            Log.e(TAG, "Banner request threw", t)
        }
    }

    private fun appendLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "[$time] $message"
        Log.d(TAG, line)
        if (::logView.isInitialized) {
            runOnUiThread {
                logView.append(line + "\n")
            }
        }
    }
}
