package org.microg.gms.common

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import org.microg.gms.common.Constants

/**
 * utilities to spoof package information for Morphe / ReVanced patched apps
 */
object PackageSpoofUtils {
    private const val TAG = "SpoofUtils"
    private val META_SPOOF_PACKAGE_NAME =
        "${Constants.GMS_PACKAGE_NAME}.SPOOFED_PACKAGE_NAME"
    private val META_SPOOF_PACKAGE_SIGNATURE =
        "${Constants.GMS_PACKAGE_NAME}.SPOOFED_PACKAGE_SIGNATURE"

    private val spoofedPackageNameCache = HashMap<String, String>()
    private val spoofedPackageSignatureCache = HashMap<String, String>()

    @JvmStatic
    fun spoofPackageName(
        packageManager: PackageManager,
        realPackageName: String?
    ): String? {
        if (realPackageName.isNullOrEmpty()) return realPackageName

        val spoofedPackageName = getSpoofedPackageName(packageManager, realPackageName)
        return if (!spoofedPackageName.isNullOrEmpty()) {
            Log.i(TAG, "package name of $realPackageName spoofed to $spoofedPackageName")
            spoofedPackageName
        } else realPackageName
    }

    @JvmStatic
    @JvmName("spoofStringSignature")
    fun spoofSignature(
        packageManager: PackageManager,
        packageName: String,
        realSignature: String?
    ): String? {
        val spoofedSignature = getSpoofedSignature(packageManager, packageName)
        return if (!spoofedSignature.isNullOrEmpty()) {
            Log.i(TAG, "package signature of $packageName spoofed to $spoofedSignature")
            spoofedSignature
        } else realSignature
    }

    @JvmStatic
    @JvmName("spoofBytesSignature")
    fun spoofSignature(
        packageManager: PackageManager,
        packageName: String,
        realSignature: ByteArray?
    ): ByteArray? {
        val spoofedSignatureString = getSpoofedSignature(packageManager, packageName)
        return if (!spoofedSignatureString.isNullOrEmpty()) {
            Log.i(TAG, "package signature of $packageName spoofed to $spoofedSignatureString")

            // convert hex string to bytes
            spoofedSignatureString.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        } else realSignature
    }

    private fun getSpoofedPackageName(
        packageManager: PackageManager,
        packageName: String
    ): String? {
        return spoofedPackageNameCache[packageName] ?: run {
            val meta = getPackageMetadata(packageManager, packageName)
            val spoofedPackageName = meta?.getString(META_SPOOF_PACKAGE_NAME)
                ?: meta?.getString("app.revanced.android.gms.SPOOFED_PACKAGE_NAME")
                ?: meta?.getString("app.morphe.android.gms.SPOOFED_PACKAGE_NAME")
            if (spoofedPackageName != null) {
                spoofedPackageNameCache[packageName] = spoofedPackageName
            }
            spoofedPackageName
        }
    }

    private fun getSpoofedSignature(
        packageManager: PackageManager,
        packageName: String
    ): String? {
        return spoofedPackageSignatureCache[packageName] ?: run {
            val meta = getPackageMetadata(packageManager, packageName)
            val spoofedSignature = meta?.getString(META_SPOOF_PACKAGE_SIGNATURE)
                ?: meta?.getString("app.revanced.android.gms.SPOOFED_PACKAGE_SIGNATURE")
                ?: meta?.getString("app.morphe.android.gms.SPOOFED_PACKAGE_SIGNATURE")
            if (spoofedSignature != null) {
                spoofedPackageSignatureCache[packageName] = spoofedSignature
            }
            spoofedSignature
        }
    }

    private fun getPackageMetadata(packageManager: PackageManager, packageName: String): Bundle? {
        return try {
            packageManager
                .getPackageInfo(packageName, PackageManager.GET_META_DATA)
                ?.applicationInfo
                ?.metaData
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "failed to get application metadata for $packageName", e)
            null
        }
    }
}
