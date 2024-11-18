package com.shpakovskiy.cambot.util

import android.content.Context
import android.net.ConnectivityManager

fun getDeviceIpAddress(context: Context): String? {
    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as? ConnectivityManager ?: return null

    val linkProperties = connectivityManager.getLinkProperties(
        connectivityManager.activeNetwork
    ) ?: return null

    return linkProperties.linkAddresses.last().address.toString().replace("/", "")
}