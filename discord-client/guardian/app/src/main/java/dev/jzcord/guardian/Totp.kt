package dev.jzcord.guardian

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * RFC 6238 TOTP: given a base32 secret (what Discord shows under the 2FA
 * QR code), produces the current 6-digit code the same way the Google
 * Authenticator / Authy / Discord's own 2FA screen does.
 */
object Totp {
    fun generate(secret: String): String {
        val key = base32Decode(secret.filterNot { it == ' ' || it == '-' }.uppercase())
        if (key.isEmpty()) return ""
        val timeStep = System.currentTimeMillis() / 1000 / 30
        val time = ByteArray(8)
        var value = timeStep
        for (i in 7 downTo 0) {
            time[i] = (value and 0xff).toByte()
            value = value shr 8
        }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(time)
        val offset = (hash[hash.size - 1].toInt() and 0x0f)
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            (hash[offset + 3].toInt() and 0xff)
        val otp = binary % 1_000_000
        return "%06d".format(otp)
    }

    private fun base32Decode(input: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = input.trimEnd('=').filter { it in alphabet }
        if (cleaned.isEmpty()) return ByteArray(0)
        val output = ByteArray(cleaned.length * 5 / 8)
        var buffer = 0
        var bitsLeft = 0
        var index = 0
        for (char in cleaned) {
            val value = alphabet.indexOf(char)
            if (value < 0) return ByteArray(0)
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output[index++] = ((buffer shr (bitsLeft - 8)) and 0xff).toByte()
                bitsLeft -= 8
            }
        }
        return output
    }
}
