package com.kratos.fflauncher

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object GameStatusReader {
    private const val TARGET_PACKAGE = "com.dts.freefireth"

    fun readGameStatus(context: Context): GameStatus {
        return try {
            val packageManager = context.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(TARGET_PACKAGE, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(TARGET_PACKAGE, 0)
            }
            val versionName = info.versionName ?: "desconhecida"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            GameStatus(
                installed = true,
                versionName = versionName,
                versionCode = versionCode,
                message = "Free Fire encontrado. Versão: $versionName"
            )
        } catch (_: PackageManager.NameNotFoundException) {
            GameStatus(false, message = "Free Fire não encontrado neste dispositivo.")
        } catch (e: Exception) {
            GameStatus(false, message = "Não foi possível verificar o jogo: ${e.message ?: "erro"}")
        }
    }
}
