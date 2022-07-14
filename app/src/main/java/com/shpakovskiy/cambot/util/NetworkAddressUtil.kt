package com.shpakovskiy.cambot.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties

fun getDeviceIpAddress(context: Context): String {
    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    val linkProperties = connectivityManager.getLinkProperties(
        connectivityManager.activeNetwork
    ) as LinkProperties
    return linkProperties.linkAddresses.last().address.toString().replace("/", "")
}