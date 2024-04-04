package com.radroid.weatherapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constants {
    const val APP_ID="a9d5bf1a62d5cf2d0122834fda6ff0ab"
    const val BASE_URL="https://api.openweathermap.org/data/"
    const val METRIC_UNIT="metric"



//    define a function called isNetworkAvailable that takes the context parameter and return a boolean value
@SuppressLint("ObsoleteSdkInt")
fun isNetworkAvailable(context: Context):Boolean{
//        get an instance of connectivity manager using the context parameter
        val connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

//    check if the device api is level 23 or high
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//        get the currently active network and return false if there is none
        val network=connectivityManager.activeNetwork?:return false

//        get the capabilities of the active network and return false if it has none
        val activeNetwork=connectivityManager.getNetworkCapabilities(network)?:return false

//        check the type of the active network and return true if it is either wifi,cellular or ethernet
        return when{
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->return true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->return true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->return true
            else->return false
        }

    }else{
//for api levels below 23, get the active network info and return true if it is conecting
        val networkInfo=connectivityManager.activeNetworkInfo
        return  networkInfo!=null &&networkInfo.isConnectedOrConnecting
    }
    }
}