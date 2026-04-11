/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */
package org.breezyweather.common.utils

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.VisibleForTesting
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * Get the SHA256 certificate for an installed Android app.
 * Cherry-picked from android-components of firefox-mobile
 * Mozilla Public Licence v2.0
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/service/digitalassetlinks/src/main/java/mozilla/components/service/digitalassetlinks/AndroidAssetFinder.kt
 */
object AndroidSignatureFinder {

    /**
     * Converts the Android App with the given package name into an asset descriptor
     * by computing the SHA256 certificate for each signing signature.
     *
     * The output is lazily computed. If desired, only the first item from the sequence could
     * be used and other certificates (if any) will not be computed.
     */
    fun getAndroidSignatures(
        packageName: String,
        packageManager: PackageManager,
    ): Sequence<String> {
        return packageManager.getSignatures(packageName).asSequence()
            .mapNotNull { signature -> getCertificateSHA256Fingerprint(signature) }
    }

    /**
     * Get a package info with a specified flag
     *
     * @param packageName The name of the package to check for.
     */
    fun PackageManager.getPackageInfoCompat(packageName: String, flag: Int): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flag.toLong()))
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, flag)
        }
    }

    /**
     * Computes the SHA256 certificate for the given package name. The app with the given package
     * name has to be installed on device. The output will be a 30 long HEX string with : between
     * each value.
     * @return The SHA256 certificate for the package name.
     */
    @VisibleForTesting
    internal fun getCertificateSHA256Fingerprint(signature: Signature): String? {
        val input = ByteArrayInputStream(signature.toByteArray())
        return try {
            val certificate = CertificateFactory.getInstance("X509").generateCertificate(input) as X509Certificate
            byteArrayToHexString(MessageDigest.getInstance("SHA256").digest(certificate.encoded))
        } catch (e: CertificateEncodingException) {
            // Certificate type X509 encoding failed
            null
        } catch (e: CertificateException) {
            throw AssertionError("Should not happen", e)
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError("Should not happen", e)
        }
    }

    @Suppress("PackageManagerGetSignatures")
    // https://stackoverflow.com/questions/39192844/android-studio-warning-when-using-packagemanager-get-signatures
    private fun PackageManager.getSignatures(packageName: String): Array<Signature> {
        val packageInfo = getPackageSignatureInfo(packageName) ?: return emptyArray()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo
            if (signingInfo === null) {
                emptyArray()
            } else {
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    val history = signingInfo.signingCertificateHistory
                    if (history.isEmpty()) {
                        emptyArray()
                    } else {
                        arrayOf(history.first())
                    }
                }
            }
        } else {
            @Suppress("Deprecation")
            packageInfo.signatures
        } ?: emptyArray()
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun PackageManager.getPackageSignatureInfo(packageName: String): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getPackageInfoCompat(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("Deprecation")
                getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Will return null if there is no package found.
            return null
        }
    }

    /**
     * Converts a byte array to hex string with : inserted between each element.
     * @param bytes The array to be converted.
     * @return A string with two letters representing each byte and : in between.
     */
    @Suppress("MagicNumber")
    @VisibleForTesting
    internal fun byteArrayToHexString(bytes: ByteArray): String {
        val hexString = StringBuilder(bytes.size * HEX_STRING_SIZE - 1)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexString.append(HEX_CHAR_LOOKUP[v.ushr(HALF_BYTE)])
            hexString.append(HEX_CHAR_LOOKUP[v and 0x0F])
            if (j < bytes.lastIndex) hexString.append(':')
        }
        return hexString.toString()
    }

    private const val HALF_BYTE = 4
    private const val HEX_STRING_SIZE = "0F:".length
    private val HEX_CHAR_LOOKUP = "0123456789ABCDEF".toCharArray()
}
