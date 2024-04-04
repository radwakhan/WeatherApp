package com.radroid.weatherapp

data class WeatherResponse(
    val coord:Coord,
    val weather:MutableList<Weather>,
    val base:String,
    val main:Main,
    val visibility:Int,
    val wind:Wind,
    val cloud:Clouds,
    val dt:Int,
    val sys:Sys,
    val timeZone:Int,
    val id:Int,
    val name:String,
    val cod:Int

)