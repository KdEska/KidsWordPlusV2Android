package com.demarpsoftware.kidswordplus

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Requires a validated internet connection; a Wi-Fi link alone is insufficient. */
@Composable
internal fun InternetRequired(onDisconnected: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val manager = remember(context) { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    fun connected(): Boolean = manager.getNetworkCapabilities(manager.activeNetwork)?.let {
        it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } == true
    var online by remember { mutableStateOf(connected()) }
    val disconnected by rememberUpdatedState(onDisconnected)
    DisposableEffect(manager, lifecycle) {
        val handler = Handler(Looper.getMainLooper())
        var disposed = false
        fun refresh() { if (!disposed) online = connected() }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = refresh()
            override fun onLost(network: Network) = refresh()
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = refresh()
        }
        manager.registerDefaultNetworkCallback(callback, handler)
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        lifecycle.lifecycle.addObserver(observer)
        refresh()
        onDispose {
            disposed = true
            manager.unregisterNetworkCallback(callback)
            lifecycle.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(online) { if (!online) disconnected() }
    if (!online) {
        val tr = InterfaceStrings.language == "tr"
        fun t(a: String, b: String) = InterfaceStrings.text(tr, a, b)
        Dialog(onDismissRequest = {}, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
        )) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t("İnternet bağlantısı gerekli", "Internet connection required"),
                        style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(t("Devam etmek için Wi-Fi veya mobil internete bağlanın. Bağlantı geldiğinde kaldığınız yerden devam edebilirsiniz.",
                        "Connect to Wi-Fi or mobile internet to continue. You can resume where you left off when the connection returns."),
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { online = connected() }) {
                        Text(t("Tekrar dene", "Try again"))
                    }
                }
            }
        }
    }
}
