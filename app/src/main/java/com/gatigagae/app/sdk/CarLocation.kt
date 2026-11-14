package com.gatigagae.app.sdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager

/**
 * 차의 현재 위치 (장소 검색 기준점)
 * 위치 권한이 있으면 안드로이드가 마지막으로 알고 있는 위치를 쓰고,
 * 없거나 이상한 값(한국 밖)이면 기본 위치를 써요.
 */
object CarLocation {

    // Pleos 에뮬레이터 내비에 나온 위치(성남시 수정구 시흥동) 근처
    const val DEFAULT_LAT = 37.4386
    const val DEFAULT_LNG = 127.1006

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    fun hasPermission(context: Context): Boolean =
        permissions.any { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    @SuppressLint("MissingPermission")
    fun current(context: Context): Pair<Double, Double> {
        if (!hasPermission(context)) return DEFAULT_LAT to DEFAULT_LNG
        val manager = context.getSystemService(LocationManager::class.java) ?: return DEFAULT_LAT to DEFAULT_LNG
        val latest = runCatching {
            manager.getProviders(true)
                .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
                .maxByOrNull { it.time }
        }.getOrNull() ?: return DEFAULT_LAT to DEFAULT_LNG

        // 에뮬레이터 기본값(미국)처럼 한국 밖이면 기본 위치로
        val inKorea = latest.latitude in 33.0..39.0 && latest.longitude in 124.0..132.0
        return if (inKorea) latest.latitude to latest.longitude else DEFAULT_LAT to DEFAULT_LNG
    }
}
