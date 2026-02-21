package com.robocar.app.ui.blockly

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.robocar.app.MainViewModel
import com.robocar.app.ble.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BlocklyScreen(viewModel: MainViewModel) {
    val sensorData by viewModel.sensorData.collectAsState()
    var webView: WebView? by remember { mutableStateOf(null) }

    // Push sensor data to WebView whenever it changes
    LaunchedEffect(sensorData) {
        webView?.let { wv ->
            val js = """
                if (window.sensorData !== undefined) {
                    window.sensorData = [${sensorData.p1}, ${sensorData.p2}, ${sensorData.p3}, ${sensorData.p4}];
                    var d = window.sensorData;
                    var e1 = document.getElementById('valP1');
                    var e2 = document.getElementById('valP2');
                    var e3 = document.getElementById('valP3');
                    var e4 = document.getElementById('valP4');
                    if(e1) e1.innerText = d[0];
                    if(e2) e2.innerText = d[1];
                    if(e3) e3.innerText = d[2];
                    if(e4) e4.innerText = d[3];
                }
            """.trimIndent()
            wv.post { wv.evaluateJavascript(js, null) }
        }
    }

    // When BLE connects/disconnects, update JS state
    val bleState by viewModel.bleState.collectAsState()
    LaunchedEffect(bleState) {
        val connected = viewModel.bleManager.isConnected
        val deviceName = if (bleState is com.robocar.app.ble.BleState.Connected)
            (bleState as com.robocar.app.ble.BleState.Connected).deviceName else ""
        webView?.post {
            webView?.evaluateJavascript(
                "if(window.setConnectionState) window.setConnectionState($connected, '$deviceName'); else window.isConnected = $connected;",
                null
            )
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D141F)),
        factory = { context ->
            createBlocklyWebView(context, viewModel).also { wv ->
                webView = wv
            }
        },
        update = { wv ->
            webView = wv
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
fun createBlocklyWebView(context: Context, viewModel: MainViewModel): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Inject Android bridge after page loads
                val initJs = """
                    // Mark as running inside Android APK
                    window.isAndroidApp = true;
                    window.isConnected = ${viewModel.bleManager.isConnected};
                    window.sensorData = [0, 0, 0, 0];
                    
                    // Override sendCarPacket to use Android BLE
                    window.sendCarPacket = function(l, r) {
                        return new Promise(function(resolve) {
                            Android.sendCarPacket(Math.round(l), Math.round(r));
                            resolve();
                        });
                    };
                    
                    // Override sendDrivePacket
                    window.sendDrivePacket = function(m1, m2, m3, m4) {
                        return new Promise(function(resolve) {
                            Android.sendDrivePacket(
                                Math.round(m1), Math.round(m2),
                                Math.round(m3), Math.round(m4)
                            );
                            resolve();
                        });
                    };
                    
                    // Override runCode to not check connection via alert
                    var _originalRunCode = window.runCode;
                    window.runCode = async function() {
                        if (!window.isConnected) {
                            Android.showToast("Спочатку підключіться до Bluetooth!");
                            return;
                        }
                        if (_originalRunCode) await _originalRunCode();
                    };
                    
                    // Log bridge
                    window.androidLog = function(msg, type) {
                        Android.log(msg, type || 'info');
                    };
                    
                    console.log = function() {
                        try { Android.log(Array.from(arguments).join(' '), 'info'); } catch(e) {}
                    };
                    
                    console.error = function() {
                        try { Android.log(Array.from(arguments).join(' '), 'err'); } catch(e) {}
                    };
                """.trimIndent()

                view.evaluateJavascript(initJs, null)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                return true
            }
        }

        // Inject JavaScript Interface
        addJavascriptInterface(
            AndroidBridge(viewModel, this),
            "Android"
        )

        // Load the local HTML from assets
        loadUrl("file:///android_asset/web/index.html")
    }
}

class AndroidBridge(
    private val viewModel: MainViewModel,
    private val webView: WebView
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    @JavascriptInterface
    fun onConnectClicked() {
        scope.launch { viewModel.onConnectClicked() }
    }

    @JavascriptInterface
    fun sendCarPacket(l: Int, r: Int) {
        viewModel.bleManager.sendCarPacket(l, r)
    }

    @JavascriptInterface
    fun sendDrivePacket(m1: Int, m2: Int, m3: Int, m4: Int) {
        viewModel.bleManager.sendDrivePacket(m1, m2, m3, m4)
    }

    @JavascriptInterface
    fun log(msg: String, type: String) {
        // Log is already handled in ViewModel
    }

    @JavascriptInterface
    fun showToast(msg: String) {
        scope.launch {
            android.widget.Toast.makeText(
                webView.context,
                msg,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    @JavascriptInterface
    fun isConnected(): Boolean = viewModel.bleManager.isConnected

    @JavascriptInterface
    fun getSensorData(): String {
        val s = viewModel.sensorData.value
        return "[${s.p1},${s.p2},${s.p3},${s.p4}]"
    }

    @JavascriptInterface
    fun recordMove(m1: Int, m2: Int, m3: Int, m4: Int) {
        viewModel.recordMove(m1, m2, m3, m4)
    }

    @JavascriptInterface
    fun recordWait(sec: Double) {
        viewModel.recordWait(sec)
    }

    @JavascriptInterface
    fun startTrackRecording() {
        viewModel.startRecording()
    }

    @JavascriptInterface
    fun stopTrackRecording() {
        viewModel.stopRecording()
    }

    @JavascriptInterface
    fun replayTrack(times: Int) {
        viewModel.replayTrack(times)
    }

    @JavascriptInterface
    fun goHome() {
        viewModel.goHome()
    }
}
